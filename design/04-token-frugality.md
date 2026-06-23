# 04 — Token-Frugality Design (AC5)

**Status:** v0 design-stage. No compile/run claim.
**Date:** 2026-06-22

## Purpose

The enterprise constraint is a shared, hard-capped token pool. Token frugality is therefore a first-class design property, not an afterthought. This doc specifies the two highest-leverage levers — **dynamic tool discovery (Tool Search Tool)** and **session compaction** — and how the memory architecture stays cheap by default.

## Design

### Lever 1 — Tool Search Tool (dynamic tool discovery)

The problem: an enterprise agent with many tools (memory tools, RAG, plus future department tools) pays a per-call token tax for every tool schema sent to the model, every turn. Tool schemas are verbose, and Anthropic's are the most verbose of the major providers.

The mechanism (Spring AI):
- At conversation start, **only `toolSearchTool` is sent**, not the full tool catalog.
- The model calls it with a natural-language query; a vector index (`ToolIndex`, Lucene default) over tool *descriptions* runs a similarity search.
- A **Recursive Advisor** expands only the matched tool definitions into context for the next request.

Verified savings from research (28-tool benchmark): **Anthropic 64%** (6,273 vs 17,342 tokens), Gemini 60%, OpenAI 34%. The biggest win is on Anthropic models precisely because their schemas are largest, which matters if the company runs Claude via Bedrock/Anthropic.

Wiring: starter `spring-ai-starter-tool-search-advisor`; `spring.ai.chat.client.tool-search-advisor.enabled=true`. It sits last in the advisor chain (`01-architecture.md`) because it operates on the tool surface, not memory content.

**Design implication:** every tool added for a future department agent costs ~nothing at rest, because it is only loaded when semantically relevant. This makes the multi-department roadmap (`05`) affordable.

### Lever 2 — Session compaction

The session layer (Layer S) is the other token sink: raw conversation history grows unbounded. Spring AI's Session API is event-sourced (JDBC) with **compaction triggers + strategies**, including recursive summarization.

Design choices:
- **Trigger:** compact on a token/message-count threshold, deterministically (not "when the model feels like it").
- **Strategy:** recursive summarization for older turns; keep a verbatim window of the most recent N turns (`MessageWindowChatMemory`).
- **What survives compaction:** anything that should persist beyond the session is not the session's job to keep — it should have been **promoted to Layer A (file canon)** via a memory tool. Compaction can safely summarize aggressively because durable facts live in canon, not in the transcript. This is the payoff of the layered design: the session can forget because long-term memory remembers.

### Frugality as a property of the whole stack

- **Layer A** is read on demand via tools, not dumped into every prompt — the model pulls the file it needs.
- **Layer B** retrieval is bounded by `topK` + `similarityThreshold`; it injects only the few most-relevant chunks, not the corpus.
- **Layer S** is compacted.
- **Tool surface** is search-gated.

The architecture's default posture is "load the minimum that answers this turn," which is the same index-don't-dump discipline Bishop runs (decision #99 token conservation).

## Deterministic vs model-judgment

- **Deterministic:** compaction trigger thresholds; `topK`/`threshold` caps; which tools exist; the verbatim-window size.
- **Model-judgment:** the natural-language tool-search query; the content of a recursive summary; which memory file to pull. Caps and thresholds bound the cost of every model judgment.

## Confirm-at-work / open items

- The actual per-call tool count for the company agent (drives how much Tool Search saves).
- Compaction threshold tuning against the real token budget and conversation length distribution.
- Whether the Lucene-default `ToolIndex` is sufficient or the tool index should share the Mongo Atlas vector substrate.

## Sources

- `research/2026-06-22-spring-ai-api-deep-dive.md` Gap 4 (Tool Search Tool, verified savings) + short-term/compaction notes.
- `docs.spring.io/spring-ai/reference/guides/dynamic-tool-search.html`.
- Bishop decision #99 (token conservation; index-don't-dump).
