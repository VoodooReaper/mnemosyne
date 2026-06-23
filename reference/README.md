# Mnemosyne v0 — Reference Code (DESIGN-STAGE)

> **DESIGN-STAGE. NOT-YET-COMPILED. NO compile/run claim is made for any file here.**
> This is illustrative reference code: drop-in material and a concrete shape for the
> eventual work-seat build, written against the Spring AI API surface documented in
> `../research/2026-06-22-spring-ai-api-deep-dive.md`. Community-module class/method
> names (`spring-ai-agent-utils`, 2.0.0-M2+ / SNAPSHOT) are fluid; confirm at build
> time. (Bishop decision #139 output-reality gate; gate-1 TC4.) No company data or
> source appears here (decision #145) — placeholders only.

## File map

| File | Maps to | What it shows |
|---|---|---|
| `dependencies.md` | AC3 | BOM + starters per layer |
| `config/MemoryArchitectureConfig.java` | AC1/AC3 | The three advisors wired on one `ChatClient`, ordered |
| `memory/AutoMemoryAdvisorSetup.java` | AC3 | Layer A — `AutoAutoMemoryToolsAdvisor` (file canon) |
| `memory/VectorRecallSetup.java` | AC3 | Layer B — `MongoDBAtlasVectorStore` + `RetrievalAugmentationAdvisor` |
| `memory/SessionMemorySetup.java` | AC3 | Layer S — session/chat-memory + compaction |
| `memory/schema/memory-file-schema.md` | AC3 | The 4-type memory file schema (user/feedback/project/reference) + templates |
| `memory/schema/MEMORY.md` | AC3 | Example always-loaded index |
| `hardening/MemoryCanonGate.java` | AC2/AC3 | A deterministic gate sketch (stdlib-only, venue-portable) |

## How to read it

Start from `config/MemoryArchitectureConfig.java` (the assembly), then each `memory/*Setup.java`
for the per-layer detail, then `hardening/MemoryCanonGate.java` for the wedge. Design docs in
`../design/` explain the why; this folder shows the how.

## When this becomes real

The working build is a together-session on Ryan's work Anthropic Enterprise seat, against company
infrastructure, under company policy. At that point: confirm community-module API names, pick the
backend (`../design/03-data-boundary.md`), wire real config, and only then make a compile/run claim
under the output-reality gate.
