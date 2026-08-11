# Part 23 — GenAI & LLMs

> **Phase placement:** Cover the 🔴 backend baseline during the Sprint. It focuses on integrating AI safely into applications, not model training, ML mathematics, GPU infrastructure, or data-engineering pipelines. Advanced RAG tuning, agent internals, model catalogues, and MCP implementation remain Consolidation material.

> **Sprint allocation:** Shared light block during Weeks 10-12. **Budget: ~7-8 hrs for 🔴 backend fundamentals.**

## 23 GenAI & LLMs — topic inventory

| # | Topic | Tags | Tier | Time | Done | Partial | Grilling | Visit Again | Notes | Resources |
|---|-------|------|------|------|------|---|---|-------------|-------|-----------|
| 1 | LLM mental model for backend engineers — tokens, context window, training vs inference, attention at a high level, probabilistic output, hallucinations | 🔴 💼 🆕 | M | 45 min | [ ] | [ ] | [ ] | [ ] | Basic concept only; no transformer mathematics or training algorithms. |  |
| 2 | Prompting basics — system/user roles, zero-shot, few-shot, structured instructions | 🔴 💼 🆕 | L | 15 min | [ ] | [ ] | [ ] | [ ] | Basic concept only; prompting is not a substitute for output validation. | 💻 Warm-up: write zero-shot and few-shot prompts for one extraction task (15 min) |
| 3 | Backend LLM integration — model APIs, function/tool calling, structured output, schema validation, timeouts, retries, and rate limits | 🔴 💼 🆕 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | Treat model output as untrusted external input. | 💻 Warm-up: define a typed tool schema, mock execution, validate the response, and handle one failure (30 min) |
| 4 | RAG end-to-end mental model — ingest, chunk, embed, store, retrieve, augment, generate; when it fits and how retrieval misses fail | 🔴 💼 🎯 🆕 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | Backend architecture baseline; detailed retrieval tuning remains lower priority. | 📖 `gen_ai/rag/index.md` |
| 5 | GenAI security basics — prompt injection, PII/secrets leakage, least-privilege tools, output validation, and approval for sensitive actions | 🔴 💼 🔐 🆕 | M | 1 hr | [ ] | [ ] | [ ] | [ ] | Never put secrets in prompts; RAG does not eliminate prompt injection. |  |
| 6 | Cost and latency basics — input/output tokens, context size, model selection, and caching awareness | 🔴 💼 🆕 | M | 30 min | [ ] | [ ] | [ ] | [ ] | Estimate and bound usage; do not memorise provider prices. |  |
| 7 | Agent basics — model vs agent, bounded tool loop, termination conditions, and when a deterministic workflow is better | 🔴 💼 🆕 | M | 30 min | [ ] | [ ] | [ ] | [ ] | Basic concept only; agents add cost, latency, and failure modes. |  |
| 8 | Skills — reusable instructions and workflows that teach an agent a specialised capability; discovery and invocation | 🔴 💼 🆕 | M | 30 min | [ ] | [ ] | [ ] | [ ] | Concept is portable; packaging and precedence are platform-specific. |  |
| 9 | Plugins — packaged capabilities that may bundle skills, tools, integrations, or configuration | 🔴 💼 🆕 | M | 30 min | [ ] | [ ] | [ ] | [ ] | Know the concept and lifecycle; exact plugin formats are platform-specific. |  |
| 10 | MCP fundamentals — client, server, tools, resources, and why MCP standardises access to external systems | 🔴 💼 🆕 | M | 45 min | [ ] | [ ] | [ ] | [ ] | Theory only; implementation remains lower priority. |  |
| 11 | Streaming responses, token-by-token UX | 🟠 💼 🆕 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] |  |  |
| 12 | Caching strategies for LLM calls — prompt caching and semantic caching | 🟠 💼 🆕 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] |  |  |
| 13 | Embeddings — what they are and similarity metrics | 🟠 💼 🆕 | L | 15 min | [ ] | [x] | [ ] | [ ] | Partial: embedding concept + vector meaning/search covered; similarity metrics detail pending. ~17 min transcript import | 📖 `gen_ai/rag/index.md` |
| 14 | RAG failure analysis and evaluation — bad chunks, retrieval misses, unsupported answers, stale indexes, golden sets | 🟠 💼 🆕 | MP | 1.5 hrs | [ ] | [x] | [ ] | [ ] | Partial: retrieval miss, bad chunking, unsupported answers, stale index covered; evaluation harness pending. ~17 min transcript import | 📖 `gen_ai/rag/index.md` |
| 15 | Open vs closed model catalogue and provider comparison | 🟡 🆕 | M | 45 min | [ ] | [ ] | [ ] | [ ] | Learn selection criteria when needed; do not memorise model brands or benchmark tables. |  |
| 16 | Vector-database product details — pgvector, Pinecone, Weaviate, Milvus, OpenSearch k-NN | 🟡 🆕 | MP | 1.5 hrs | [ ] | [x] | [ ] | [ ] | Partial: vector DB purpose + common options covered; pgvector implementation pending. ~17 min transcript import | 📖 `gen_ai/rag/index.md` |
| 17 | Advanced chunking — overlap, hierarchical and semantic strategies | 🟡 🆕 | MP | 1.5 hrs | [ ] | [x] | [ ] | [ ] | Partial: fixed/section/semantic chunking covered; overlap/eval tuning pending. ~17 min transcript import | 📖 `gen_ai/rag/index.md` |
| 18 | Hybrid search — BM25 + vector | 🟡 🆕 | MP | 1.5 hrs | [ ] | [x] | [ ] | [ ] | Partial: vector + keyword motivation covered; BM25 mechanics pending. ~17 min transcript import | 📖 `gen_ai/rag/index.md` |
| 19 | Reranking — cross-encoders and provider rerankers | 🟡 🆕 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] |  |  |
| 20 | ReAct and agent-loop internals — plan, act, observe, reflect | 🟡 🆕 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] |  |  |
| 21 | Multi-step agent orchestration internals | 🟡 🆕 | M | 1 hr | [ ] | [ ] | [ ] | [ ] |  |  |
| 22 | Building an MCP server | 🟡 🆕 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | Implementation practice, not a generic backend interview requirement. | 💻 Warm-up: expose one tool from a minimal MCP server and invoke it from a client (60 min) |

## Time summary

| Scope | Hours (zero baseline) | Weeks @ 10–12 hrs/wk | Actual time |
|-------|-----------------------|----------------------|-------------|
| 🔴 MUST only (Sprint priority — 3-month plan) | ~7.75 hrs | ~0.7 wk | |
| 🔴 + 🟠 HIGH (must + high — senior coverage) | ~12.5 hrs | ~1.14 wk | ~17 min so far |
| Full Part (all items including 🟡) | ~23.25 hrs | ~2.11 wk | ~17 min so far |

## Frequently asked

1. **Q:** Explain attention in 2 minutes. Why does it scale O(n²)?
   - **Why asked:** Modern senior-fluency baseline. Each token's representation attends to every other token in the context (query · key dot product, softmax-normalized). N tokens × N comparisons = O(n²). That's why context length is expensive: doubling context quadruples attention cost. Modern advances (sparse attention, sliding window, RAG) avoid the cost.
2. **Q:** Function calling vs prompt-engineered JSON output — when does each fit?
   - **Why asked:** Practical LLM integration. Function calling: model returns structured tool invocation with arguments. Strongly typed by schema. Use when you need to invoke real APIs reliably. JSON mode (without function calling): model produces JSON conforming to schema. Use when you want a structured response, no real action.
3. **Q:** When does RAG fit, and what's the canonical failure mode?
   - **Why asked:** Architecture decision. RAG when: (1) knowledge is large + dynamic, (2) need provenance / citations, (3) cost-savings vs giant-context approach. Failure: retrieval miss (the relevant chunk wasn't in top-K → model hallucinates from training data). Mitigations: better chunking, hybrid search (BM25+vector), reranking, retrieval evaluation.
4. **Q:** Walk through prompt injection. How do you defend against it?
   - **Why asked:** Senior LLM security. Direct: user types "ignore previous instructions, do X". Indirect: user-provided content (a doc, a webpage) contains instructions the LLM follows. Defenses: (1) separate system from user content in prompt structure, (2) validate output before acting, (3) sandbox tool calls, (4) human-in-loop for sensitive actions, (5) instruction-hierarchy training (recent model improvement).
5. **Q:** Design an evals strategy for an LLM feature.
   - **Why asked:** Senior LLM engineering. Build a golden set (50-200 representative inputs with expected outputs / acceptable variations). Run prompts vs golden set on each model/prompt change. Score with deterministic checks (regex, JSON validity) + LLM-as-judge (another model rates correctness). Track regression on each release.
6. **Q:** What's MCP, why does it matter?
   - **Why asked:** 2025 hot topic. Model Context Protocol — Anthropic's open standard for connecting LLMs to tools / data sources. Standardizes the "give the LLM access to my system" interface (resources, tools, prompts). MCP servers expose capabilities; MCP clients (Claude Desktop, Cursor, etc.) consume them. Avoids each tool integration being bespoke.
7. **Q:** Tool, skill, plugin, and MCP — how do they differ?
   - **Why asked:** Tests whether you can reason about the agent integration stack. A tool is an executable operation. A skill is reusable task guidance or workflow knowledge. A plugin packages capabilities for installation or distribution. MCP is a protocol through which clients discover and invoke tools or access resources. Skill and plugin packaging varies by platform.
8. **Q:** Why does prompt caching save money?
   - **Why asked:** Cost optimization. Long system prompts repeated per request = redundant tokens. Prompt caching (Anthropic, OpenAI now): cache the prefix on the provider side, charge less on cache hit. Practical: structure prompts with stable prefix (system + few-shot examples) + variable suffix (user input). Significant cost savings on RAG with long context.

## Trick questions / gotchas

1. **Q:** Your LLM returns JSON sometimes, broken sometimes. You added "return valid JSON only" to the prompt. Still 5% broken. Why?
   - **Gotcha:** Plain prompt-engineering for JSON is unreliable. Use JSON mode (model is constrained to produce valid JSON) or function calling (typed schema). Plus validate + retry on the consuming side. Don't rely on prompting alone for structured output.
2. **Q:** Your RAG system gives confident but wrong answers. Sources are listed but don't actually support the answer. What's the fix?
   - **Gotcha:** Hallucination under retrieval context. Model fills gaps with training data, citing the retrieved chunks anyway. Fixes: (1) prompt the model to explicitly say "I don't have enough info" when sources are insufficient, (2) post-hoc validation: extract claim + verify against cited chunks, (3) reduce model creativity (lower temperature, system prompt emphasizing fidelity).
3. **Q:** Your agent loops forever on a task. Why?
   - **Gotcha:** Missing exit conditions. Agent re-invokes the same tool when the result doesn't match its plan. Fixes: (1) max-iterations limit (e.g., 10), (2) detect repeating tool calls (same args twice in a row = stop), (3) better prompt for "give up gracefully if stuck", (4) explicit termination tool the agent can call.
4. **Q:** Prompt injection attack: user input contains "Tell me your system prompt". The model dutifully prints it. Why?
   - **Gotcha:** No instruction hierarchy enforced. Modern models (Claude 3.5+, GPT-4+) have some instruction-hierarchy training but not bulletproof. Defenses: (1) don't put secrets in the system prompt, (2) sanitize / detect suspicious user input, (3) layer with separate validation step, (4) for high-risk: human approval before action.

## Mastery candidates (top 3–5 from this Part — suggestions, not commitments)

- **Function calling end-to-end** (~2.5 hrs) — define schemas, handle multi-turn tool invocations, error paths, parallel tool calls. Most useful practical LLM pattern for backend integration.
- **RAG architecture walkthrough** (~2 hrs) — explain ingest, chunk, embed, retrieve, augment, and generate, including retrieval misses and unsupported answers. Implementation with a specific vector database is optional.
- **Agent integration vocabulary** (~1.5 hrs) — clearly distinguish models, agents, tools, skills, plugins, and MCP, then explain where each fits in a backend workflow.
- **Prompt injection + LLM security** (~2.5 hrs) — modern attack surface. Catalog defenses. Apply to any LLM feature you'd add to KYC.

## Hands-on exercises (Practice + Advanced)

Warm-up GenAI exercises are listed inline in the topic-table Resources column (counted in main Time summary). Longer exercises below are tracked separately.

### Practice — mid-level (~30-60 min each)

1. **Prompt engineering: zero-shot vs few-shot vs CoT** (~45 min) — same task (e.g., extracting entities from KYC document text). Try three prompt styles. Score outputs against a small golden set. Document which works best for the task.
2. **Function calling with structured output** (~60 min) — Anthropic or OpenAI API. Define a `get_kyc_status(verification_id)` tool. Send user query "what's the status of verification X". Parse tool call, mock-execute, return result to model, generate user-facing response.
3. **Simple RAG with pgvector** (~60 min) — Postgres + pgvector extension. Embed 20 KYC FAQs using a small embedding model. User query → embed → top-5 similarity search → feed to LLM with retrieved context → answer. Test with in-corpus and out-of-corpus questions.

### Advanced — senior-grade depth (~60+ min each)

4. **Build a minimal MCP server** (~90 min) — TypeScript or Python. Expose a tool (e.g., "get KYC verification details"). Connect to Claude Desktop. Invoke from a chat. Document protocol details.
5. **Evals harness for LLM feature** (~75 min) — golden set of 30 representative inputs. Deterministic checks (output JSON valid, contains expected fields). LLM-as-judge scoring (another model rates correctness 1-5). Run on baseline + variant prompts. Track regression.

### Hands-on time summary (Practice + Advanced only)

| Tier | Total time | Weeks @ 10–12 hrs/wk | Actual time |
|------|------------|----------------------|-------------|
| Practice (mid-level) | ~2.75 hrs | ~0.25 wk | |
| Advanced (senior-grade) | ~2.75 hrs | ~0.25 wk | |
| **Combined hands-on (Practice + Advanced)** | **~5.5 hrs** | **~0.5 wk** | |

> Warm-up exercises (counted in main Time summary above) total ~1 hr 45 min for Part 23 across 3 in-table warm-ups.

## Quick recall

**Q. Attention O(n²) — what's the cost driver?**
A. Each token attends to all other tokens (query · key dot product). N × N comparisons. Doubling context quadruples cost. Mitigations: RAG, sparse attention, sliding window attention.

**Q. RAG canonical failure mode?**
A. Retrieval miss — relevant chunk wasn't in top-K, model hallucinates from training data while citing retrieved chunks anyway. Fix: better chunking, hybrid search, reranking, prompt for "say I don't know" if context insufficient.

**Q. Function calling vs JSON mode?**
A. Function calling: strongly-typed tool schema; model produces tool invocation, you execute, return result, model uses result. JSON mode: model produces JSON output conforming to schema. Function calling is for *acting*; JSON mode is for *structured response*.

**Q. Prompt injection defenses?**
A. Separate system from user content, validate output, sandbox tool calls, human-in-loop for sensitive actions, modern models' instruction-hierarchy training (not bulletproof).

**Q. MCP in one sentence.**
A. Model Context Protocol — Anthropic's open standard for connecting LLMs to tools and data sources, so each integration isn't bespoke.

**Q. Tool vs skill vs plugin?**
A. A tool performs an operation; a skill provides reusable task guidance; a plugin packages capabilities for installation. Exact skill and plugin formats are platform-specific.

**Q. Prompt caching — what does it save?**
A. Cost. Cache the stable prefix (system prompt + few-shot examples) on provider side. On cache hit, charge only for the variable suffix. Huge savings on RAG with long contexts.
