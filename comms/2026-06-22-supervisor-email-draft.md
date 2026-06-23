# DRAFT — email to Jared (Ryan's review + send; never auto-sent)

Posture: "what I'm thinking, and why" (early thinking, invites steer). Email, informal.
Context: Jared = direct supervisor, strong dev, leaning on Ryan for AI. Met today, got along well.
Jared already knows Bishop + has seen fianchetto.ai and nextgameperformance; this task grew out of
that conversation. Guardrails: Ryan's voice, no em dashes, no AI-provenance line (Ryan's call: none).

---

**Subject:** Early thinking on the memory project

Hey Jared,

Really enjoyed today, and the memory project has been turning over in my head since, so I wanted to get some early thinking in front of you and hear your steer before I go heads-down.

The short version: I think the right shape is two layers, not one.

- Short-term: a per-conversation memory that stays inside the context window, using Spring AI's Session API and its built-in compaction so long conversations do not blow the token budget.
- Long-term: durable, cross-session knowledge the agent retrieves by meaning. Spring AI gives us most of the parts already: AutoMemoryTools for curated, typed memory files, plus a vector store such as MongoDB Atlas Vector Search (which fits our stack) behind a retrieval step for semantic recall.

A few things I want to design around from the start:

- Token cost. Spring AI's dynamic tool discovery cuts tool-related token usage by roughly a third to two-thirds depending on the model, and compaction caps context growth. I want the memory layer frugal by design, not something we walk back once the bill shows up.
- Data boundary. Whatever we build should keep inference inside our own cloud where policy requires it (both the AWS and Google paths can do that). I would want to pin down our exact policy early so the architecture matches it from day one.
- Keeping memory clean, not just storing it. The hard part of long-term memory is not writing it, it is stopping it from rotting: stale or duplicate entries that resurface and mislead the agent. I want deterministic checks for that, not a hope that the model tidies up after itself.

This is the same problem I have been solving in Bishop, the system I showed you. Persistent long-term memory, subagent delegation, skill-based capabilities, all of it. Spring AI's Agentic Patterns series maps almost one to one onto what I have already built, broken, and rebuilt there. So the architecture is familiar ground for me; the Java and Spring AI surface is what I am ramping on, and that is coming fast.

It is early and you have context I do not yet. What have you already worked through here, and are there constraints or priorities I should be designing toward? Happy to turn this into a fuller write-up whenever it would help.

Talk soon,
Ryan
