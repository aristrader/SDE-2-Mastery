---
order: 10
---

# OS, Virtualization, and Containers Fundamentals

## Operating System and Kernel Architecture

An **Operating System (OS)** manages physical hardware resources (CPU, RAM, disk, networking) and provides an execution environment for user-space applications.

- **Kernel**: The privileged core of the OS executing in CPU Ring 0 (kernel mode). It manages CPU scheduling, virtual memory paging, block storage/VFS, network stacks, and process lifecycles.
- **System Calls (syscalls)**: The programmatic interface (`read`, `write`, `fork`, `execve`, `clone`, `epoll_create`) through which user-space processes (Ring 3) request privileged kernel operations via software interrupts or CPU instructions (`SYSCALL`/`SYSENTER`).
- **Device Drivers**: Kernel modules that translate generic OS subsystem commands into device-specific hardware protocols (e.g., NVMe, GPU, NIC).
- **User Space vs Kernel Space**: Processes execute in isolated user address spaces with restricted CPU instructions. A crash in user space kills only that process; a panic in the kernel crashes the entire operating system.

```
+-------------------------------------------------------------------+
| User Space (Ring 3)                                               |
|  [Java JVM / App]     [MySQL]     [Shell: bash/zsh]   [CLI Tools] |
|       |                  |                |                |      |
|  [Standard C Library (glibc / musl)]                              |
+-------+------------------+----------------+----------------+------+
        | System Call Interface (syscalls: clone, epoll, futex)     |
+-------v-----------------------------------------------------------+
| Kernel Space (Ring 0)                                             |
|  [CPU Scheduler]  [Virtual Memory / Page Tables]  [VFS / Storage]  |
|  [Network Stack (TCP/IP)]  [IPC]  [Namespaces & Cgroups Subsystems]|
|  [Device Drivers (NIC, Disk, GPU)]                                |
+-----------------------------------+-------------------------------+
                                    |
+-----------------------------------v-------------------------------+
| Physical Hardware (CPU, DRAM, NVMe Storage, NIC, Motherboard)     |
+-------------------------------------------------------------------+
```

---

## Processes vs Threads

- **Process**: An independent instance of a running program (`java`, `mysqld`, `chrome`) owning a dedicated virtual address space, file descriptor table, and security context.
- **Thread**: An execution unit within a process sharing the same virtual memory and file descriptors, but maintaining its own stack, program counter, and registers.
- All processes on a standard OS share the same underlying kernel.

---

## Virtual Machines (VMs) and Hypervisors

A **Virtual Machine (VM)** is a complete virtualization of physical hardware, providing the illusion of a dedicated physical machine with its own virtualized CPU, memory, storage devices, and virtual network interfaces (NICs).

### Hypervisors (Virtual Machine Monitors - VMM)
The hypervisor is the software/firmware layer responsible for creating, scheduling, and isolating VMs.

1. **Type-1 (Bare-Metal) Hypervisors**:
   - Runs directly on bare physical server hardware without a host OS (e.g., VMware ESXi, KVM via Linux kernel-based module, Xen, AWS Nitro / Hyper-V).
   - Near bare-metal performance, minimal virtualization overhead, enterprise standard for cloud and production virtualization.
2. **Type-2 (Hosted) Hypervisors**:
   - Runs as an application inside a host operating system (e.g., VirtualBox, VMware Workstation).
   - Incurs double-scheduling overhead (guest OS -> hypervisor -> host OS -> hardware); suitable for local desktop development.

### Failure and Security Isolation
- **VM-Level Failure**: An OS panic, kernel crash, or out-of-memory condition in a guest VM affects only that guest VM. Other VMs on the same hypervisor continue running.
- **Host Hardware Failure**: Physical CPU, memory, power supply, or host motherboard failure brings down every VM co-located on that physical server.
- **Security Boundary**: Hardware-enforced virtualization (Intel VT-x, AMD-V, nested page tables/EPT/NPT). Guest code cannot directly inspect or tamper with host or sibling VM memory without hypervisor escape vulnerabilities.

---

## Containers: OS-Level Virtualization

A **container** is not a virtual machine and contains no hypervisor or guest kernel. A container is a **standard host process** constrained and isolated by Linux kernel primitives: **Namespaces** and **Control Groups (cgroups)**.

### 1. Linux Namespaces (What a process can see)
Namespaces partition kernel resources so a process sees an isolated workspace:

| Namespace | Linux Flag | What it Isolates |
| :--- | :--- | :--- |
| **PID** | `CLONE_NEWPID` | Process IDs. The container entrypoint becomes PID 1 inside, but maps to a standard PID (e.g., 28412) on the host. |
| **NET** | `CLONE_NEWNET` | Network stack: independent IP addresses, routing tables, port bindings (`:8080`), firewall rules (`iptables`/`nftables`), and `veth` pairs. |
| **MNT** | `CLONE_NEWNS` | Filesystem mount points. Isolates root filesystem (`/`) views. |
| **IPC** | `CLONE_NEWIPC` | System V IPC and POSIX message queues, preventing shared memory snooping between containers. |
| **UTS** | `CLONE_NEWUTS` | Hostname and NIS domain name. |
| **USER** | `CLONE_NEWUSER` | Maps container root (UID 0 inside) to an unprivileged user (UID 10001 outside) for privilege containment. |
| **CGROUP** | `CLONE_NEWCGROUP` | Isolates the container's view of its own cgroup hierarchy. |

### 2. Control Groups / cgroups (How much a process can use)
cgroups meter, throttle, and enforce hard and soft resource limits:
- **CPU (`cpu.max`, `cpu.weight`)**: Quota enforcement via Completely Fair Scheduler (CFS) bandwidth (e.g., 200ms quota per 100ms period = 2 vCPUs).
- **Memory (`memory.max`, `memory.high`)**: Hard limits. If exceeded without available swap, the Linux kernel Out-Of-Memory (OOM) Killer terminates the offending process inside the container (`Exit Code 137`).
- **Disk I/O (`io.max`)**: Throttles IOPS and read/write byte rates to prevent noisy-neighbor storage saturation.
- **PIDs (`pids.max`)**: Limits total process and thread count to prevent `fork()` bomb denial-of-service attacks.

---

## Container Images and OCI Specifications

Container images adhere to the **Open Container Initiative (OCI)** open standards:
- **OCI Image Specification**: An immutable tar-based bundle containing layered root filesystems (Layer 1: Base OS, Layer 2: JDK runtime, Layer 3: JAR/App code) plus a JSON manifest describing entrypoints, environment variables, and architecture.
- **Union Filesystems (OverlayFS)**: Combines multiple read-only image layers into a single unified filesystem view, with a lightweight read-write layer on top (Copy-On-Write).
- **OCI Runtime Specification (`runc`)**: Low-level runtime that consumes the unpacked rootfs and `config.json` to issue `clone()`, `unshare()`, `setns()`, and mount syscalls to launch the container.

---

## VMs vs Containers: Architectural Trade-Offs

| Dimension | Virtual Machine (VM) | Container |
| :--- | :--- | :--- |
| **Isolation Boundary** | Hardware-level (Hypervisor, virtual BIOS/CPU/RAM). Separate guest kernel. | OS-level (Namespaces + Cgroups). Shared host kernel. |
| **Startup Latency** | Seconds to minutes (full OS boot, init scripts, hardware probing). | Milliseconds to seconds (standard `fork`/`clone` process execution). |
| **Resource Overhead** | High (500MB - 2GB RAM per VM for guest OS kernel and system daemons). | Low (Megabytes of RAM; only application memory overhead). |
| **Density** | Tens of VMs per physical host. | Hundreds to thousands of containers per host. |
| **Kernel Flexibility** | Can run different OS kernels on the same host (e.g., Linux and Windows VMs on one hypervisor). | Restricted to the host kernel ABI (Linux container requires Linux host). |
| **Blast Radius & Security** | Strong hardware isolation. Guest kernel exploit does not compromise host. | Kernel exploit compromises host and co-located containers unless sandboxed. |

---

## Orchestration Boundaries and Workload Placement

Modern distributed systems separate concerns across layers:
1. **Physical / Cloud Infrastructure (IaaS)**: Manages bare-metal hardware, power, physical networks, and Type-1 hypervisors (AWS EC2 Nitro, GCP Compute Engine).
2. **Container Orchestrator (Kubernetes, ECS)**: Schedules containers onto a fleet of VMs/nodes, handling declarative desired-state reconciliation, service discovery, rolling updates, ingress routing, and horizontal pod autoscaling (HPA).
3. **Application Workload**: Packaged as OCI containers, agnostic of the underlying physical machine.

---

## Failure, Recovery, and Selection Decision Matrix

### When to Choose VMs vs Containers
- **Choose Bare VMs / Dedicated Instances**:
  - Legacy stateful monolithic workloads tightly coupled to custom kernel modules or specific OS kernels (e.g., Windows Server kernels, specialized Solaris/FreeBSD legacy stacks).
  - Multi-tenant untrusted code execution where kernel-level security isolation is non-negotiable.
  - Hard real-time hardware-bound workloads (dedicated GPU/FPGA passthrough without virtualization layers).
- **Choose Containers**:
  - Microservices, stateless web services, event consumers, and batch processing jobs.
  - High-density multi-service architectures with fast horizontal auto-scaling requirements.
  - Standardized CI/CD build artifacts that ensure "build once, run anywhere" parity across dev, staging, and production.

### Failure Modes and Recovery Trade-offs

```
+--------------------------+------------------------------+------------------------------------+
| Failure Mode             | Blast Radius                 | Recovery Mechanism                |
+--------------------------+------------------------------+------------------------------------+
| Process Crash / OOM      | Single Container             | Orchestrator restarts container    |
| (Exit 137 / Unhandled)   | (App-level only)             | via liveness probe (< 2 sec)       |
+--------------------------+------------------------------+------------------------------------+
| Host Node Kernel Panic / | All Containers on that Node  | Orchestrator node lease expires    |
| VM Hardware Failure      | (Entire VM/Node)             | -> Reschedules Pods to healthy     |
|                          |                              | nodes in cluster (30s - 2 min)     |
+--------------------------+------------------------------+------------------------------------+
| Availability Zone (AZ)   | All Nodes in that Datacenter | Multi-AZ deployment + Global/Cloud |
| Power / Network Outage   | (Subnet/Datacenter level)    | Load Balancer health check reroute |
+--------------------------+------------------------------+------------------------------------+
```

---

## Quick recall

1. **What is the fundamental architectural difference between a VM and a container?**
   A VM virtualizes hardware and runs a separate guest OS with its own kernel; a container is an isolated host process sharing the host OS kernel via Linux namespaces and cgroups.

2. **What role do Linux Namespaces play in containerization?**
   Namespaces provide isolation by restricting what a process can *see* (e.g., PID for process IDs, NET for network interfaces/IPs, MNT for filesystem mounts).

3. **What role do Linux cgroups play?**
   Control groups enforce resource metering and limits on how much a process can *use* (CPU quota, memory limits, disk I/O bandwidth, PID count).

4. **Why do containers start significantly faster than virtual machines?**
   Containers only execute a `clone`/`execve` syscall to start a host process; VMs must boot an entire virtual BIOS, load a guest kernel, initialize virtual hardware drivers, and run OS init systems.

5. **What causes a Container Exit Code 137?**
   The container process exceeded its memory cgroup limit (`memory.max`), triggering the Linux kernel Out-Of-Memory (OOM) Killer to send `SIGKILL` (`128 + 9 = 137`).

6. **Why is a hypervisor considered a stronger security boundary than a container?**
   Hypervisors enforce isolation via CPU hardware virtualization rings and separate guest kernels; a standard container shares the host kernel, making host compromise possible if a kernel-level vulnerability is exploited.
