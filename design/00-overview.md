# Mnemosyne v0 Design Spec — Overview & Reading Order

**Status:** v0 design-stage. Architecture + illustrative reference code. NO compile/run claim is made for any code in this spec (decision #139 output-reality gate; gate-1 TC4). A working build is a later together-session on Ryan's work seat.
**Date:** 2026-06-22
**Phase:** CRPE-R Execute (v0)
**Plan:** `plans/2026-06-22-v0-longterm-memory-architecture.md` (gate-1 APPROVED, 7 ACs)
**Grounding:** `research/2026-06-22-spring-ai-agentic-patterns-brief.md` (v1) + `research/2026-06-22-spring-ai-api-deep-dive.md` (v2)

## What this spec is

A design for a **long-term memory layer for Spring AI agents**, ported from Bishop's proven memory architecture and hardened past the framework's tutorial baseline. It is the first Mnemosyne deliverable for Ryan's Winsupply mandate. It is memory-layer-only by ruling (gate-1 Q2); the multi-department agent gets a roadmap note, not a v0 spec.

## Hard boundary (repeated because it is load-bearing)

This is a **personal design repo**. No company (Winsupply) data or source enters this repo, any commit, or any model prompt run from here. The real implementation runs on Ryan's work Anthropic Enterprise seat under company policy. See `mnemosyne/CLAUDE.md` and Bishop decision #145.

## The bet (why this is a port, not a greenfield)

Spring AI's `AutoMemoryTools` is near-identical to Bishop's memory system: the same four typed categories (`user` / `feedback` / `project` / `reference`), a `MEMORY.md` index, a two-step save, and a sandboxed file root. Bishop has run this design in production for months and added the hardening the framework leaves to the model. So the job is to **port a battle-tested architecture and graft on the hardening + a semantic-recall layer for enterprise scale.**

## Reading order

| Doc | AC | Covers |
|---|---|---|
| `00-overview.md` (this) | — | Map, boundary, bet, template |
| `01-architecture.md` | AC1 | Two-layer model; the three-advisor stack on one `ChatClient`; diagram |
| `02-hardening.md` | AC2 | Deterministic mechanisms (quarantine->canon, freshness/integrity gates, consolidation) vs the framework's model-trusted `memoryConsolidationTrigger` |
| `03-data-boundary.md` | AC4 | Backend-agnostic design (Bedrock / Vertex / Anthropic); sanctioned-channel pick = confirm-at-work |
| `04-token-frugality.md` | AC5 | Tool Search Tool + compaction strategies as first-class |
| `05-session-integration-and-roadmap.md` | AC6 | Boundary to the short-term Session layer; multi-department agent roadmap note |
| `../reference/` | AC3 | Design-stage Java + schema + a deterministic gate sketch |

## Section template (TC5, for repeatability)

Every design doc follows: **Purpose -> Design -> Deterministic vs model-judgment (where relevant) -> Confirm-at-work / open items -> Sources.** This fixed shape is the reusable artifact for the eventual work-seat build.

## The three layers at a glance

1. **Curated file canon** (`AutoAutoMemoryToolsAdvisor`) — typed Markdown + YAML frontmatter, `MEMORY.md` index, two-step save, sandboxed root. The durable, auditable, human-reviewable spine. Ports 1:1 from Bishop.
2. **Semantic recall** (`MongoDBAtlasVectorStore` behind `RetrievalAugmentationAdvisor`) — embeddings + vector search for retrieval-by-meaning at enterprise scale, where flat files do not scale.
3. **Short-term session** (`MessageChatMemoryAdvisor` / Session API + compaction) — within-conversation working memory. The boundary, not the deliverable, but specced because the long-term layer reads from and writes to it.

On top of all three: the **hardening layer** (deterministic gates), which is the wedge over the framework baseline.

## Provenance

model provenance: Claude Code / Opus 4.8. No external publish; internal design artifact.
