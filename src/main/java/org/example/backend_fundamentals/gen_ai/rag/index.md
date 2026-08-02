---
order: 10
---

# RAG

RAG means **Retrieval-Augmented Generation**. The model does not answer only from training memory. The system first retrieves relevant application/company data, adds it to the prompt, then asks the LLM to answer from that context.

Use RAG when the answer depends on data that is private, large, or changes often: internal docs, KYC policies, support tickets, product catalogues, reports, or customer-specific state.

## Why RAG

| Problem | How RAG helps |
| --- | --- |
| hallucination | answer is grounded in retrieved documents |
| stale model knowledge | retrieve current data at request time |
| private/company data | keep data outside model training; retrieve only relevant chunks |
| fine-tuning cost | add knowledge through retrieval instead of retraining |

Example: a generic model cannot know why your booked flight is delayed. A RAG support bot can retrieve your booking, flight status, and airline disruption note, then answer specifically.

## Ingestion pipeline

Ingestion prepares the searchable knowledge base.

```text
documents / PDFs / pages / DB rows
    ↓
extract text
    ↓
split into chunks
    ↓
create embeddings
    ↓
store chunks + embeddings in vector DB
```

**Chunking** matters because the model and vector DB work better with smaller pieces than entire documents.

Common chunking options:

| Strategy | Meaning | Trade-off |
| --- | --- | --- |
| fixed-size | split every N tokens | simple, can break context mid-sentence |
| section/paragraph | split by headings or paragraphs | better context, needs cleaner parsing |
| semantic | split when topic meaning changes | higher quality, more complexity/cost |

## Embeddings and vector search

An embedding is a vector of numbers representing the meaning of text.

```text
"heart attack symptoms"  →  [0.12, -0.44, 0.91, ...]
```

Vector search finds chunks with similar meaning, not only exact keyword matches. That is why a query like "heart attack symptoms" can retrieve a chunk about "cardiac arrest warning signs" even if the exact words differ.

A vector database stores embeddings and supports similarity search. Common choices include pgvector, Pinecone, Weaviate, Milvus, OpenSearch k-NN, FAISS, or Chroma.

## Retrieval pipeline

Retrieval happens when the user asks a question.

```text
user question
    ↓
embed the question
    ↓
retrieve top matching chunks from vector DB
    ↓
add chunks as context to the prompt
    ↓
LLM generates answer
```

That is the name:

- **Retrieval**: fetch relevant chunks
- **Augmented**: add those chunks to the prompt
- **Generation**: LLM writes the final answer

## Search variants

| Variant | Use |
| --- | --- |
| standard RAG | clean docs, FAQs, simple support bot |
| hybrid search | combine vector search + keyword/BM25 for IDs, names, exact terms |
| RAG with memory | preserve conversation history for follow-up questions |
| graph RAG | preserve relationships between entities; useful for fraud/legal/research style data |
| agentic RAG | multi-step retrieval/tool use for questions that need decomposition |
| multimodal RAG | retrieve from text + images/audio/video |

For backend interviews, standard RAG and hybrid search are the most practical.

## Failure modes

| Failure | What happens | Fix |
| --- | --- | --- |
| bad chunking | relevant facts split or buried | chunk by section/paragraph, tune chunk size |
| retrieval miss | relevant chunk not in top-K | hybrid search, better embeddings, retrieval evals |
| unsupported answer | model fills gaps from training data | require "not enough context", cite checked sources |
| stale index | source docs changed but vectors did not | re-index on document change |
| duplicate effects | retry repeats external action | idempotency keys for tool/action calls |

## Quick recall

**Q. What is RAG?**  
A. Retrieve relevant data, add it to the prompt, then generate an answer grounded in that context.

**Q. Why not just fine-tune?**  
A. RAG is cheaper and works with changing/private data. Fine-tuning changes model behavior; RAG supplies knowledge at request time.

**Q. What is chunking?**  
A. Splitting documents into searchable pieces before embedding them.

**Q. What is an embedding?**  
A. A numeric vector representing text meaning, used for similarity search.

**Q. Vector search vs keyword search?**  
A. Vector search matches meaning; keyword search matches exact terms. Hybrid search uses both.

**Q. Common RAG failure mode?**  
A. Retrieval miss: the right chunk is not retrieved, so the model answers from incomplete context.
