# Research Brief: Spring AI Agentic Patterns + Token Discipline

**Date:** 2026-06-22
**Phase:** CRPE-R Research (v1; round-out gaps listed at the end)
**Sources:** Spring AI "Agentic Patterns" blog series, Parts 1-7, plus the Dynamic Tool Discovery post. All public spring.io content, fetched and indexed 2026-06-22. No company data involved.

## Sources (cited)

1. Part 1 — Agent Skills: https://spring.io/blog/2026/01/13/spring-ai-generic-agent-skills
2. Part 2 — AskUserQuestionTool: https://spring.io/blog/2026/01/16/spring-ai-ask-user-question-tool
3. Part 3 — TodoWriteTool: https://spring.io/blog/2026/01/20/spring-ai-agentic-patterns-3-todowrite/
4. Part 4 — Subagent Orchestration: https://spring.io/blog/2026/01/27/spring-ai-agentic-patterns-4-task-subagents
5. Part 5 — A2A Integration: https://spring.io/blog/2026/01/29/spring-ai-agentic-patterns-a2a-integration
6. Part 6 — AutoMemoryTools (persistent long-term memory): https://spring.io/blog/2026/04/07/spring-ai-agentic-patterns-6-memory-tools
7. Part 7 — Session API (short-term + compaction): https://spring.io/blog/2026/04/15/spring-ai-session-management
8. Dynamic Tool Discovery (34-64% token savings): https://spring.io/blog/2025/12/11/spring-ai-tool-search-tools-tzolov

## Headline finding

**Bishop is a working, hardened superset of the entire Spring AI Agentic Patterns playbook.** Pattern for pattern:

| Spring AI pattern | Bishop equivalent (already shipped) |
|---|---|
| Part 1 Agent Skills (`SKILL.md` + YAML, `.claude/skills`) | 23 skills, same open spec, same directory |
| Part 2 AskUserQuestion (clarify before acting) | The gate-1 grill, one question at a time |
| Part 3 TodoWrite (structured tasks) | tasks.md / TaskCreate |
| Part 4 Subagent Orchestration (Task tool) | Hub-and-spoke roster (#49) |
| Part 5 A2A (Agent2Agent interop) | Multi-department interop layer (to build) |
| Part 6 AutoMemoryTools (long-term memory) | `memory/` system — near-identical |
| Part 7 Session API (short-term + compaction) | Transcript + compaction |
| Dynamic Tool Discovery (token savings) | Progressive disclosure / token discipline |

Implication: the project is a **port + harden**, not a greenfield learn. The deliverable should be "the framework pattern PLUS the production hardening the tutorials omit."

## Part 6 — AutoMemoryTools (THE deliverable)

Persistent long-term memory across sessions. Design, verified from the docs:

- **Memory file format:** Markdown file with YAML frontmatter, fields `name`, `description`, `type`, then body. Example: `name: user profile` / `description: Alice, backend engineer, prefers short answers` / `type: user`. **This is exactly Bishop's memory file format.**
- **Index + save:** "typed files, `MEMORY.md` index, two-step save." Identical to Bishop.
- **Three integration modes:**
  - **Option A — `AutoMemoryTools` + `AutoMemoryToolsAdvisor`:** relative paths, **sandboxed to a memories root, built-in traversal guard.** Accepts a `memoryConsolidationTrigger` predicate for automatic consolidation.
  - **Option B — explicit:** same tools, manual consolidation by asking.
  - **Option C — `FileSystemTools` + `ShellTools`:** the agent uses generic Read/Write/Edit/Bash; memory is just a directory. Same conventions (typed files, `MEMORY.md`, two-step save) but **no sandbox** (stays in dir by convention only). Driven by a system prompt (`AUTO_MEMORY_FILESYSTEM_TOOLS_SYSTEM_PROMPT.md`) parameterized with the memories root.
- **Keeping memory clean:** "over time a store accumulates redundant, overlapping, or stale entries. Periodically asking the agent to consolidate (merge duplicates, drop outdated facts, tighten descriptions) keeps it lean." Option A can automate this via the trigger predicate.

**Where Bishop goes beyond the framework (the exceed-expectations wedge):**
- Quarantine-before-canon (`_inbox/`): machine proposals land quarantined; promotion to canon is a deliberate gated step. The framework writes straight to the store.
- **Deterministic** freshness/integrity gates (canon manifest tripwire, staleness sweeps) instead of "ask the agent to consolidate." Mechanical, not model-trusted.
- A **second retrieval layer**: flat markdown files do not scale to enterprise semantic search. Add embeddings + a vector store for retrieval-by-meaning. AutoMemoryTools alone is file-based.
- The sandbox/traversal guard (Option A) is directly relevant to the company data-protection boundary.

## Part 7 — Session API (short-term memory)

Event-sourced short-term memory with **context compaction**.
- Append-only event log; JDBC persistence via `spring-ai-starter-session-jdbc` (`AI_SESSION` + `AI_SESSION_EVENT` tables; Postgres/MySQL/MariaDB/H2).
- **Compaction = triggers + strategies (must be configured together or it throws):**
  - Triggers: `TurnCountTrigger(20)`, `TokenCountTrigger.threshold(4000)`, `CompositeCompactionTrigger.anyOf(...)`.
  - Strategies: `SlidingWindow`, `TurnWindow`, `TokenCount` (no LLM, cheap, snap to turn boundary) and `RecursiveSummarizationCompactionStrategy` (LLM summarizes archived events into a synthetic turn; rolling compressed history; `.maxEventsToKeep(n).overlapSize(k)`).
- **Architecture point:** Session API = short-term (one long conversation). The long-term layer (Part 6 + vector RAG) is the cross-session knowledge layer. This project owns the long-term layer and its handoff to/from Session.

## Token discipline (the budget constraint is a first-class feature)

The enterprise seat shares a hard-capped monthly token pool. Spring AI already provides the levers:
- **Dynamic Tool Discovery (Part 8-ish / Dec 2025):** 34-64% token savings by not loading all tool definitions into context; discover/load only relevant tools. (Mechanism detail = round-out gap.)
- **Compaction strategies** (above): cheap no-LLM windows keep context short; `TokenCountTrigger` enforces a hard ceiling.
- **Progressive disclosure** (Part 1 skills): load only name+description at discovery, full instructions only on activation. Register hundreds of skills, keep context lean.
- Design principle for everything built here: cheap models route, expensive models judge; retrieve short; summarize before reasoning.

## Parts 1-5 (brief)

- **Part 1 Agent Skills:** open spec (agentskills.io), same as Claude Code; `SkillsTool.builder().addSkillsDirectory(".claude/skills")`, wired as tool callbacks; needs `org.springaicommunity:spring-ai-agent-utils:0.4.2` + Spring AI `2.0.0-M2+`. Bishop's skills are directly loadable.
- **Part 2 AskUserQuestion:** agents that clarify before acting (= Bishop's grill).
- **Part 3 TodoWrite:** structured task management for agents (= tasks.md).
- **Part 4 Subagent Orchestration:** `Task tool` in spring-ai-agent-utils, portable/model-agnostic, inspired by Claude Code subagents. Specialized subagents in dedicated context windows return only essentials to the parent. Extensible to A2A. (= Bishop hub-and-spoke; the multi-department specialist pattern.)
- **Part 5 A2A:** Google Agent2Agent protocol for interoperable/heterogeneous agents. (Candidate for cross-department agent interop.)

## Coordinates (verify latest before building)

- Spring AI `2.0.0-M2+` (milestone — moving fast).
- `org.springaicommunity:spring-ai-agent-utils:0.4.2` (community module; check GitHub releases).
- `org.springaicommunity:spring-ai-starter-session-jdbc`.
- These are **community** modules, not Spring AI core. Treat versions/APIs as fluid; confirm against GitHub at build time.

## Competency check (against competency-matrix.md)

- **Strength:** Bishop platform self-engineering = 4 (shipped) — builds/grooms its own memory. The architecture competency is real.
- **Gaps:** Spring Boot/Java = 3 (reads/explains, not fluent author); Spring AI, MongoDB/vector stores, embeddings/RAG = net-new, unscored. Per #110 these stay informed (2-3) until a shipped + demoed artifact earns the 4. This project is the lab that earns them.

## Round-out gaps (before gate-1 grill)

1. MongoDB Atlas Vector Search integration in Spring AI (vector store API + index setup).
2. Spring AI vector store + RAG advisor reference docs (QuestionAnswerAdvisor / RetrievalAugmentationAdvisor).
3. Full AutoMemoryTools API surface (tool names, advisor config, the system-prompt contract).
4. Dynamic Tool Discovery mechanism detail.
5. A2A specifics (when A2A vs Task-tool subagents for departments).
6. Anthropic Enterprise + Vertex/Bedrock backend routing for Spring AI (sanctioned-channel config).
