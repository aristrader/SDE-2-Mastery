---
layout: home
hero:
  name: "SDE-2 Mastery"
  text: "Backend Fundamentals, System Design & Patterns"
  tagline: "A local, runnable study workspace — read the theory, run the code, ask the agent."
  actions:
    - theme: brand
      text: "Open Study Plan"
      link: "/todo/study_plan/README"
    - theme: alt
      text: "Design Patterns"
      link: "/design_patterns/"
features:
  - title: "Study Plan"
    details: "The 32-Part execution map and weekly schedule."
    link: "/todo/study_plan/README"
  - title: "Java & JVM"
    details: "Streams, records, concurrency, the memory model, generics."
    link: "/java/"
  - title: "Design Patterns"
    details: "Creational patterns and OOP/SOLID foundations — with runnable demos."
    link: "/design_patterns/"
  - title: "System Design"
    details: "Clustering, caching, load balancing, CDNs, fault tolerance."
    link: "/system_design/"
  - title: "Spring"
    details: "MVC, security, JPA, Feign, auto-configuration."
    link: "/spring/"
  - title: "Networking & Databases"
    details: "IP/NAT/DHCP, DNS, TCP vs UDP, SQL vs NoSQL, graph DBs."
    link: "/networking/"
---

## How this site works

Every topic folder that contains Java shows a **Playground** tab next to **Read**: browse the
folder's files, edit them, and run any class with a `main` (folders with several runnable demos
get a Run button per file). The **✦ Ask AI** button opens a Claude agent scoped to this repo that
can explain the page, quiz you, or edit the files on request.

Run locally with `npm run dev` (starts the docs site and the backend that compiles/runs Java and
streams the agent).
