# Research Brief v2: Spring AI API Deep-Dive (round-out)

**Date:** 2026-06-22
**Phase:** CRPE-R Research (v2 — closes the 6 gaps from v1)
**Method:** 3 parallel research-scouts (Sonnet), primary-sourced via context7 + docs.spring.io + Anthropic/AWS/GCP official docs. All public framework/cloud docs; no company data.

> Sourcing caveat carried from the scouts: several spring.io **blog** URLs 404'd on re-fetch; the authoritative content came from **context7 + docs.spring.io reference docs + the spring-ai-community GitHub repo**. Treat community-module APIs (2.0.0-M2+ / 2.0-SNAPSHOT) as fluid; confirm at build time.

## Gap 1 — AutoMemoryTools full API (the deliverable's core)

The convergence with Bishop is even tighter than v1 suggested:
- **Six memory tools**, mirroring the Anthropic Claude Code memory-tool spec: `MemoryView`, `MemoryCreate`, `MemoryStrReplace`, `MemoryInsert`, `MemoryDelete`, `MemoryRename`. All sandboxed to a `memoriesDir` root; path traversal + absolute-path injection blocked.
- **Four typed frontmatter categories: `user`, `feedback`, `project`, `reference`.** These are *exactly* Bishop's four memory types. Auto-maintained `MEMORY.md` index. Two-step save.
- **Advisor:** `AutoAutoMemoryToolsAdvisor` (yes, the class name doubles "Auto" in the codebase). Builder: `.memoriesRootDirectory(...)` (required), `.order(...)`, `.memorySystemPrompt(resource)`, `.memoryConsolidationTrigger(BiPredicate<ChatClientRequest, Instant>)` (default `(req,instant)->false`).
- **System prompt:** bundled at `classpath:/prompt/AUTO_MEMORY_TOOLS_SYSTEM_PROMPT.md`, placeholder `{MEMORIES_ROOT_DIERCTORY}` (typo is in the actual source). It distinguishes long-term memory from conversation history, teaches the two-step save, the four types, and staleness rules.
- **Combine layers** on one ChatClient: `AutoAutoMemoryToolsAdvisor` (long-term) + `MessageChatMemoryAdvisor`/`MessageWindowChatMemory` (short-term/session).
- Source: `github.com/spring-ai-community/spring-ai-agent-utils` docs (`docs/tools/AutoMemoryTools.md`, `AutoMemoryToolsAdvisor.md`). The 2026/04/07 blog URL 404'd; repo is the source of truth.

**Implication:** Bishop's memory system ports almost 1:1 onto AutoMemoryTools. Mode A (sandboxed, traversal guard) is the natural fit for the company data boundary.

## Gap 2 — MongoDB Atlas Vector Search (the semantic layer)

- Starter: `org.springframework.ai:spring-ai-starter-vector-store-mongodb-atlas` (2.0-SNAPSHOT).
- Bean: `MongoDBAtlasVectorStore.builder(mongoTemplate, embeddingModel)` with `.collectionName`, `.vectorIndexName`, `.pathName`, `.numCandidates`, `.metadataFieldsToFilter(...)`, `.initializeSchema(true)` (auto-creates the Atlas vector index).
- API: `vectorStore.add(List<Document>)`, `vectorStore.similaritySearch(SearchRequest.builder().query(...).topK(k).similarityThreshold(t).filterExpression("author in ['x'] && type == 'blog'").build())`. Portable filter DSL translates to Atlas-native; filter fields must be declared in `metadataFieldsToFilter`.
- Source: `docs.spring.io/spring-ai/reference/2.0-SNAPSHOT/api/vectordbs/mongodb.html`.

**Implication:** Mongo Atlas Vector is a clean in-stack substrate for the semantic-recall layer.

## Gap 3 — RAG advisors (retrieval wiring)

- `QuestionAnswerAdvisor.builder(vectorStore)` — naive RAG, one line.
- `RetrievalAugmentationAdvisor.builder()` — modular: `.documentRetriever(VectorStoreDocumentRetriever.builder().vectorStore(vs).similarityThreshold(0.5).build())`, optional `.queryTransformers(RewriteQueryTransformer...)`, `.queryAugmenter(ContextualQueryAugmenter.builder().allowEmptyContext(true).build())`.
- Modular `org.springframework.ai.rag` pieces: `DocumentReader`, `TokenTextSplitter` (chunking), `VectorStore` as `DocumentWriter`, `VectorStoreDocumentRetriever`, `DocumentPostProcessor` (reranking slot — no confirmed built-in reranker). ETL shorthand: `vectorStore.accept(splitter.apply(reader.get()))`.
- Advisors inject retrieved context into the prompt via the ChatClient advisor chain; ordering via `.order()`.
- Source: `docs.spring.io/spring-ai/reference/2.0-SNAPSHOT/api/retrieval-augmented-generation.html`.

## Gap 4 — Dynamic Tool Discovery (the token lever)

- **Tool Search Tool** pattern: only `toolSearchTool` is sent at conversation start. The LLM calls it with a natural-language query; a vector index (`ToolIndex`, Lucene default) over tool descriptions does similarity search; a **Recursive Advisor** expands the matched tool definitions into context for the next request.
- Verified savings (28 tools): Gemini 60%, OpenAI 34%, **Anthropic 64%** (6,273 vs 17,342 tokens — Anthropic's schema is most verbose, so biggest reduction).
- Starter: `spring-ai-starter-tool-search-advisor`; enable `spring.ai.chat.client.tool-search-advisor.enabled=true`.
- Source: `docs.spring.io/spring-ai/reference/guides/dynamic-tool-search.html`.

**Implication:** This is a direct answer to the enterprise token-budget mandate. With many department tools, tool-search keeps per-call token cost down materially — biggest win on Anthropic models.

## Gap 5 — A2A vs in-process subagents (multi-department orchestration)

- **A2A** (Google Agent2Agent): agent card at `/.well-known/agent.json`, JSON-RPC/HTTP, `tasks/send` + `tasks/sendSubscribe` (SSE) + `tasks/get`. Spring AI exposes server + client abstractions (exact class names Unverified). Use when agents are **separate processes / different teams / heterogeneous stacks / independently deployed**.
- **In-process Task-tool subagents (Part 4):** same JVM, one team, lower latency, tighter type safety. Use when orchestrator + subagents are co-deployed.
- Source: spring.io Part 5 (indexed; some detail Unverified due to retrieval limits) + A2A spec.

**Implication:** Start department specialists as **in-process Task-tool subagents**; graduate to **A2A** only when a department's agent becomes a separately-deployed service owned by another team.

## Gap 6 — Sanctioned backend routing (the data-boundary answer)

| Rail | Compute in company tenancy? | Notes |
|---|---|---|
| Claude Code + **Bedrock** (`CLAUDE_CODE_USE_BEDROCK=1`) | Yes (AWS acct+region) | Invoke API; `us.anthropic.claude-*` model IDs; IAM `bedrock:InvokeModel*`; PrivateLink available |
| Claude Code + **Vertex** (`CLAUDE_CODE_USE_VERTEX=1`) | Yes (GCP project+region) | `ANTHROPIC_VERTEX_PROJECT_ID`, `CLOUD_ML_REGION`, `roles/aiplatform.user`, Model Garden access; FedRAMP High boundary |
| **Anthropic Enterprise** direct (`api.anthropic.com`) | No (Anthropic US infra) | Governed by API DPA; zero-data-retention is a *negotiated* enterprise option, not automatic |
| Spring AI **Anthropic** (`spring-ai-starter-model-anthropic`) | No | same as direct API |
| Spring AI **Bedrock** (`spring-ai-starter-model-bedrock-converse`) | Yes | **Converse** API (note: Claude Code uses Invoke; Spring AI uses Converse — different IAM) |
| Spring AI **Vertex** (`spring-ai-starter-model-vertex-ai-gemini`) | Yes | **Gemini-native**; routing *Claude* through Spring AI on Vertex is not native (needs custom base-url / Anthropic SDK) — Unverified |

**Implication:** If company policy requires inference inside company-owned cloud, the **Bedrock or Vertex** rails are the answer for both Claude Code and Spring AI. The Anthropic Enterprise seat satisfies a "governed channel" (DPA) but not "compute in our tenancy." Which one applies is a **policy question to confirm at work** (verify ZDR terms / tenancy requirement). Verify Anthropic Enterprise ZDR and GCP Vertex Claude CDPA terms at agreement time (both were JS-rendered / 404'd for the scouts).

## Design implications for gate-1 (now answerable)

1. **Memory = three advisors on one ChatClient:** `AutoAutoMemoryToolsAdvisor` (curated file canon, Mode A sandboxed) + `MongoDBAtlasVectorStore` behind `RetrievalAugmentationAdvisor` (semantic recall) + `MessageChatMemoryAdvisor`/Session API (short-term + compaction). This IS the two-layer design, concretely.
2. **Bishop hardening to add on top:** quarantine-before-canon (`_inbox` equivalent), deterministic freshness/integrity gates (not the model-trusted `memoryConsolidationTrigger` alone), promotion gating. This is the contractor-beating wedge.
3. **Token budget:** adopt the Tool Search Tool + compaction triggers from day one.
4. **Departments:** in-process Task-tool subagents first; A2A when they split into separate services.
5. **Backend:** Bedrock-Converse or Vertex for tenancy; confirm the policy requirement at work.

## Still open (confirm at work, not researchable here)

- The exact company policy: does inference have to stay in-tenant (Bedrock/Vertex), or is the Anthropic Enterprise DPA channel sufficient?
- Whether the Salesman App / company data lives in SQL, Mongo, or both (informs the vector substrate choice).
