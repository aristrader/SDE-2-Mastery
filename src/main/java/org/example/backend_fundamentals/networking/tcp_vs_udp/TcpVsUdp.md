# TCP vs UDP — Transport Protocol Choice (and the OSI Model in Practice)

## How it works

### OSI model: what actually matters for SDE2

You are generally not expected to recite all OSI layers in an interview. What interviewers care about is whether you understand the practical networking concepts behind backend systems:

- How a request travels from client to server
- How HTTP works
- How services communicate
- How load balancers route traffic
- How API gateways fit into request flow
- How DNS resolves names to IPs (see `networking/dns/DnsResolution.md`)

The OSI model is primarily a mental framework for understanding where different technologies operate — the practical application matters more than memorizing layers.

### TCP

TCP prioritizes **reliability**:

- Connection-oriented
- Guarantees delivery
- Guarantees ordering
- Retransmits lost packets
- Performs congestion control

Typical examples: API calls, database connections, most backend service-to-service communication.

If packet #5 is lost: TCP detects it, retransmits it, and delivers packets in correct order.

### UDP

UDP prioritizes **speed and low latency**:

- Connectionless
- No delivery guarantee
- No ordering guarantee
- No automatic retransmission
- Lower overhead

Typical examples: DNS queries, voice calls (VoIP), real-time gaming traffic, live streaming.

If packet #5 is lost: it is simply lost, and communication continues. **The system decides whether loss matters.**

### Why DNS uses UDP

DNS is usually a simple request-response: "What is the IP for example.com?" → "Here's the IP."

Using TCP would require connection establishment → request → response → connection teardown. UDP avoids this overhead. If a DNS request is lost, the client simply retries. For this use case, speed is more valuable than guaranteed delivery.

### Why voice calls use UDP

Voice is real-time. Suppose one packet containing a spoken word is lost:

- **TCP approach:** detect loss → retransmit → wait for the missing packet → continue playback. This introduces delay.
- **UDP approach:** skip the missing packet, keep playing.

Users prefer tiny audio glitches over noticeable lag and pauses — therefore VoIP systems favor UDP.

### TCP vs UDP in online games

Games do not treat all data equally — different categories of information have different requirements.

**Movement updates** (player position, direction, rotation, camera) are highly time-sensitive. If one update is lost, a newer update arrives immediately afterward, so UDP is ideal:

```text
Sent:     Position A, Position B, Position C, Position D
B lost:   Position A, Position C, Position D   → the game still works
```

**Critical game events** (shooting, hit registration, inventory changes, match state) would create incorrect gameplay if lost. Games therefore add reliability mechanisms on top:

- Application-level acknowledgements
- Retries
- Server-side confirmation logic

The key idea: **UDP itself remains unreliable, but the game adds reliability where needed.**

### Multiple communication channels in games

A game session is not necessarily a single communication pipe. A common pattern: one channel for low-latency updates, another for reliable operations. These may use different ports, different protocols, and different reliability mechanisms — the networking layer decides which data goes through which channel.

### YouTube Video Streaming vs Video Calls

A common misconception is that video streaming (like YouTube) must use UDP because it's video and "losing some packets is acceptable."

**YouTube Video Streaming:** Mostly **TCP**. Video content is delivered in chunks over HTTPS (which uses TCP). Smooth playback is achieved through **buffering** (download ahead, store locally, play later), which absorbs any delays caused by TCP retransmissions.

**Video Calls (Zoom, Discord Voice):** Usually **UDP**. These prioritize **low latency** over perfect reliability. A tiny audio glitch from a lost packet is preferable to waiting for a retransmission, which would cause noticeable lag in a live conversation.

## Gotchas / Trick questions

1. **"At SDE2 do I need to memorize the OSI model?"** No — interviewers care far more about practical concepts: TCP vs UDP, HTTP, DNS, load balancers, service communication. OSI is mainly a conceptual framework.
2. **"Games use UDP, so what happens if my bullet packet is lost?"** Misconception: shots should randomly disappear. Correction: games add reliability logic (acknowledgements, retries, server-side validation) for critical actions — the application layer compensates for UDP's unreliability.
3. **"If a game uses UDP, does everything use UDP?"** No — different traffic categories may use different paths: movement → UDP; critical events → reliability mechanisms or separate channels.
4. **"Is there only one connection during a game session?"** No — login, movement, shooting, inventory commonly use multiple channels and potentially multiple ports.
5. **"Why not use TCP for DNS — isn't reliability always better?"** DNS lookups are short request-response operations; TCP's overhead is unnecessary. UDP resolves faster, and failed requests can simply be retried.
6. **"Why not use TCP for voice calls — shouldn't lost packets be retransmitted?"** For real-time communication, latency is worse than minor packet loss. Users prefer slight audio glitches over conversation delays.

## Performance characteristics

| Topic | TCP | UDP |
|-------|-----|-----|
| Connection setup | Required | Not required |
| Delivery guarantee | Yes | No |
| Ordering guarantee | Yes | No |
| Retransmission | Automatic | None |
| Latency | Higher | Lower |
| Typical use | APIs, DBs | DNS, VoIP, gaming |

## Good to know

### Gaming traffic is application-aware

A common mistake is thinking TCP and UDP are competing choices for the entire application. In practice, systems classify traffic — latency-sensitive vs reliability-sensitive — and handle each category differently. The lesson: **network protocol choice is driven by business requirements, not protocol popularity.**

## Quick recall

**Q. Do SDE2 interviews expect full OSI-layer memorization?**
A. No. Practical understanding of networking concepts matters more than layer memorization.

**Q. Why is TCP used for APIs and databases?**
A. Because it guarantees delivery, ordering, and reliability.

**Q. Why is UDP used for DNS?**
A. DNS is a small request-response operation where low overhead is more valuable than guaranteed delivery.

**Q. Why is UDP used for voice calls?**
A. A small amount of packet loss is preferable to retransmission-induced latency.

**Q. Why can games use UDP for movement updates?**
A. New position updates quickly replace old ones, making occasional packet loss acceptable.

**Q. How do games handle important actions like shooting?**
A. They add reliability mechanisms such as acknowledgements, retries, and server-side confirmation logic on top of UDP.
