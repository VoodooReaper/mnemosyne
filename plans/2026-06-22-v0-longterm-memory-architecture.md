# Gate-1 Plan: Mnemosyne v0 — Long-Term Memory Architecture for Spring AI

**Status:** APPROVED 2026-06-22 (Ryan, gate-1 via AskUserQuestion: "Approve, build tonight"). **Build venue (Ryan's call):** fresh session after `/clear` (the #115 path), NOT in-session. The fresh session reads this plan + the handoff and executes the 7 ACs as its first act, apart-style, then runs an adversary pass; gate-2 = Ryan's morning review.
**Date:** 2026-06-22
**Lane:** infrastructure (free; NP is creative+paused, no infra build in flight)
**Northstar:** #112 "Bishop = complete partner who finishes things." Here, finishing = making Ryan exceptional at the job that funds the freedom-to-build, by porting the proven Bishop memory architecture into his Winsupply work and winning the CAIO mandate.

## Source rows
- Concept lock 2026-06-22 (this session); `project_mnemosyne` memory pointer.
- Research v1 (`research/2026-06-22-spring-ai-agentic-patterns-brief.md`) + v2 (`research/2026-06-22-spring-ai-api-deep-dive.md`).

## Pre-grill facts (from research v1+v2, primary-sourced)
- Spring AI `AutoMemoryTools` = curated file-canon memory: 6 tools (MemoryView/Create/StrReplace/Insert/Delete/Rename), 4 typed categories (user/feedback/project/reference, identical to Bishop), `MEMORY.md` index, two-step save, sandboxed traversal guard. `AutoAutoMemoryToolsAdvisor` builder with `memoryConsolidationTrigger` BiPredicate.
- Semantic layer: `MongoDBAtlasVectorStore` (in-stack) behind `RetrievalAugmentationAdvisor` / `QuestionAnswerAdvisor`; modular RAG (retriever, query rewrite, contextual augmenter).
- Short-term: Session API (event-sourced, JDBC) + compaction (triggers + strategies, incl. recursive summarization).
- Token lever: Tool Search Tool (34-64% savings; 64% on Anthropic).
- Backend: Bedrock/Vertex keep inference in-tenant; Anthropic Enterprise = governed DPA, not in-tenant. Spring AI starters per provider. (Company policy = confirm at work.)
- The wedge over the framework baseline: Bishop's hardening (quarantine-before-canon, deterministic freshness/integrity gates, promotion gating) vs the framework's "ask the model to consolidate."

## 12-inch table (adjacent + touched systems + contracts)
| System touched | Change | Authorization |
|---|---|---|
| `mnemosyne/` repo | new design artifacts (spec, reference code, plan) | internal+reversible, autonomous |
| Bishop `memory/project_mnemosyne.md` + `MEMORY.md` | pointer + index (DONE this session) | internal, autonomous |
| Bishop `competency-matrix.md` | new Mnemosyne rows (DONE this session) | internal, autonomous |
| `comms/` supervisor email | draft only; **Ryan edits + sends** | EXTERNAL SEND — Ryan only, never Bishop |
| GitHub `VoodooReaper/mnemosyne` | created + pushed (DONE) | Ryan's infra, his repo-create |
- Adjacent found/handled: gh-auth failure (resolved via manual repo create); email provenance (Ryan ruled: none).
- No company data or source touches this repo (hard boundary). No compile/run environment assumed present.

## PROCESS-IMPROVEMENT (#129)
- **Script vs model:** the design *spec* is judgment (model-authored). But the deliverable must SPEC deterministic mechanisms, not model-trusted ones: the freshness/integrity/promotion gates are designed as scripts (the Bishop pattern: `verify_state`-style checks), explicitly contrasted with the framework's model-trusted `memoryConsolidationTrigger`. Reference code is illustrative; any "it compiles/runs" claim is gated by the output-reality rule (#139) and NOT made unattended.
- **Deterministic gate vs judgment:** memory promotion (quarantine->canon) and staleness sweeps = deterministic gates in the spec; semantic retrieval relevance = model/embedding judgment.
- **More repeatable than last time:** grounded in two cited research briefs (not memory); spec follows a fixed section template; competency rows declared up front; the spec itself is the reusable artifact for the eventual work-seat build.

## MATRIX-ROWS (competency)
| Row | Score | Research-dispatch / note |
|---|---|---|
| Spring AI framework | 3 | v1+v2 read, primary-sourced. Informed, unbuilt. |
| Enterprise long-term-memory architecture | 3 | Bishop self-eng = 4 (shipped own memory); Spring-AI-Java impl unproven. |
| MongoDB + vector stores | 2 -> 3 | **Research-dispatched in v2** (scout 1, Mongo Atlas Vector section) — now informed. |
| RAG / retrieval advisors | 3 | v2 scout 1, RAG advisors section. |
| Java/Spring Boot greenfield | 3 | reads/explains; the build is the ramp. |
- No sub-3 row remains un-researched (Mongo/vector closed in v2).

## Grill Q&A
- **Q1 [ANSWERED]:** v0 definition-of-done / output scope. **Ruling: as recommended — design spec + illustrative reference code, every code file flagged design-stage / not-yet-compiled.** No compile/run claim made unattended (output-reality #139). The reference code is drop-in material for the eventual work-seat build.
- **Q2 [ANSWERED]:** scope boundary. **Ruling: as recommended — memory layer only.** The multi-department agent/tools/orchestration get a brief roadmap note, not a v0 spec (cannot design department agents well without knowing company systems yet).
- No further strategic forks. Remaining decisions are Bishop-owned tactical calls (below). Close fork = gate-1 approval (pending).

## Tactical calls (Bishop-owned, recorded not asked)
- **TC1 Backend-agnostic.** Company policy unknown, so the design supports Bedrock / Vertex / Anthropic interchangeably; the sanctioned-channel pick is flagged as a confirm-at-work item, not baked in.
- **TC2 Portable vector layer.** Design against Spring AI's `VectorStore` abstraction with `MongoDBAtlasVectorStore` as the reference implementation (matches Spring AI's portability ethos + unknown stack specifics).
- **TC3 Hardening is the spec's core.** Full Bishop hardening (quarantine->canon, deterministic freshness/integrity gates, promotion) is baked in as the differentiator over the framework baseline. This is the wedge; not optional.
- **TC4 Reference code is design-stage.** Illustrative, explicitly flagged not-yet-compiled; no compile/run claim made unattended (#139). The working build is a together task.
- **TC5 Fixed spec template** for repeatability (#129).

## Definition of done — numbered observable ACs
All committed to `mnemosyne/`, data-free, no compile/run claim:
1. **Architecture spec** of the two-layer long-term memory system on Spring AI (curated file-canon via AutoMemoryTools + semantic recall via VectorStore/RAG advisor), including a diagram of the advisor stack on one `ChatClient`.
2. **Hardening layer** specced as deterministic mechanisms: quarantine->canon promotion, freshness/integrity gates, consolidation, each explicitly contrasted with the framework's model-trusted `memoryConsolidationTrigger`.
3. **Reference code (design-stage, flagged not-yet-compiled):** `AutoMemoryToolsAdvisor` + `MongoDBAtlasVectorStore` + `RetrievalAugmentationAdvisor` + Session/compaction wiring; the 4-type memory file schema; at least one deterministic gate sketch.
4. **Data-boundary design:** backend-agnostic (Bedrock/Vertex/Anthropic), sanctioned-channel pick flagged as confirm-at-work.
5. **Token-frugality design:** Tool Search Tool + compaction strategies as first-class.
6. **Integration boundary** to the short-term Session layer documented; **short roadmap note** for the multi-department agent (per Q2, not detailed).
7. Spec under a `design/` path, reference code under a `reference/` path; `PROJECT.md` phase advanced to Execute; plan Status APPROVED + committed.
