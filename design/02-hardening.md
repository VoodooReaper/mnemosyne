# 02 — Hardening Layer: Deterministic Mechanisms (AC2)

**Status:** v0 design-stage. No compile/run claim.
**Date:** 2026-06-22

## Purpose

Specify the production hardening that sits on top of the Spring AI memory primitives. This is the **wedge** over the framework's tutorial baseline. The framework gives you memory tools and a `memoryConsolidationTrigger`; it then trusts the model to maintain its own memory. Bishop's months of production experience say that is where memory rots. The hardening layer replaces "ask the model to keep memory clean" with **deterministic gates the model cannot talk its way past.**

## The core contrast

| Concern | Framework baseline (model-trusted) | Mnemosyne hardening (deterministic) |
|---|---|---|
| When to consolidate | `memoryConsolidationTrigger` BiPredicate, then the model decides what/how to consolidate | A scheduled deterministic job + explicit promotion gate; the model proposes, a script decides |
| New fact entering canon | Model calls `MemoryCreate`, writes straight to the canon root | Write lands in **quarantine** (`_inbox/`); a deterministic gate validates and promotes |
| Index correctness | Model is told to keep `MEMORY.md` in sync | A script verifies `MEMORY.md` <-> files are in sync; mismatch fails the gate |
| Staleness | Model is told facts can go stale | A deterministic sweep flags files past an age/▲threshold; surfaces, never auto-deletes |
| Integrity / tampering | none | A canon manifest (hash/inventory) tripwire; unexpected canon mutation fails the gate |
| Schema validity | Model is told the frontmatter shape | A script parses every file's frontmatter and type; invalid shape fails the gate |

The principle is Bishop decision #92 (a script beats a model whenever a script can do the job) applied to memory governance: mechanical checks are scripts, never an agent's "I checked."

## Design — the four deterministic mechanisms

### 1. Quarantine -> canon promotion (the `_inbox` pattern)

Model-proposed canon writes do not land in canon. They land in a quarantine directory (`memoriesDir/_inbox/`). Promotion to canon is a **separate, gated step**:

1. Model calls a memory tool to save a fact -> the write is redirected to `_inbox/` (or a parallel "proposed" namespace).
2. A deterministic promotion gate runs (on schedule, on commit, or on explicit invocation): it validates frontmatter shape, type, dedup against existing canon, and index impact.
3. Only on a clean gate does the fact move into canon and `MEMORY.md` get its pointer line.
4. Anything failing the gate stays quarantined and is surfaced for human review, never silently dropped.

This mirrors Bishop's `memory/_inbox/` quarantine + drain. It means a confused or adversarial model turn cannot corrupt canon directly; the worst it can do is fill quarantine, which is visible and reversible (git-backed).

### 2. Freshness / staleness gate

A deterministic sweep walks every canon file and flags:
- files older than an age threshold whose `type` implies volatility (e.g. `project` state is expected to change; `reference` less so),
- files whose content references dates/versions now past,
- `MEMORY.md` entries pointing to files that moved or changed hook text.

Output is a FRESH / STALE / N/A audit. **It flags, it does not auto-edit** (Bishop's `verify-state-fresh` pattern): stale canon is surfaced to a human or a gated consolidation job, because auto-rewriting pinned truth is exactly the model-trusted failure mode we are removing.

### 3. Integrity gate (manifest tripwire + index sync)

A canon manifest records the expected inventory of canon files (and optionally content hashes). On every gate run:
- **Index sync:** every canon file has exactly one `MEMORY.md` pointer and vice versa. Drift fails.
- **Manifest tripwire:** canon mutations outside the sanctioned promotion path are detected (a file appeared/changed/vanished without going through quarantine). Fails the gate and surfaces the diff.
- **Schema validity:** frontmatter parses; `type` is one of the four; required fields present.

This is the deterministic answer to "did memory actually stay consistent," replacing the model's self-report. Bishop ships this as `tools/verify_state.py --update-canon-manifest`. **Scope note (v0):** the reference sketch `../reference/hardening/MemoryCanonGate.java` ports the **index-sync + schema-validity + uniqueness** sub-checks of this gate; the **manifest/hash tripwire** is designed here but is NOT in the v0 sketch (it needs a persisted manifest store, deferred to the work-seat build). The sketch's own header honestly lists the five checks it implements.

### 4. Consolidation (gated, not model-trusted)

The framework's `memoryConsolidationTrigger` only decides *when* to ask the model to consolidate. The hardened design keeps that trigger as one input but routes consolidation through a deterministic pipeline:
- the trigger (or a schedule) proposes a consolidation pass,
- the model may *draft* merges/summaries of related quarantined or stale entries,
- the **deterministic gate** validates the draft (no canon fact silently deleted, index stays consistent, schema valid, dedup correct) before anything is written,
- diffs are git-committed so every consolidation is auditable and revertible.

Model proposes; gate disposes; git records.

## Where the gate runs

The same deterministic gate is designed to run in three venues (defense in depth):
1. **Pre-commit / CI** — blocks a bad canon state from ever being committed (Bishop's primary venue).
2. **Scheduled job** — periodic freshness + integrity sweep (Bishop's nightly groom).
3. **Post-tool hook** — optional, validates a model's memory-tool output at hand-back before it reaches quarantine.

The reference sketch (`MemoryCanonGate.java`) is plain Java (stdlib-only, no Spring dependency) precisely so it can run in any of these venues, including CI without a running application context.

## Deterministic vs model-judgment

- **Deterministic (this whole layer):** quarantine redirection, promotion validation, freshness sweep, index-sync check, manifest tripwire, schema validation, consolidation-diff validation.
- **Model-judgment (wrapped by the above):** what fact is worth saving, how to phrase a consolidated summary, which entries are duplicates *in meaning*. The gate never asks the model to certify its own correctness; it checks the artifact.

## Confirm-at-work / open items

- Whether the company stack runs gates in CI (Jenkins/GitHub Actions/GitLab) or a scheduler determines the gate's packaging. The Java sketch is venue-portable by design.
- Hash-based manifest vs inventory-only manifest is a cost/strictness tradeoff to set at build time.

## Sources

- Bishop hardening in production: decisions #68 (REMEMBER/LEARN/GOVERN), #92 (deterministic-first), #67/#129 (verify_state gate), `_inbox` quarantine pattern, `wiki/knowledge-schema.md`.
- `research/2026-06-22-spring-ai-api-deep-dive.md` Gap 1 (`memoryConsolidationTrigger` default `(req,instant)->false`).
