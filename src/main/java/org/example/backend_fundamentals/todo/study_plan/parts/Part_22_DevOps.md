# Part 22 — DevOps & Infrastructure

> **Sprint allocation:** Week 10 (shared with start of Part 29). **Budget: ~5-6 hrs.**

## 22 DevOps & Infrastructure — topic inventory

| # | Topic | Tags | Tier | Time | Done | Partial | Grilling | Visit Again | Notes | Resources |
|---|-------|------|------|------|------|---|---|-------------|-------|-----------|
| 3b | Container internals — namespaces, cgroups, overlayfs | 🟠 💼 | M | 1 hr | [ ] | [ ] | [ ] | [ ] | | |
| 4 | Docker — networking, volumes, compose | 🔴 💼 | MP | 1.5 hrs | [ ] | [x] | [ ] | [ ] | Notes: compose basics (Kafka example) covered | |
| 5 | Terraform — providers, state, modules, workspaces | 🔴 💼 | D | 2 hrs 30 min | [ ] | [ ] | [ ] | [ ] | | 💻 Warm-up: write Terraform that provisions an S3 bucket with versioning + lifecycle, apply + destroy (30 min) |
| 6 | Daily Linux fluency — ps, top, htop, lsof, netstat / ss, tcpdump, strace | 🔴 💼 | D | 3 hrs | [ ] | [ ] | [ ] | [ ] | | 💻 Warm-up: identify which process holds port 8080 (`lsof -i :8080`), find which files a process has open (`lsof -p <pid>`), watch syscalls (`strace -p <pid>`) (30 min) |

| 8 | Filesystem layout, permissions, ulimits | 🔴 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |

| 12 | Kubernetes — probes (liveness, readiness, startup), resource requests / limits | 🟠 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 13 | Kubernetes — HPA, VPA, cluster autoscaler | 🟠 💼 | MP | 1.5 hrs | [x] | [ ] | [ ] | [ ] | 📖 `deployments/kubernetes_and_containers/index.md` | |
| 14 | Kubernetes — StatefulSet, DaemonSet, Job, CronJob | 🟠 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |

| 17 | GitHub Actions / GitLab CI / Jenkins — pipelines, secrets | 🟠 💼 | MP | 2 hrs | [ ] | [ ] | [ ] | [ ] | | 💻 Warm-up: GitHub Actions workflow that runs mvn test on PR + deploys on push to main (15 min) |

| 19 | Blue-green, canary, feature flags | 🟠 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 20 | Bash scripting fluency | 🟠 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 21 | awk, sed, jq for log mining | 🟠 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |

| 25 | SSH — key-based auth, `~/.ssh/config`, ssh-agent, host key verification, port forwarding (`-L`/`-R`/`-D`), ProxyJump / bastion | 🟠 💼 🔐 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | 💻 Warm-up: generate ed25519 keypair, add to authorized_keys on a remote, set up a `~/.ssh/config` alias with ProxyJump bastion, verify host key is pinned in known_hosts (20 min) |
| 27 | VM vs Container vs Lambda — Architecture, pros/cons, and SDE2 appropriate scope | 🔴 💼 | MP | 1 hr | [x] | [ ] | [ ] | [ ] | 📖 `deployments/kubernetes_and_containers/index.md` | |

## Time summary

| Scope | Hours (zero baseline) | Weeks @ 10–12 hrs/wk | Actual time |
|-------|-----------------------|----------------------|-------------|
| 🔴 MUST only (Sprint priority — 3-month plan) | ~20.08 hrs | ~1.83 wk | |
| 🔴 + 🟠 HIGH (must + high — senior coverage) | ~42.33 hrs | ~3.85 wk | |
| Full Part (all items including 🟡) | ~45.83 hrs | ~4.17 wk | |

## Frequently asked

1. **Q:** Multi-stage Docker build — why?
   - **Why asked:** Senior Docker fluency. Smaller image: build stage has JDK + Maven + sources (huge), runtime stage has only JRE + jar (~150 MB). Better security: no build tools in production image. Faster cold starts. Distroless even smaller (no shell, no package manager).
2. **Q:** K8s readiness vs liveness probe — when does each kick in?
   - **Why asked:** Operational canonical. Liveness: "is the container healthy enough to keep running?" If fails, K8s restarts it. Readiness: "is the container ready to serve traffic?" If fails, K8s removes it from Service endpoints (no restart). Common bug: same endpoint for both → restart loop during heavy GC.
3. **Q:** Terraform state — where does it live, and what's the trap?
   - **Why asked:** Operational depth. State stores resource IDs + metadata. Local file by default — DANGER for teams (race conditions, lost state). Right answer: remote backend (S3 + DynamoDB lock, or Terraform Cloud) so multiple engineers can collaborate safely. State contains secrets — encrypt at rest.
4. **Q:** Blue-green vs canary — when does each fit?
   - **Why asked:** Modern deployment. Blue-green: two identical environments, atomic traffic switch. Good for stateless services, instant rollback. Cost: double the capacity. Canary: gradual traffic shift (1% → 10% → 50% → 100%), with metrics gating. Good for risk reduction on user-impactful changes. Better with feature flags layered on.
5. **Q:** Walk through K8s Pod-to-Pod networking.
   - **Why asked:** K8s internals. Each Pod gets a cluster-unique IP. Service abstracts a stable VIP over a set of Pods (selector → endpoint slice). kube-proxy (or eBPF / Cilium) handles VIP → backend Pod routing on each node. DNS resolution via CoreDNS: `service.namespace.svc.cluster.local`.
6. **Q:** Your CI build flakiness rate is 5%. How do you triage and fix?
   - **Why asked:** Senior engineering practice. (1) Categorize failures: infra-flaky (retry the build), test-flaky (mark + investigate), real bug. (2) Quarantine tests with high flake rate. (3) Address root causes in priority order. (4) Set a flake budget — fail the build if breached. CI flakiness is a tax on the whole team.
7. **Q:** `ps aux` shows your Java service has 12 threads named "ForkJoinPool.commonPool-worker-*". What's likely happening?
   - **Why asked:** Linux + Java intersection. Parallel streams (or other ForkJoin uses) spin up worker threads on the common pool. 12 threads = your machine has ~12 cores. Potential issue: parallel streams on web server threads can starve the common pool. Recommend dedicated executors for app-level parallel work.
8. **Q:** Container image scanning — Trivy vs Snyk vs ECR scanning, which when?
   - **Why asked:** Supply-chain security fluency expected of seniors. **Trivy:** free, OSS, broad feature set (CVE scan + IaC + secrets + SBOM), actively maintained by Aqua, slower on large images but the default for OSS pipelines. **Snyk:** commercial, polished UX, deep CI/CD integrations, dev-friendly remediation advice, license cost scales with repos. **ECR scanning:** free baseline (basic) or enhanced (Inspector-backed) on AWS, runs at-rest on pushed images, basic CVE scan only — no SBOM, no IaC, no secrets detection. **Recommendation:** Trivy as the gate in CI pipelines (fail builds on critical CVEs) + ECR enhanced scanning at rest (catches CVEs that surface *after* the image is pushed, since vuln DBs update continuously). Add Snyk if the org already pays for it or wants polished triage dashboards.

## Trick questions / gotchas

1. **Q:** Your Dockerfile has `RUN apt-get install` followed by `RUN apt-get install` on different lines. Why does this bloat the image?
   - **Gotcha:** Each RUN creates a new layer. Even if you `apt-get autoremove` later, the deleted files remain in earlier layers. Right: combine into one RUN with `&&` chain, or use multi-stage build. Also: use `apt-get install --no-install-recommends` to avoid optional packages.
2. **Q:** Your K8s pod gets killed periodically. Logs show "OOMKilled" but `top` inside the container showed plenty of free memory. What's happening?
   - **Gotcha:** Container memory limits != host memory. K8s kills based on cgroup limits. JVM by default doesn't respect cgroup limits (pre-Java 8u131) — uses host RAM as max heap. Fix: `-XX:+UseContainerSupport` (default in modern JVMs) + `-XX:MaxRAMPercentage=75.0` to leave headroom for native memory.
3. **Q:** Terraform apply succeeds but the resource doesn't appear in AWS console. What's wrong?
   - **Gotcha:** Provider region mismatch. Default region in Terraform vs the region you're viewing in console. Always: `aws_region` explicitly set in provider config; AWS console region matches.
4. **Q:** GitHub Actions secret `&#123;&#123; secrets.AWS_KEY &#125;&#125;` is logged in the action output. Why?
   - **Gotcha:** Likely echoed via `echo` or printed via debug. GitHub auto-redacts secrets in logs, but commands that output the secret via base64 / encoded form bypass the redaction. Never echo secrets. Use OIDC federation for cloud access — no static secrets needed.

## Mastery candidates (top 3–5 from this Part — suggestions, not commitments)

- **Docker multi-stage + distroless image** (~2.5 hrs row 1) — production-grade Dockerfile. Build slim image, document size reduction, demonstrate cold-start improvement.
- **Terraform state + collaboration** (~2.5 hrs row 3) — remote backend with locking. Module structure. Workspaces for env separation. Migration story from local to remote.
- **K8s deployment patterns** (~3 hrs rows 7+8+9) — Pod / Deployment / Service / Ingress + probes + autoscaling. Deploy a real-ish workload, observe HPA scaling.
- **Linux production debugging fluency** (~3 hrs row 4) — lsof, strace, tcpdump, ss, jstack, jmap on a real Java service. Daily senior signal.

## Hands-on exercises (Practice + Advanced)

Warm-up DevOps exercises are listed inline in the topic-table Resources column (counted in main Time summary). Longer exercises below are tracked separately.

### Practice — mid-level (~30-60 min each)

1. **Distroless Spring Boot Docker image** (~60 min) — multi-stage Dockerfile: build stage uses `eclipse-temurin:17-jdk`, runtime uses `gcr.io/distroless/java17-debian12`. Compare image size to fat-jar approach.
2. **Terraform module for VPC** (~60 min) — write a reusable module that takes CIDR + AZs as input, outputs VPC + public/private subnets + route tables. Apply, destroy, reapply.
3. **K8s rolling deployment + readiness probe** (~45 min) — Deployment manifest with readiness probe on `/actuator/health/readiness`. Trigger a rolling update with broken probe — observe no traffic shift. Fix probe, re-deploy.

### Advanced — senior-grade depth (~60+ min each)

4. **GitOps with ArgoCD** (~90 min) — local kind cluster + ArgoCD. Git repo with app manifests. ArgoCD syncs cluster to repo state. Make a change in git, observe auto-sync. Roll back via git revert.
5. **Linux production debugging walkthrough** (~75 min) — simulate a hung Spring Boot app (deadlock or memory leak). Use `jstack` for thread dump, `jmap -dump` for heap dump, `lsof` for open files, `ss -tnp` for network connections. Document the diagnosis sequence.

### Hands-on time summary (Practice + Advanced only)

| Tier | Total time | Weeks @ 10–12 hrs/wk | Actual time |
|------|------------|----------------------|-------------|
| Practice (mid-level) | ~2.75 hrs | ~0.25 wk | |
| Advanced (senior-grade) | ~2.75 hrs | ~0.25 wk | |
| **Combined hands-on (Practice + Advanced)** | **~5.5 hrs** | **~0.5 wk** | |

> Warm-up exercises (counted in main Time summary above) total ~2 hrs for Part 22 across 5 in-table warm-ups.

## Quick recall

**Q. Multi-stage Docker build — what's the win?**
A. Smaller image (build deps left behind), better security (no build tools in runtime), faster cold start. Build stage has JDK + Maven; runtime stage has only JRE + jar.

**Q. K8s liveness vs readiness probe?**
A. Liveness: "alive?" — fail → K8s restarts. Readiness: "ready to serve?" — fail → K8s removes from Service endpoints, no restart.

**Q. Terraform state — where to store?**
A. Remote backend (S3 + DynamoDB lock, or Terraform Cloud). Never local for teams — race conditions, lost state, no collaboration.

**Q. Canary vs blue-green deployment.**
A. Blue-green: atomic switch between two complete environments. Canary: gradual traffic shift (1% → ... → 100%) with metric gating. Canary is risk-aware; blue-green is fast rollback.

**Q. JVM in container — memory gotcha?**
A. Pre-Java 8u131, JVM ignored cgroup limits → set max heap to host RAM → OOMKilled. Modern: `-XX:+UseContainerSupport` (default) + `-XX:MaxRAMPercentage=75.0` for headroom.

**Q. Linux debug commands — what does `lsof -i :8080` show?**
A. Process holding port 8080. Useful for "what's running on this port?" Variants: `lsof -p <pid>` for files held by a process, `lsof | grep deleted` for leaked file descriptors.
