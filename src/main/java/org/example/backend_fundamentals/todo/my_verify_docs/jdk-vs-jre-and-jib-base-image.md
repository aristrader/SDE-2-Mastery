# JDK vs JRE, Jib, and the Production Container Base Image

## Why this doc exists

While reviewing CVE scan output we changed the Jib base image from
`eclipse-temurin:21-jdk-jammy` to `eclipse-temurin:21-jdk`. Two things
changed:

- We dropped the explicit OS tag (was `-jammy`, now nothing).
- The bare `21-jdk` tag is a floating alias maintained by Eclipse
  Temurin. As of today (May 2026) it resolves to the noble (Ubuntu
  24.04) image; it used to resolve to jammy (Ubuntu 22.04); it'll
  resolve to whatever Ubuntu LTS Temurin promotes next when that
  happens.

We kept the JDK variant (rather than switching to JRE) for cross-repo
consistency — other Vida services also use `*-jdk*` base images. The
JRE variant would have been correct from a pure-runtime perspective
(smaller, fewer packages, fewer CVE-surface findings), but the
consistency win across the fleet outweighs the size savings.

This doc walks through *what* a JDK image vs a JRE image actually
contains, *why* either is sufficient to run our service, and *which*
JDK runs where across the build-and-deploy pipeline. The goal is that
anyone reading this can later argue confidently for any of the
plausible base-image choices.

---

## The two-stage life of a Java application

Every JVM service has two completely separate phases. Engineers often
conflate them, which is the root of most JDK/JRE confusion.

### Phase A — Compile

Source `.java` files become `.class` bytecode files, then get bundled
into a JAR.

```
src/main/java/.../VerifyApiController.java   (text, human-readable)
   |  javac (the Java compiler) reads source, parses, type-checks,
   |  resolves dependencies, emits bytecode
   v
target/classes/.../VerifyApiController.class (binary, JVM-readable)
   |  spring-boot-maven-plugin packages everything
   v
target/myservice-1.0.jar                      (a ZIP file containing all
                                               .class files and dependency
                                               JARs)
```

This phase runs on the build machine — your laptop, Bitbucket Pipelines,
etc. It needs:

- `javac` to compile.
- `jar` (or its modern equivalent inside the JDK) to package.
- Annotation processors (Lombok, Spring Boot's configuration processor)
  that run as plugins inside `javac`.
- These all live in the JDK, not the JRE.

The artifact this phase produces — `myservice-1.0.jar` — is **the only
thing the runtime cares about**. Everything before that point is build
infrastructure.

### Phase B — Run

The container starts in production. The entry point runs:

```
java -javaagent:/dd-agent/dd-java-agent.jar -jar /app/myservice-1.0.jar
```

Step by step:

1. The Linux kernel loads the `java` ELF binary into memory.
2. `java` resolves dynamic libraries (`libjvm.so`, `libc.so.6`,
   `libpthread.so.0`, ...) using the OS's dynamic linker.
3. `java` parses CLI args, sets JVM options (heap, GC), and calls
   `JNI_CreateJavaVM` inside `libjvm.so`.
4. The JVM (HotSpot) initializes: bootstrap class loader, native memory
   regions, GC threads.
5. The JVM opens `myservice-1.0.jar`, finds the `Main-Class` from its
   manifest, starts loading classes.
6. As classes are loaded, the verifier validates each one's bytecode.
   The interpreter starts executing. Hot methods get JIT-compiled
   (C1 → C2).
7. Spring Boot bootstraps, scans the classpath, builds the application
   context, starts Undertow on its port.
8. Service is alive.

**At no point in Phase B does `javac` run, get loaded, or get touched.**
The code was compiled in Phase A. The container is a runtime, not a
workshop.

---

## What's actually in JDK vs JRE

The JDK is a strict superset of the JRE. The JRE is enough to *run*
Java; the JDK is enough to *build* Java.

### JRE contents (Eclipse Temurin's `21-jre-noble`)

```
/opt/java/openjdk/bin/
  java         the launcher
  jcmd         JVM command interface (operator tool)
  jstack       thread dump (operator tool)
  jmap         heap dump (operator tool)
  jstat        JVM stats (operator tool)
  jfr          Java Flight Recorder controller
  jhsdb        HotSpot Serviceability Agent (post-mortem)
  keytool      cert/keystore management
  rmiregistry  RMI registry daemon

/opt/java/openjdk/lib/
  libjvm.so    the HotSpot JVM itself
  libnet.so    networking JNI
  libnio.so    NIO JNI
  libsunec.so  elliptic-curve crypto JNI
  modules      packed standard library (java.base, java.net, ...)
```

### Additional JDK contents (in `21-jdk-jammy`)

Everything in JRE, plus:

```
/opt/java/openjdk/bin/
  javac        compiler                          ← Phase A only
  javadoc      doc generator                     ← Phase A only
  javap        bytecode disassembler             ← rarely used
  jar          JAR creation/extraction           ← Phase A only
  jdb          command-line debugger             ← rarely used
  jdeps        module dependency analyzer        ← Phase A only
  jdeprscan    deprecated-API scanner            ← Phase A only
  jimage       inspects .jimage runtime images   ← rarely used
  jlink        custom runtime image builder      ← Phase A only
  jmod         JMOD module operations            ← Phase A only
  jpackage     native installer builder          ← Phase A only
  jshell       Java REPL                         ← interactive only
  jwebserver   simple HTTP server                ← testing only
  serialver    prints serialVersionUID           ← rarely used

/opt/java/openjdk/include/
  jni.h        ... (C headers for JNI)
  ...

/opt/java/openjdk/lib/
  src.zip      standard library source (~70 MB) ← documentation only
```

The "Phase A only" tools are exactly what's needed to compile + package
Java code. None of them are exercised when you run a precompiled JAR.

The JDK image is roughly **150–250 MB larger** than the JRE image.
Stripping the JDK removes ~30 binaries, the source ZIP, JNI headers,
and the supporting library modules backing the dev tools.

### Important: diagnostic tools are kept in the JRE

Eclipse Temurin's JRE distributions explicitly retain the operator-side
diagnostic tools — `jcmd`, `jstack`, `jmap`, `jstat`, `jfr`, `jhsdb`.

This matters because the legitimate worry about going JRE-only is "what
if I need to attach jstack to a misbehaving pod?" You still can. The
tools needed to inspect a *running* JVM are in the JRE; only the tools
needed to *build* Java are missing.

---

## The four "JDKs" in your daily workflow

This is the source of most confusion. There are **at least four**
distinct Java installations involved in shipping our service, and they
are completely independent of each other.

```
+-------------------------------------------------------------------+
| 1. JDK on your laptop                                             |
|    e.g. /Library/Java/JavaVirtualMachines/temurin-21.jdk          |
|    Purpose: compiles + runs code while you develop.               |
|    Set by:   you (sdkman, brew, manual install)                   |
|    Used by:  IntelliJ, mvn (locally), java/javac in your shell    |
+-------------------------------------------------------------------+
| 2. JDK on the CI build runner                                     |
|    e.g. inside a Bitbucket Pipelines container                    |
|    Purpose: runs `mvn clean install` to produce the fat-jar       |
|    Set by:   bitbucket-pipelines.yml (`image:` directive)         |
|    Used by:  Maven during CI                                      |
+-------------------------------------------------------------------+
| 3. The pom.xml `<java.version>21</java.version>`                  |
|    NOT a JDK installation — just a number.                        |
|    Purpose: tells whichever JDK is doing the compile to produce   |
|             Java-21-compatible bytecode.                          |
|    Effect:  `javac --release 21` is invoked.                      |
+-------------------------------------------------------------------+
| 4. The JVM inside the production container image                  |
|    e.g. /opt/java/openjdk inside eclipse-temurin:21-jre-noble     |
|    Purpose: actually runs our fat-jar in production.              |
|    Set by:   the Jib plugin's `<from><image>` config in pom.xml   |
|    Used by:  the running pod                                      |
+-------------------------------------------------------------------+
```

Crucially: **the JVM that runs our service in production has nothing
to do with the JDK on your laptop or the JDK on the CI runner.** It's
shipped inside the container image we publish to ECR. That's the one
thing Jib's `<from><image>` controls.

### #1 — JDK on your laptop

When you click "Build → Build Project" in IntelliJ:

- IntelliJ uses the JDK set under **Project Structure → Project → SDK**
  to invoke `javac` on changed `.java` files. Bytecode lands in
  `target/classes/`.
- When you click Run, IntelliJ uses `java` from that same SDK.

When you do `mvn clean install` in a terminal:

- Maven follows `JAVA_HOME` (or the symlink Maven resolves Java from).
- It invokes `javac` from that JDK to compile, then runs tests on that
  JVM.
- IntelliJ has a separate "Maven Runner JDK" override under **Build,
  Execution, Deployment → Build Tools → Maven → Runner**, distinct from
  the Project SDK. Most people leave them aligned.

This JDK is a real install on your filesystem. You picked it
(via SDKMAN, Homebrew, or a JetBrains runtime). Nothing in the codebase
tells IntelliJ which JDK to use; that's a per-machine setting.

### #2 — JDK on the CI build runner

`bitbucket-pipelines.yml` declares the image inside which the build
runs (something like `image: maven:3.9-eclipse-temurin-21`). That image
already contains a JDK.

CI runs `mvn clean install`. Maven uses the CI image's `javac` to
compile, runs tests, and produces the fat-jar inside the runner's
workspace.

Then Jib (a Maven plugin that runs as part of the same build) takes
that fat-jar and builds the **production container image** (#4 below).

The CI runner's JDK is for compiling. It is never shipped to
production. After CI's job finishes, that JDK installation is gone —
the runner container is destroyed.

### #3 — `<java.version>21</java.version>` in pom.xml

This is **not** a JDK installation. It's metadata. It tells Maven —
specifically `maven-compiler-plugin` and `spring-boot-maven-plugin` —
two things:

- **Source compatibility**: the .java files use Java 21 syntax
  (records, pattern matching, virtual threads, etc.).
- **Target bytecode level**: produce class files at bytecode major
  version 65 (= Java 21).

When `javac` actually runs (whether from #1 on your laptop or #2 on
CI), it sees `--release 21` derived from this property and refuses to
compile if the JDK doing the work is older than 21.

So this property is a **demand** — "I need at least JDK 21 to build
me." The actual JDK satisfying that demand is whichever one is on the
machine running the build. The pom doesn't bundle a JDK; it just
states the requirement.

### #4 — JVM inside the production container

When Kubernetes starts our pod, it doesn't care what's installed on the
host node. Everything the pod needs — the JVM, system libraries, our
fat-jar, configs — has to be **inside the container image**.

Choosing `eclipse-temurin:21-jre-noble` as the base image means the
final image, when pulled and started by K8s, has Eclipse Temurin's JRE
21 already installed at `/opt/java/openjdk`, ready to run our code.

This JVM is **completely independent** of the JDK on your laptop or
the JDK on the CI runner. Different installation, different filesystem,
different lifecycle. The only thing that matters for this JVM is what
we set in the Jib `<from><image>` config.

---

## Why Jib exists

To get our compiled fat-jar running in Kubernetes, we need to produce
a **container image**. Kubernetes can't run a `.jar` file directly —
it runs container images.

A container image is a tarball with:

- A base operating system layer (Ubuntu noble, in our case).
- A JVM layer (Eclipse Temurin JRE 21).
- Our application layer (the fat-jar and the dd-java-agent).
- Metadata (entry point, env vars, labels).

There are several tools that can produce such an image:

| Tool | How it works |
|---|---|
| `docker build` with a Dockerfile | Classic. You write `FROM eclipse-temurin:21-jre-noble` + `COPY ... + ENTRYPOINT [...]`. Requires a Docker daemon. |
| Buildpacks (Paketo, Heroku) | Auto-detects your project type, picks a base image, packages your jar. No Dockerfile. |
| **Jib** (Google, Maven/Gradle plugin) | Reads your Maven build, takes the compiled fat-jar, layers it on top of the configured base image. **No Dockerfile, no Docker daemon needed** — Jib pushes directly to the registry over HTTPS. |
| ko (Go-specific) | Similar idea for Go projects. |

**Jib's selling points**: no Docker daemon, no Dockerfile, integrated
into the Maven build, optimized layering, native multi-architecture
support (we publish amd64 + arm64).

So Jib's job is **container assembly**, not Java compilation. The JDK
that Jib bakes into the image (via `<from><image>`) is the JVM that
will run our code in production — entirely separate from the JDK on
your laptop or the CI runner.

The Jib block in our pom looks like:

```xml
<plugin>
    <groupId>com.google.cloud.tools</groupId>
    <artifactId>jib-maven-plugin</artifactId>
    <version>3.4.4</version>
    <configuration>
        <from>
            <image>eclipse-temurin:21-jre-noble</image>
            <platforms>
                <platform><architecture>amd64</architecture><os>linux</os></platform>
                <platform><architecture>arm64</architecture><os>linux</os></platform>
            </platforms>
        </from>
        <extraDirectories>
            <paths>
                <path>
                    <from>${user.home}/.m2/repository/com/datadoghq/dd-java-agent/${dd.agent.version}</from>
                    <into>/dd-agent</into>
                    <includes>dd-java-agent-${dd.agent.version}.jar</includes>
                </path>
            </paths>
        </extraDirectories>
        <container>
            <jvmFlags>
                <jvmFlag>-javaagent:/dd-agent/dd-java-agent-${dd.agent.version}.jar</jvmFlag>
            </jvmFlags>
        </container>
    </configuration>
</plugin>
```

`<from><image>` controls #4 — the production JVM.

---

## What our service actually exercises at runtime

Tracing through our code, what does the running JVM call? At a
class-loader / native-call level:

- `java.lang.*` (String, Object, Thread) — JRE.
- `java.util.*` (Collections, concurrent, time) — JRE.
- `java.io.*`, `java.nio.*` (files, sockets) — JRE; calls native
  `libnet.so`, `libnio.so`, which call into `libc`.
- `java.net.*`, `javax.net.*` (URLs, HTTP) — JRE.
- `javax.crypto.*`, `java.security.*` (TLS, signing) — JRE; native
  crypto via `libsunec.so`.
- `org.springframework.*`, `org.hibernate.*`, our own `id.vida.*` —
  bytecode in our fat-jar, executed by the JVM.
- `io.undertow.*`, `io.netty.*` — same, plus some native calls
  (Netty epoll/transport).
- `software.amazon.awssdk.*` — same, talks to AWS over HTTPS.
- `dd-java-agent` — Datadog agent, attaches via JVMTI, instruments
  bytecode at load time.

**None of this touches `javac`, `jar`, `jdb`, `javadoc`, `javap`,
`jlink`, etc.**

Even bytecode-generation libraries — Spring's CGLIB / ByteBuddy
proxies, Hibernate's bytecode enhancer — generate bytecode in memory
using the JVM's own `Unsafe` / `ClassLoader.defineClass` APIs. They
don't shell out to `javac`.

So "no JDK at runtime" isn't a special configuration. It's the normal
case for any modern Spring Boot service.

---

## The OS layer underneath: jammy vs noble

A container image is layered:

```
+---------------------------------------------+
| Application layer                           |
|   /app/myservice-1.0.jar  (your code)       |
|   /dd-agent/dd-java-agent.jar  (datadog)    |
+---------------------------------------------+
| Java layer                                  |
|   /opt/java/openjdk/bin/java                |
|   /opt/java/openjdk/lib/libjvm.so           |
|   /opt/java/openjdk/lib/libnet.so           |
|   /opt/java/openjdk/lib/server/...          |
+---------------------------------------------+
| OS userspace (jammy or noble)               |
|   /usr/bin/curl         <- OS package       |
|   /usr/bin/sed          <- OS package       |
|   /usr/lib/x86_64-linux-gnu/libc.so.6       |
|   /usr/lib/x86_64-linux-gnu/libpthread...   |
|   /usr/lib/x86_64-linux-gnu/libssl.so.3     |
|   /etc/ssl/certs/ca-certificates.crt        |
|   /usr/share/zoneinfo/...                   |
|   ... lots of OS packages ...               |
+---------------------------------------------+
| Linux kernel (provided by host, not image)  |
+---------------------------------------------+
```

### What stays the same across jammy and noble

- Linux kernel — provided by the host, not the image. Both run on the
  same K8s node kernel.
- The JVM — Eclipse Temurin installs into `/opt/java/openjdk`. The JVM
  binary doesn't change between jammy and noble.
- Our application — same fat-jar.

### What changes between jammy and noble

OS-package versions. The CVE scan flagged exactly these:

| Package | jammy version (vulnerable) | noble version (patched) | CVEs cleared |
|---|---|---|---|
| `curl`, `libcurl4` | 7.81.0-1ubuntu1.24 | 8.5.0-2ubuntu10.x | CVE-2026-5545, -6253, -6429, -7168, -4873, -5773, -6276 |
| `libcap2` | 1:2.44-1ubuntu0.22.04.3 | 1:2.66-5ubuntu2 | CVE-2026-4878 |
| `libnghttp2-14` | 1.43.0-1ubuntu0.3 | 1.59.0-1ubuntu0.x | CVE-2026-27135 |
| `sed` | 4.8-1ubuntu2.1 | 4.9-2build1 | CVE-2026-5958 |

Bumping jammy → noble doesn't *patch* these packages; it replaces them
with newer upstream versions that were never vulnerable in the same
way.

Other differences across the OS:

- `glibc` — jammy ships 2.35, noble ships 2.39. Newer glibc is
  backward-compatible with anything compiled against older. The JVM,
  the dd-java-agent, and any native libs we transitively depend on
  continue to work.
- `libssl` (OpenSSL) — jammy ships 3.0.x, noble ships 3.0.13+. Java
  uses its own SunJSSE for TLS, not OS OpenSSL, so this matters only
  if a native library (e.g., Netty's optional native transport) uses
  it. We don't.
- `ca-certificates` — both ship the standard Mozilla CA bundle. TLS
  to AWS, VIDA, KeyCloak keeps working.
- `tzdata` — both ship current timezone data. `LocalDateTime` calls
  keep working.

### Why curl CVEs don't break our app even when curl is on the image

`curl` is an executable at `/usr/bin/curl`. It runs only when something
*invokes* it — a shell script, a `Runtime.exec("curl ...")` from Java,
a human running `kubectl exec ... curl`.

Our service's outgoing HTTP traffic uses:

- `WebClient` (Spring WebFlux + Reactor Netty) — pure Java HTTP/1.1+
  HTTP/2.
- `Feign` clients — pure Java, calls Java HTTP libraries.
- AWS SDK — uses `UrlConnectionHttpClient`, pure Java.

Java doesn't shell out to `curl` for any of this. The vulnerable curl
binary just sits on disk doing nothing. The CVE is theoretical for our
service: an attacker would need to first compromise the container and
then invoke curl to trigger it.

But scanners flag it anyway because they don't analyze reachability.
Their job is "what vulnerable packages are on this image" — they
answer that, regardless of whether the package is exercised. Switching
to noble removes the version with the CVE; we satisfy the scanner
without changing how our app behaves.

---

## Concrete walkthrough: who runs what when

For one full code-change-to-prod-traffic cycle, this is who's running
which Java:

| Step | Whose Java is running? | Why |
|---|---|---|
| You edit `VerifyApiController.java` in IntelliJ | None yet — text editing | — |
| IntelliJ shows a red squiggle for a typo | **#1** — your laptop's JDK, indirectly. IntelliJ uses the project SDK to run its incremental analyzer | Type-checks your code on the fly |
| You hit Run (locally) | **#1** — IntelliJ invokes `java` from that SDK to launch your service locally | Local debugging |
| You commit and push | None | Git is text |
| CI runs `mvn clean install` | **#2** — CI runner's JDK | Compiles, tests, packages |
| CI runs Jib (as part of `mvn install`) | **#2** is what's executing Jib (Jib is itself written in Java). But Jib's *output* — the image — embeds `eclipse-temurin:21-jre-noble` (= **#4**) as the runtime layer | Image assembly |
| Image is pushed to ECR | None — just a tarball upload | — |
| Kubernetes pulls the image, starts the pod | **#4** — Eclipse Temurin JRE 21 from `21-jre-noble` | Production |
| Live traffic hits `/api/v1/verify` | **#4** all the way down | — |

#1 and #2 are used during compile / packaging, then **discarded**.
They never reach production. Production runs only #4.

---

## Why JDK images exist (legitimate use cases)

The JDK image is the right choice when **the running container needs
to compile or generate Java code at runtime**. Examples:

| Use case | Why JDK is needed |
|---|---|
| CI / build runners (Jenkins, GitHub Actions, Bitbucket Pipelines) | Run `mvn clean install`, which runs `javac`. |
| Multi-stage Dockerfile builds | The build stage uses JDK; the runtime stage uses JRE. |
| Servlet containers compiling JSPs at runtime | Tomcat-with-JSPs invokes Jasper, which calls `javac`. |
| Apps executing user-supplied Java/Groovy | Jenkins (Groovy scripts), online IDEs, build platforms. |
| GraalVM native-image / AOT tooling | Building native binaries needs the JDK toolchain. |
| `jlink` / `jpackage` consumers | Custom runtime images, native installers. |

Our service is **none of these**. We're a precompiled Spring Boot
fat-jar. Compilation happens once in CI, never in production.

The original `eclipse-temurin:21-jdk-jammy` choice was the "default
safe-but-bloated" pick that engineers reach for when they're not sure.
Image hygiene is something you discover via security scans. We kept
the JDK variant in the move to noble because other Vida services also
use JDK base images, and consistency across repos is worth more than
the ~150 MB and few extra CVE-surface packages.

---

## Comparison with sibling repos

For reference, two other Vida repos use these Jib base images:

| Repo | Base image | Notes |
|---|---|---|
| Repo A | `eclipse-temurin:21-jdk` | No OS tag — floating alias. Today resolves to noble; was jammy historically; will move with Temurin's chosen default. |
| Repo B | `bellsoft/liberica-openjdk-alpine:21` | BellSoft's Liberica JDK on Alpine Linux. Smaller (~200 MB) but uses musl libc, which can cause native-library issues. |
| Ours | `eclipse-temurin:21-jdk` | Matches Repo A exactly. |

Three orthogonal dimensions are at play across these:

1. **Distribution** — Eclipse Temurin (Adoptium) vs BellSoft Liberica.
   Both are TCK-certified OpenJDK builds. Temurin is the de-facto
   default; Liberica covers more architectures and offers Alpine-musl
   builds.
2. **Variant** — JDK vs JRE. Discussed above. We chose JDK for
   consistency with other repos.
3. **OS base** — Ubuntu (jammy/noble) vs Alpine. Ubuntu uses glibc
   (compatible with everything; ~80 MB OS layer). Alpine uses musl
   (smaller; ~5 MB OS layer; native libs may misbehave).

We standardized on Temurin + JDK + bare `21-jdk` tag, matching Repo A:

- **Temurin**: matches Repo A's distribution choice.
- **JDK**: matches Repo A and Repo B — consistency across the fleet.
- **Bare tag (no OS suffix)**: follows whatever Temurin currently
  promotes as the default Ubuntu LTS. Today that's noble, which
  carries patched curl / libcap2 / libnghttp2-14 / sed. When Temurin
  promotes the next LTS, we'll get it automatically.

### Trade-off: floating tag vs pinned tag

| Style | What it gets you |
|---|---|
| **Bare `21-jdk`** (chosen, matches Repo A) | Reproducible *intent* ("always the current stable Temurin 21 default"), non-reproducible *bytes*. Each build pulls whatever the current alias points at. Catches OS CVE patches as Temurin republishes the alias. Risk: an OS bump can land in a build without explicit review. |
| **Pinned `21-jdk-noble`** (alternative) | Reproducible *bytes* — every build gets the same OS layer. You consciously bump when ready. Safer for strict reproducibility, but requires manual upkeep when Ubuntu LTS rotates. |

We chose the floating tag for cross-repo consistency. If reproducible
builds become a hard requirement (e.g., for compliance or supply-chain
attestation), switching to a pinned tag is a one-line change.

---

## How operators debug a JRE-only container in production

The legitimate worry is "what if I need to attach jstack to a
misbehaving pod?" Three layers of answer:

### Day-to-day observability (95% of debugging)

- Datadog APM via the dd-java-agent — traces, metrics, profiling.
- Application logs (Logstash → Datadog/Elastic).
- JVM metrics via Micrometer to Prometheus / Datadog.
- Kubernetes events, pod restarts, OOM-killer logs.

These cover almost everything. You rarely need to attach a tool to a
live JVM.

### On-demand JVM diagnostics (the remaining 5%)

Eclipse Temurin's JRE keeps `jcmd`, `jstack`, `jmap`, `jstat`, `jfr`
in the image. So:

```
kubectl exec -it pod-name -- jstack 1
kubectl exec -it pod-name -- jcmd 1 GC.heap_dump /tmp/heap.hprof
kubectl exec -it pod-name -- jcmd 1 JFR.start duration=60s filename=/tmp/profile.jfr
```

PID 1 is the Java process inside the container. Everything works
exactly as on a JDK image.

### Auto-on-OOM heap dump

JVM-level flags trigger a dump without any tool attach:

```
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/tmp/heap.hprof
```

Works on JRE images. Set these in the Jib `<jvmFlags>` block.

### What you actually lose with JRE

- `javap` (bytecode disassembly) — useful for reading library bytecode
  you don't have source for. Almost never needed in prod.
- `jdeps` (module dependency analysis) — build-time tool.
- `jlink` (custom runtime building) — build-time tool.
- `javac`, `jar`, `javadoc`, `jdb`, `jshell`, `jpackage` — all
  build-time.

If you absolutely need the full toolchain against a running pod,
Kubernetes supports `kubectl debug` to attach an ephemeral debug
container with whatever image you want — including a full JDK. So the
tools are still reachable, just not pre-installed on every prod pod.

---

## Mental model summary

- **Compiling Java** needs a JDK (because of `javac`). Happens on your
  laptop and in CI.
- **Running compiled Java** needs a JVM (which is the same in both
  JDK and JRE — they bundle it differently, but the JVM itself is
  identical). Happens in your container.
- **Jib** is a Maven plugin that builds container images. It reads
  your fat-jar, slaps it on top of whatever base image you tell it,
  pushes to a registry. Independent of any of the JDKs you use to
  run it; only its `<from><image>` setting affects the production
  container.
- **The change to `21-jdk`** dropped the explicit OS pin (`-jammy`).
  The bare tag is a floating alias maintained by Temurin; today it
  points at the noble (Ubuntu 24.04) image, which carries patched
  curl / libcap2 / libnghttp2-14 / sed. Matches Repo A.
- **The JRE alternative** (`21-jre-noble`) would have been correct
  on technical merits — smaller image, less CVE surface, same JVM —
  but consistency with sibling repos won out.
- **The pinned-OS alternative** (`21-jdk-noble`) would have been
  more reproducible across rebuilds, but the bare-tag style matches
  Repo A and lets us pick up Temurin's OS upgrades automatically.

The JDK on your laptop, the JDK in CI, and the runtime JVM in the
container are independent installations. Jib orchestrates only the
third one.

---

## TL;DR

| Question | Answer |
|---|---|
| Does my IntelliJ break? | No. It uses your laptop's JDK (#1), unaffected. |
| Does CI break? | No. It uses the CI runner's JDK (#2), unaffected. |
| Does the production app behave differently? | No. The JVM is identical; only OS-level packages and the absence of dev tools changed. |
| Do heap dumps still work? | Yes. `jcmd`, `jstack`, `jmap`, `jfr` are kept in Temurin's JRE image. |
| Why JDK and not JRE? | JRE would be technically correct (smaller, less CVE surface). We kept JDK to match sibling Vida repos — consistency wins over a ~150 MB savings. |
| Why bare `21-jdk` and not `21-jdk-noble`? | The bare tag matches Repo A and follows Temurin's default-LTS promotions automatically. Today it points at noble, so we pick up the patched curl / libcap2 / libnghttp2-14 / sed for free. |
| Why noble matters today | Today's `21-jdk` resolves to Ubuntu 24.04 (noble), which has the patched OS packages. Same kernel ABI, backward-compatible glibc — the JVM and our app run unchanged. |
| What about CVE-2026-3260 (Undertow DoS)? | Deferred — the fix is in Undertow 2.4.x, but `undertow-servlet:2.4.x` isn't published yet, and `spring-boot-starter-undertow` requires it. Track Undertow until the servlet module ships. |
