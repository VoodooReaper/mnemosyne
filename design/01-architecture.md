# 01 — Architecture: Two-Layer Long-Term Memory on Spring AI (AC1)

**Status:** v0 design-stage. No compile/run claim.
**Date:** 2026-06-22

## Purpose

Define the long-term memory architecture: how curated file-canon and semantic vector-recall combine into one long-term memory served to a single `ChatClient` through the Spring AI advisor chain, alongside the short-term session layer.

## Design

### The two long-term layers + one short-term layer

The system separates memory by **durability and access pattern**, not by topic:

- **Layer A — Curated file canon (durable, exact, auditable).** Typed Markdown files with YAML frontmatter under a sandboxed `memoriesDir`, indexed by `MEMORY.md`. This is the source of truth for facts the agent must never lose or hallucinate: user identity, standing feedback/rules, project state, reference pointers. Read-mostly, human-reviewable, git-backed. Written through the model's memory tools but governed by deterministic gates (see `02-hardening.md`). Maps 1:1 to Bishop's `memory/*.md` + `MEMORY.md`.
- **Layer B — Semantic recall (broad, fuzzy, scale).** An embedding index over a far larger corpus (past conversations, documents, tickets, knowledge-base entries) in a `VectorStore`. Answers "what do we know that is *related* to this?" where Layer A answers "what is *true and pinned*?" Retrieval by meaning, ranked by similarity, injected as context. This is what flat files cannot do at enterprise scale.
- **Layer S — Short-term session (working memory).** The current conversation's event-sourced history with compaction. Bounded, volatile relative to A/B. Detailed in `05-session-integration-and-roadmap.md`; included here because the advisor chain carries all three.

**Why both A and B (not one or the other).** Layer A gives precision and governance (you can audit, diff, and gate every pinned fact). Layer B gives recall and scale (you cannot hand-curate ten thousand support tickets, but you can embed them). A pinned fact in Layer A overrides a fuzzy hit from Layer B; B never silently rewrites A. This precision-vs-recall split is the core architectural decision.

### The advisor stack on one ChatClient

Spring AI composes memory by chaining **advisors** on a `ChatClient`. Each advisor intercepts the request, can inject context, and is ordered by `.order()` (lower runs earlier). The long-term layers and the session layer are each an advisor:

```
                         ┌─────────────────────────────────────────────┐
   user prompt  ───────► │                 ChatClient                   │
                         │            (advisor chain, by .order)        │
                         └─────────────────────────────────────────────┘
                                              │
        ┌─────────────────────────┬──────────┴───────────┬──────────────────────────┐
        ▼                         ▼                        ▼                          ▼
 ┌──────────────┐        ┌─────────────────┐      ┌──────────────────┐       ┌────────────────┐
 │  order 0     │        │   order 100     │      │   order 200      │       │   order 300    │
 │ Session /    │        │ AutoAutoMemory  │      │ Retrieval-       │       │ ToolSearch     │
 │ ChatMemory   │        │ ToolsAdvisor    │      │ Augmentation     │       │ Advisor        │
 │ advisor      │        │ (Layer A:       │      │ Advisor          │       │ (token lever,  │
 │ (Layer S:    │        │  file canon,    │      │ (Layer B:        │       │  see doc 04)   │
 │  session +   │        │  6 memory tools,│      │  VectorStore     │       │                │
 │  compaction) │        │  sandboxed)     │      │  similarity RAG) │       │                │
 └──────┬───────┘        └────────┬────────┘      └────────┬─────────┘       └───────┬────────┘
        │                         │                        │                         │
        │ recent turns            │ pinned facts via       │ semantically            │ only matched
        │ (compacted)             │ tool calls / system    │ relevant docs           │ tool defs
        │                         │ memory prompt          │ (topK, threshold)       │ expanded
        ▼                         ▼                        ▼                         ▼
                         ┌─────────────────────────────────────────────┐
                         │              assembled prompt                │
                         │   system + memory-tools + retrieved-context  │
                         │        + compacted session + user turn       │
                         └─────────────────────────────────────────────┘
                                              │
                                              ▼
                                       Chat model (backend-agnostic,
                                       see doc 03: Bedrock/Vertex/Anthropic)
```

**Ordering rationale.** Session first (it frames "what are we even talking about"), then file canon (pinned truth the model can read/update via tools), then vector RAG (related context augmenting the answer), then tool-search last (it operates on the tool surface, not the memory content). Orders are spaced by 100 so a future advisor can slot between without renumbering.

### Read and write paths

- **Read (every turn):** Session advisor supplies compacted recent turns. AutoMemory advisor exposes the six memory tools (`MemoryView` / `Create` / `StrReplace` / `Insert` / `Delete` / `Rename`) plus a system prompt teaching the four types, the two-step save, and staleness rules; the model pulls pinned facts on demand. RAG advisor runs a similarity search and prepends the top-k relevant documents.
- **Write (gated):** The model *proposes* a canon write via a memory tool. In the hardened design that proposal lands in **quarantine** (`_inbox/` equivalent), not directly in canon; a deterministic gate promotes it (see `02-hardening.md`). Vector writes (embedding new documents) go through an ETL path (`reader -> splitter -> vectorStore.add`) that is additive and idempotent, not model-trusted.

### Component-to-API map (from research v2)

| Layer | Spring AI component | Builder / entry |
|---|---|---|
| A — file canon | `AutoAutoMemoryToolsAdvisor` (`spring-ai-agent-utils`) | `.memoriesRootDirectory(...)`, `.memorySystemPrompt(...)`, `.memoryConsolidationTrigger(...)`, `.order(...)` |
| B — vector store | `MongoDBAtlasVectorStore` | `.builder(mongoTemplate, embeddingModel)` + `.vectorIndexName`, `.metadataFieldsToFilter`, `.initializeSchema(true)` |
| B — RAG advisor | `RetrievalAugmentationAdvisor` | `.documentRetriever(VectorStoreDocumentRetriever...)`, `.queryTransformers(...)`, `.queryAugmenter(...)` |
| S — session | `MessageChatMemoryAdvisor` / `MessageWindowChatMemory` (+ Session API) | `.order(...)`; compaction triggers/strategies |
| token lever | Tool Search Advisor | `spring.ai.chat.client.tool-search-advisor.enabled=true` |

Concrete wiring of each is in `../reference/`.

## Deterministic vs model-judgment

- **Deterministic:** advisor ordering; the file-canon schema and index; the quarantine->canon promotion path; the vector ETL (chunk/embed/add) is mechanical; metadata filter fields.
- **Model-judgment:** *which* memory tool to call and what to write; semantic-retrieval relevance (embedding similarity is a learned function); query rewriting. The hardening layer's job is to wrap every model-judgment write in a deterministic gate before it becomes canon.

## Confirm-at-work / open items

- Exact `AutoAutoMemoryToolsAdvisor` class/method names are from `spring-ai-agent-utils` docs (community module, 2.0.0-M2+ / SNAPSHOT); treat as fluid and confirm at build time.
- Whether company corpus for Layer B lives in SQL, Mongo, or both (informs whether Mongo Atlas Vector is the single substrate or one of several). Confirm at work.

## Sources

- `research/2026-06-22-spring-ai-api-deep-dive.md` Gaps 1 (AutoMemoryTools), 2 (Mongo Atlas Vector), 3 (RAG advisors).
- `docs.spring.io/spring-ai/reference/2.0-SNAPSHOT/` (vectordbs/mongodb, retrieval-augmented-generation).
- `github.com/spring-ai-community/spring-ai-agent-utils` (AutoMemoryTools docs).
- Bishop production memory architecture (decision #68; `wiki/knowledge-schema.md`).
