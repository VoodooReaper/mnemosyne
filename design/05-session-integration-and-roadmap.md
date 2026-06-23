# 05 — Session Integration Boundary + Multi-Department Roadmap (AC6)

**Status:** v0 design-stage. No compile/run claim.
**Date:** 2026-06-22

## Purpose

Two things: (1) document the boundary between the long-term memory layer (this deliverable) and the short-term Session layer it sits beside, since the two exchange data every turn; (2) a short roadmap note for the multi-department agent, which by gate-1 ruling (Q2) is out of v0 scope and gets direction, not a spec.

## Part 1 — The long-term <-> session boundary

### What each layer owns

- **Session layer (short-term, Layer S).** The current conversation: an event-sourced history (Spring AI Session API, JDBC) with compaction. Owns "what was just said." Bounded and disposable; its job is to give the model recent context cheaply.
- **Long-term layer (this deliverable, Layers A + B).** Owns "what is true and pinned" (A) and "what we know that is related" (B). Survives across sessions, users, and restarts.

### The contract between them (the boundary)

The boundary is defined by **who reads from whom, and what promotes across it:**

1. **Read direction (every turn):** both layers feed the same assembled prompt through their advisors (`01-architecture.md`). The session advisor supplies compacted recent turns; the long-term advisors supply pinned facts (A, via tools) and retrieved context (B, via RAG). They are siblings on the chain, not nested.
2. **Promotion (session -> long-term):** a fact worth keeping beyond the conversation is **promoted out of the session into Layer A** via a memory-tool call, which (hardened) routes through quarantine -> gate -> canon (`02-hardening.md`). The session itself never becomes the long-term store; it is explicitly allowed to forget *because* durable facts have been promoted.
3. **No reverse contamination:** the long-term layer never writes into the session transcript. Retrieved context is injected into the prompt for the current turn only; it does not mutate session history.
4. **Compaction safety (the payoff):** because durable facts live in canon, the session can compact/summarize aggressively without data loss. The boundary is what makes the frugality design (`04`) safe.

### Why keep them separate (not one store)

Different durability, different access pattern, different governance. Session is high-churn and volatile; canon is read-mostly and gated; vector recall is broad and fuzzy. Collapsing them would force one set of tradeoffs (e.g. gating every conversational turn, or letting unvetted turns become permanent truth). The separation is the same precision-vs-recall-vs-recency split from `01`.

### Boundary diagram

```
   ┌──────────────────────── one ChatClient turn ────────────────────────┐
   │                                                                      │
   │   Layer S (session)        Layer A (canon)        Layer B (vector)   │
   │   recent turns ───────┐    pinned facts ──┐       related docs ──┐   │
   │   (compacted)         │    (via tools)    │       (via RAG)      │   │
   │                       ▼                   ▼                      ▼   │
   │                   ┌──────────────── assembled prompt ───────────┐   │
   │                   └──────────────────────────────────────────────┘  │
   │                                                                      │
   │   promotion: a keep-worthy fact in S ──(memory tool)──► quarantine   │
   │              ──(deterministic gate, doc 02)──► canon (A)             │
   └──────────────────────────────────────────────────────────────────────┘
```

## Part 2 — Multi-department agent roadmap note (direction only, per Q2)

The multi-department enterprise agent is **out of v0 scope** and deliberately under-specified: you cannot design department agents well without knowing the company systems (which data lives where, which tools each department needs). This is direction for the next phase, not a spec.

### Sketched direction (to harden later, on the work seat)

1. **Memory scoping.** Likely one shared canon for cross-department truth (company facts, policies) plus **per-department namespaces** for domain-specific memory, all under the same hardened gate. Open question carried from PROJECT.md: shared-with-namespaces vs per-department stores + shared canon.
2. **Department specialists = in-process Task-tool subagents first.** Same JVM, one team, lower latency, tighter type safety. Graduate a department to **A2A** (Google Agent2Agent: agent card at `/.well-known/agent.json`, JSON-RPC/HTTP) only when it becomes a separately-deployed service owned by another team. (Research v2 Gap 5.)
3. **Token discipline carries over.** Every department tool is registered behind the Tool Search Tool (`04`), so adding departments does not inflate per-call cost.
4. **The memory layer is the shared substrate.** This v0 deliverable is what every department agent reads from and writes to; building it first and hardening it is the precondition for the orchestration layer. That sequencing is intentional.

### What must be confirmed before the department phase

- Company system inventory (where data lives: SQL / Mongo / both).
- Backend/tenancy policy (`03-data-boundary.md`).
- Whether departments are co-deployed (subagents) or separate services (A2A).

## Deterministic vs model-judgment

- **Deterministic:** the promotion path across the boundary; compaction triggers; namespace routing (a department tag is a deterministic filter).
- **Model-judgment:** whether a session fact is worth promoting; which department a query belongs to (until namespaces make it a filter); cross-department synthesis.

## Sources

- `research/2026-06-22-spring-ai-api-deep-dive.md` Gap 5 (A2A vs in-process subagents) + short-term Session/compaction notes.
- Mnemosyne `PROJECT.md` open questions (multi-department scoping).
- gate-1 plan Q2 ruling (memory-layer-only; departments get a roadmap note).
