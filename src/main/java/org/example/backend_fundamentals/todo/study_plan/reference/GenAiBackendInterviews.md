# GenAI Backend Interviews (2026)

This document consolidates AI topics most commonly appearing in interviews for Backend Engineers (SDE2/SDE3), filtering out ML research/training topics and focusing on AI integration, infrastructure, and engineering.

## 1. LLM Fundamentals & Prompting
*   **Concepts**: LLM vs Traditional Systems, Tokens, Context Window, Temperature, Top-K/Top-P sampling.
*   **Prompt Engineering**: Zero-shot, Few-shot, Chain-of-Thought (CoT), System vs. User prompts.
*   **Structured Outputs**: JSON mode, schema validation (crucial for backend integration).
*   **Hallucinations**: Why they occur and how to reduce them (RAG, Output Verification).

## 2. Tool Calling / Function Calling
*   **Mechanism**: Allowing LLMs to invoke external APIs to bridge knowledge gaps (e.g., querying databases).
*   **Flow**: User -> LLM -> Tool Selection -> API Call -> Result -> LLM -> Final Response.
*   **Production Constraints**: Retries, timeouts, validation, rate limiting, and handling infinite loops.

## 3. Retrieval-Augmented Generation (RAG)
*   **Why RAG vs Fine-Tuning**: RAG adds dynamic knowledge; Fine-tuning changes model behavior.
*   **Pipeline**: Document Ingestion -> Chunking -> Embeddings -> Vector Search -> Context Injection -> LLM.
*   **Chunking Strategies**: Fixed size, sliding window, semantic chunking.
*   **Advanced RAG**: Re-ranking, Hybrid Search (Vector + BM25).

## 4. Embeddings & Vector Databases
*   **Embeddings**: Dense/sparse representations, similarity metrics (Cosine, Dot Product, Euclidean).
*   **Vector Databases**: Pinecone, Milvus, Qdrant, Weaviate, pgvector.
*   **Under the Hood**: Approximate Nearest Neighbor (ANN) search vs Exact Search, HNSW, metadata filtering.

## 5. AI Agents & Workflows
*   **Agent architectures**: Planner-Executor, Reflection, Supervisor, ReAct.
*   **Frameworks**: LangChain (chains, memory), LangGraph (cyclic execution, state management).
*   **Standardization**: Model Context Protocol (MCP) for standardized tool access.

## 6. AI System Design & Infrastructure
*   **Architecture**: Designing a scalable RAG backend, Chatbot, or AI coding assistant.
*   **Performance**: Streaming responses, LLM latency, cost optimization (smaller models, prompt caching, routing, batch processing).
*   **Security**: Prompt Injection (direct/indirect) defenses, Data Leakage, PII redaction.
*   **Observability**: Prompt tracing, token tracking, cost monitoring.
*   **Evaluation**: Offline/Online evals, Golden datasets, LLM-as-a-judge.

## 7. AI-Integrated Interview Formats (Recent Trend)
*   **Interactive AI Bots**: Platforms like HackerRank now integrate AI to ask follow-up questions during DSA rounds.
*   **Handling Follow-ups**: The AI may dynamically change constraints (e.g., "What if space complexity must be O(1)?"). Be prepared to converse with the AI evaluator.

## 8. HR / Behavioral Questions regarding AI
1. How have you used AI in your work? (e.g., Boilerplate generation, Log analysis, PR summaries)
2. Describe an AI workflow you automated.
3. How do you evaluate AI systems in production?
4. How do you optimize AI costs in your applications?
5. How would you choose an LLM for production?
