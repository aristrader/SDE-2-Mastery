# OS, Virtualization, and Containers Fundamentals

## Virtual Machines (VMs)
A VM is a virtual computer system running on top of physical hardware, created and managed by a **hypervisor**. It has its own CPU, memory, storage allocation, and network interface. 
- **Why VMs Exist**: Better hardware utilization, server consolidation, isolation, running multiple OSs, environments separation.

### Hypervisor (Virtual Machine Monitor)
Creates VMs, allocates resources (CPU, memory, storage), and isolates VMs.
`Physical Server -> Hypervisor -> VM1, VM2, VM3`

### Failure Isolation
- **VM-Level Failure** (memory leak, OS panic): Only affects the specific VM. Other VMs survive.
- **Physical Hardware Failure** (CPU dies, power failure): All VMs on that server fail.

## Containers
Containers package application code, runtime, dependencies, and libraries. They run consistently across environments and are lightweight compared to VMs.
- **Separation of Responsibility**: Developers focus on logic/dependencies, Operations on infrastructure.
- **Workload Portability**: Run almost anywhere.
- **Application Isolation**: Isolated view of CPU, memory, storage, network.
- **Efficient Operations**: Fewer resources than VMs.

**Important Distinction:**
- **VM** = Virtual Computer (has separate guest OS).
- **Container** = Isolated Process (shares host OS kernel).

## Operating System (OS) and Kernel
- **Operating System**: Manager of the computer (Who gets CPU, RAM, disk, network). Examples: Windows, Ubuntu, MacOS.
- **Kernel**: Core part of OS. Talks directly to hardware. Responsible for CPU scheduling, memory management, disk access, networking, process management.
- **Driver**: Translator between OS and hardware (Printer, GPU, Network Card).

OS structure contains the Kernel, Libraries, Services, Shell, UI, Package Manager. 
- **Shell**: CLI Interface. Bash (most common), Zsh (plugins), PowerShell (Windows).
- **Package Manager**: Resolves dependencies and installs packages (apt, brew, npm, pip, Maven).

## Processes
A process is a running program. 
- Examples: Chrome, MySQL, Java Process.
All processes on an OS share the same Kernel.
