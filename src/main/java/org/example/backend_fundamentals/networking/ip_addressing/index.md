---
order: 60
---

# IP Addressing, NAT, DHCP, CGNAT, MAC Addresses, and Internet Reachability

## How it works

### Two independent classifications of IP addresses

A common wrong assumption: public IP = static IP, private IP = dynamic IP. Incorrect — these are two completely different dimensions.

| Dimension | Type | Meaning |
|-----------|------|---------|
| Reachability | Public | Reachable from the internet |
| Reachability | Private | Reachable only within a private network |
| Assignment | Static | Does not change |
| Assignment | Dynamic | Can change over time |

Any combination is possible:

| Reachability | Assignment | Example |
|--------------|------------|---------|
| Public | Static | Load balancer, public-facing server |
| Public | Dynamic | Home broadband connection |
| Private | Static | Database server in a VPC |
| Private | Dynamic | Laptop, phone, Kubernetes Pod |

### Public IPs

A public IP is globally routable — anyone on the internet can send traffic to it.

```text
api.company.com → 13.233.50.120
```

Common usage: public load balancers, public APIs, internet-facing services.

### Private IPs

Private IPs are used inside local networks and are not routable on the public internet. The reserved private ranges:

```text
10.0.0.0/8
172.16.0.0 – 172.31.255.255
192.168.0.0/16
```

A typical home network: laptop `192.168.1.2`, phone `192.168.1.3`, router `192.168.1.1`. These addresses only have meaning inside that network.

### Static IPs

A static IP remains fixed — a database at `10.0.1.20` today is still `10.0.1.20` next month. Used when other systems need a predictable address: databases, DNS servers, VPN servers, load balancers.

### Dynamic IPs

Assigned automatically and may change — `192.168.1.10` today, `192.168.1.15` tomorrow. Typical for employee laptops, phones, home devices, Kubernetes Pods, cloud VMs.

## DHCP

DHCP (Dynamic Host Configuration Protocol) lets a device join a network and automatically receive an IP address. Without it, every phone/laptop/TV would need manual IP configuration.

```text
Phone joins WiFi → requests address → router assigns IP
```

In home networks the router **is** the DHCP server, and it keeps a lease table mapping devices to assigned addresses.

### DHCP leases

A device is identified by its MAC address (`AA:BB:CC:DD:EE:FF`); the router assigns an IP (`192.168.1.10`) and stores the MAC → IP mapping for some lease duration.

Important: the IP is **not** permanently tied to the MAC. The DHCP server is simply remembering "this MAC currently owns this IP" — the mapping can change later.

## Why IPv4 addresses are scarce

IPv4 has 2^32 ≈ 4.3 billion possible addresses. The world has more people than IPv4 addresses, and far more devices. This shortage is one of the primary reasons NAT exists.

## NAT (Network Address Translation)

### The problem NAT solves

Inside a home network, laptop `192.168.1.2`, phone `192.168.1.3`, TV `192.168.1.4` are private IPs. Google cannot route traffic directly back to `192.168.1.2` — private addresses are not globally routable.

### What NAT does

The laptop sends a request with source `192.168.1.2`, destination `google.com`. The router rewrites the source to the public IP:

```text
Source 192.168.1.2  →  Source 49.204.10.15  →  google.com
```

Google sees `49.204.10.15`, not `192.168.1.2`. The router is effectively representing all local devices to the outside world.

### NAT uses ports, not just IP addresses

This is the crucial detail. When the laptop opens a connection from `192.168.1.10:54321`, the router may translate it to `49.204.10.15:60001` and store the mapping in its NAT table:

```text
49.204.10.15:60001  →  192.168.1.10:54321
```

When the response returns to `49.204.10.15:60001`, the router looks up the mapping and forwards it correctly. This is how thousands of simultaneous connections share one public IP.

## CGNAT (Carrier-Grade NAT)

### NAT on top of NAT

Many ISPs do not give each customer a unique public IP — they add their own NAT layer:

```text
Phone            192.168.1.10
  ↓ Home router NAT
ISP private IP   100.64.10.25
  ↓ ISP CGNAT
Public IP        49.204.10.15
  ↓
Internet
```

Three addresses can exist for one device: `192.168.1.10`, `100.64.10.25`, `49.204.10.15`. CGNAT is effectively NAT → NAT → Internet.

### ISP internal addressing

ISPs maintain internal address space for customer routers — e.g. Customer A `100.64.1.10`, Customer B `100.64.1.11`, Customer C `100.64.1.12` — all ultimately sharing `49.204.10.15` as the public-facing IP.

## MAC addresses

### What a MAC address is

MAC addresses identify network interfaces, e.g. `A4:5E:60:2B:11:9C`.

```text
MAC = device identity
IP  = current network location
```

A device can change locations; its MAC generally remains the same.

### IP vs MAC

| Property | IP Address | MAC Address |
|----------|------------|-------------|
| Purpose | Network location | Device/interface identity |
| Can change | Yes | Usually no |
| Used across internet | Yes | No |
| Used in local network | Yes | Yes |

A useful mental model: MAC = Aadhaar number, IP = hotel room number. You can move rooms; your identity remains the same.

### Why internet routing uses IPs, not MACs

Packets travel through many routers (laptop → home router → ISP router → Google router → Google server). MAC addresses are replaced at **every hop**; IP addresses remain the routing identifier — therefore internet routing uses IPs.

### Are MAC addresses unique?

Manufacturers receive allocated MAC ranges; the first portion identifies the vendor. A MAC is 48 bits → 2^48 ≈ 281 trillion possibilities. In theory every MAC should be unique.

### Can MAC addresses be spoofed?

Yes. A MAC address is not a secure identity — devices can intentionally present a different MAC. MAC uniqueness is an operational convention, not a security guarantee.

## How tracing works

If websites only see public IPs, how can users be traced? A website logs the public IP + timestamp (`49.204.10.15` at `10:35:12 PM`). ISP logs can map that back through CGNAT to the internal address (`100.64.1.10`) and the customer account; router records can further map to the local device (`192.168.1.10`) and potentially its MAC address.

The critical information is: public IP, timestamp, and — especially under CGNAT — often the port number.

## Websites and dynamic IPs

### Can websites use dynamic IPs?

Yes — many modern systems do. The key reason this works is DNS: `mywebsite.com` can point to `54.10.10.10` today and `18.20.30.40` tomorrow. DNS is simply updated; users keep accessing the hostname without knowing the underlying IP changed.

### Why interviewers still talk about static IPs

Historically servers commonly used static IPs. Modern cloud systems look like:

```text
User → DNS → Load Balancer → Auto-scaled Servers
```

Backend servers may have dynamic private IPs and be created/destroyed continuously. Only the externally visible entry point needs to remain stable.

### Database servers and private static IPs

Typical architecture:

```text
Internet → Load Balancer → Application Servers → Database (10.0.1.20)
```

The database is **private** (not internet reachable) and **static** (predictable for applications). This combination is common in production systems.

## Gotchas / Trick questions

1. **"Public IP means static IP."** Incorrect — a home broadband connection is often public + dynamic.
2. **"Private IP means dynamic IP."** Incorrect — a database server is often private + static.
3. **"A website must have a static IP."** Not necessarily — DNS allows the IPs behind a hostname to change.
4. **"NAT only changes the source IP."** Incomplete — NAT usually translates source IP **and** source port, maintaining a NAT table. Ports are what allow many simultaneous connections through one public IP.
5. **"My router gets a public IP from the ISP."** Sometimes — under CGNAT your router may receive `100.64.x.x`, which is not actually public; the ISP performs another NAT layer.
6. **"MAC addresses are permanent and cannot be changed."** Incorrect — MAC spoofing is common and supported by operating systems.
7. **"A hacker can directly connect to a private database IP."** Normally impossible from the internet. Breaches usually happen because an application server was compromised, a security group was misconfigured, VPN credentials were stolen, or the attacker moved laterally inside the network.
8. **"A device's IP is permanently tied to its MAC."** Incorrect — DHCP leases associate a MAC with an IP temporarily; the mapping can change later.

## Good to know

### Why databases are usually safer behind private IPs

Private IPs are not globally routable. An attacker on the public internet cannot directly send packets to `10.0.1.20` unless they first gain access to a system that already has connectivity to that network.

### Mental model that ties everything together

Three layers of identity:

```text
DNS name     → human-friendly identifier
IP address   → network location
MAC address  → local hardware identity
```

Modern distributed systems depend on DNS names instead of hardcoded IPs, because IPs frequently change in cloud environments.

## Quick recall

**Q. Are public/private and static/dynamic the same classification?**
A. No. Public/private describes reachability; static/dynamic describes whether the address changes.

**Q. What does DHCP do?**
A. Automatically assigns IP addresses and maintains lease mappings.

**Q. Why is NAT needed?**
A. Because private IPs are not internet-routable and IPv4 addresses are limited.

**Q. What does NAT actually translate?**
A. Typically both source IP and source port, tracked in a NAT table.

**Q. What is CGNAT?**
A. An ISP-level NAT layer sitting above your router's NAT — your router may get `100.64.x.x` instead of a real public IP.

**Q. What is the difference between IP and MAC?**
A. IP identifies network location; MAC identifies a local network interface. MACs are replaced at every routing hop; IPs persist end-to-end.

**Q. Can websites run on dynamic IPs?**
A. Yes. DNS allows hostname-to-IP mappings to change over time.


<ExerciseNav />
