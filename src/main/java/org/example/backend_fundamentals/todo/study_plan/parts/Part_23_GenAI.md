# Part 23 — GenAI & LLMs

> **Phase placement: Consolidation only.** This Part has zero 🔴 rows — nothing here is required for SDE2/SDE3 KYC-backend interviews in the Sprint window. The 🟠 items are senior-fluency-nice-to-have but ~40 hrs is a heavy Sprint cost for a topic adjacent to your stack. Treat this Part as Month 4+ material. If you have an AI/ML-heavy interview in the Sprint window, promote rows 1, 5, 6, 25 (attention basics, prompt engineering, function calling, prompt injection) — that's ~7 hrs and covers the senior-fluency baseline.

> **Sprint allocation:** Deferred to Consolidation (Month 4+) — zero 🔴 rows; no Sprint coverage required. **Budget: 0 hrs in Sprint (deferred).**

## 23 GenAI & LLMs — topic inventory

| # | Topic | Tags | Tier | Time | Done | Partial | Grilling | Visit Again | Notes | Resources |
|---|-------|------|------|------|------|---|---|-------------|-------|-----------|
| 4 | Open vs closed models — Llama, Mistral, Claude, GPT, Gemini | 🟠 💼 🆕 | M | 45 min | [ ] | [ ] | [ ] | [ ] | | |
| 5 | Prompt engineering — system prompts, few-shot, CoT, structured output | 🟠 💼 🆕 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | 💻 Warm-up: write 3 prompts for the same task (zero-shot, few-shot with 2 examples, CoT) — compare outputs (30 min) |
| 6 | Function calling / tool use | 🟠 💼 🆕 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | 💻 Warm-up: Claude / OpenAI function calling — define a tool schema, call API, parse tool response, return result (30 min) |
| 7 | Streaming responses, token-by-token UX | 🟠 💼 🆕 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 8 | Output validation, JSON mode, schema-enforced output | 🟠 💼 🆕 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 9 | Cost & latency — tokens in / out, model selection ladder | 🟠 💼 🆕 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 10 | Caching strategies for LLM calls (prompt caching, semantic caching) | 🟠 💼 🆕 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 12 | Embeddings — what they are, similarity metrics | 🟠 💼 🆕 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 13 | Vector databases — pgvector, Pinecone, Weaviate, Milvus, OpenSearch k-NN | 🟠 💼 🆕 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 14 | Chunking strategies, overlap, hierarchical | 🟠 💼 🆕 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 15 | Hybrid search — BM25 + vector | 🟠 💼 🆕 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 16 | Reranking — Cohere / cross-encoder | 🟠 💼 🆕 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 17 | Failure modes — bad chunks, retrieval misses, hallucinations under context | 🟠 💼 🆕 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 18 | Agent loop — plan, act, observe, reflect | 🟠 💼 🆕 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 19 | ReAct pattern, function calling for tools | 🟠 💼 🆕 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 20 | Multi-step agents — when to use, when not | 🟠 💼 🆕 | M | 1 hr | [ ] | [ ] | [ ] | [ ] | | |
| 21 | MCP (Model Context Protocol) — what it is, how servers / clients work, why it standardizes tool access | 🟠 💼 🆕 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 22 | Building MCP servers (your future leverage) | 🟠 💼 🆕 | D | 2 hrs | [ ] | [ ] | [ ] | [ ] | | 💻 Warm-up: write a minimal MCP server in Python or TypeScript exposing one tool, connect to Claude Desktop, invoke (30 min) |
| 25 | Prompt injection — direct, indirect, defense | 🟠 💼 🔐 🆕 | D | 2 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 26 | Data leakage from prompts | 🟠 💼 🔐 🆕 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |

## Time summary

| Scope | Hours (zero baseline) | Weeks @ 10–12 hrs/wk | Actual time |
|-------|-----------------------|----------------------|-------------|
| 🔴 MUST only (Sprint priority — 3-month plan) | ~0 hrs (no 🔴 — 🟠 is the baseline here) | ~0 wk | |
| 🔴 + 🟠 HIGH (must + high — senior coverage) | ~40.5 hrs | ~3.7 wk | |
| Full Part (all items including 🟡) | ~43 hrs | ~3.9 wk | |

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
7. **Q:** Why does prompt caching save money?
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

- **Function calling end-to-end** (~2.5 hrs row 6) — define schemas, handle multi-turn tool invocations, error paths, parallel tool calls. Most useful practical LLM pattern for backend integration.
- **RAG basics with pgvector** (~3 hrs combined rows 12-14) — embeddings + vector DB + chunking + retrieval. Build a simple KYC-docs Q&A as the worked example.
- **Building an MCP server** (~3 hrs row 22) — future leverage. Spec-aligned MCP server exposing a tool. Connect to Claude Desktop, invoke. Could become useful for KYC workflows (e.g., MCP server that fetches KYC status).
- **Prompt injection + LLM security** (~2.5 hrs row 25) — modern attack surface. Catalog defenses. Apply to any LLM feature you'd add to KYC.

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

> Warm-up exercises (counted in main Time summary above) total ~90 min for Part 23 across 3 in-table warm-ups.

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

**Q. Prompt caching — what does it save?**
A. Cost. Cache the stable prefix (system prompt + few-shot examples) on provider side. On cache hit, charge only for the variable suffix. Huge savings on RAG with long contexts.
