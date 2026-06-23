# Mnemosyne — Canonical State

**Load this first when working on Mnemosyne.** Append-only decisions + live state.

## Identity & bet

Mnemosyne is the design-and-build workspace for Ryan's Winsupply CAIO/dev mandate. First deliverable: a **long-term memory layer for Spring AI** (Java). Then: the **multi-department enterprise agent** that uses it. The bet is that Bishop is a working superset of Spring AI's Agentic Patterns, so the job is to port a proven architecture and harden it past the tutorial baseline.

## Scope

**In:** long-term memory architecture for Spring AI; the curated-canon + vector-RAG two-layer design; multi-department agent architecture (skills, subagents, memory, tools); design specs; data-free reference prototypes; Ryan's learning of the stack.

**Out (separate tracks):** the Salesman App competition (parked, separate); the actual company-infra implementation (lives on the work seat); anything touching company data.

## Hard boundary (data wall)

Personal design repo. **No company data or source, ever, anywhere in this repo or any prompt run from it.** Implementation against company systems happens on Ryan's work Anthropic Enterprise seat under company policy. Bishop (personal Max) and this repo stay on the design/abstraction side of the wall. See `memory/project_mnemosyne.md` in Bishop for the governance pointer.

## Architecture thesis (to be hardened at gate-1)

Two layers, mirroring Bishop's proven design:
1. **Curated canon** — typed Markdown + YAML-frontmatter memory files + a `MEMORY.md` index + a two-step save. This is exactly Spring AI's `AutoMemoryTools` format. Bishop adds: quarantine-before-canon (`_inbox`), deterministic freshness/integrity gates, periodic consolidation.
2. **Semantic recall** — embeddings + a vector store (candidate: **MongoDB Atlas Vector Search**, already in the Winsupply stack) for retrieval-by-meaning at enterprise scale, where flat files do not scale. RAG via Spring AI advisors.

Short-term vs long-term split: Spring AI's **Session API** (event-sourced + compaction) handles within-conversation memory; the **long-term layer above** is this project's deliverable.

## Target stack

Spring AI `2.0.0-M2+` (milestone), `org.springaicommunity:spring-ai-agent-utils`, Java / Spring Boot, MongoDB Atlas Vector Search, multi-cloud (GCP/Vertex + AWS/Bedrock + Anthropic Enterprise). Token-budget-constrained by design (shared enterprise pool, hard cap).

## Phase & gates (CRPE-R)

- **Concept** — LOCKED 2026-06-22 (this conversation).
- **Research** — IN PROGRESS. Deliverable: `research/2026-06-22-spring-ai-agentic-patterns-brief.md` (done, v1). Round-out needed: Mongo Atlas Vector Store in Spring AI, Spring AI vector-store + RAG advisor reference docs, full AutoMemoryTools API surface, A2A specifics.
- **Plan (gate-1 grill)** — NEXT. Design the long-term-memory architecture, one question at a time, zero TBDs.
- **Execute → v0** — design spec + data-free reference prototype here; company build on the work seat.
- **Retro** — capability write-back to the competency matrix.

## Decisions (project-local)

- D1 (2026-06-22): Project created, name `mnemosyne`, personal design-repo boundary approved by Ryan end-to-end.

## Open questions for gate-1

1. Vector backend: Mongo Atlas Vector Search (in-stack) vs alternatives, and how it coexists with the file-canon layer.
2. AutoMemoryTools integration mode: sandboxed advisor (Option A) vs FileSystemTools+ShellTools (Option C) — the sandbox/traversal guard matters for the company data boundary.
3. Multi-department scoping: one shared memory with department namespaces, or per-department stores + a shared canon.
4. Subagents (Part 4 Task tool) vs A2A (Part 5) for department specialists.
5. How the long-term layer hands off to/from the Session API short-term layer.
6. Which freshness/consolidation gates port from Bishop as deterministic (not "ask the agent").

## Next actions

1. Round out the research brief (the 4 gaps above).
2. Run the gate-1 grill.
