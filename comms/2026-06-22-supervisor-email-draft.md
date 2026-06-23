# DRAFT — email to supervisor (Ryan's review + send; never auto-sent)

Posture: "what I'm thinking, and why" (early thinking, invites steer). Email, informal, direct supervisor.
Guardrails held: Ryan's voice, no em dashes, no company-confidential specifics, no AI-provenance line added (Ryan's call).

---

**Subject:** Early thinking on the long-term memory work

Hi [Supervisor name],

Wanted to share where my head is on the long-term memory piece for our Spring AI work while it is still early. I would rather show you my reasoning and get your steer now than go heads-down and resurface in two weeks.

The short version: I think the right shape is two layers, not one.

- Short-term: a per-conversation memory that stays inside the context window, using Spring AI's Session API and its built-in compaction so long conversations do not blow the token budget.
- Long-term: durable, cross-session knowledge the agent can retrieve by meaning. Spring AI gives us most of the parts here: AutoMemoryTools for curated, typed memory files, plus a vector store such as MongoDB Atlas Vector Search (which fits the stack we already use) behind a retrieval step for semantic recall.

A few things I am deliberately designing around from the start:

- Token cost. Spring AI's dynamic tool discovery cuts tool-related token usage by roughly a third to two-thirds depending on the model, and compaction caps context growth. I want the memory layer to be frugal by design, not something we walk back once the bill shows up.
- Data boundary. Whatever we build should keep inference inside our own cloud where policy requires it (both the AWS and Google paths can do this). I would want to confirm our exact policy here early so the architecture matches it from day one.
- Keeping memory clean, not just storing it. The hard part of long-term memory is not writing it, it is stopping it from rotting: stale or duplicate entries that resurface and mislead the agent. I want deterministic checks for that, not a hope that the model tidies up after itself.

Where my background helps: I have spent the last several months building and running an agent system with exactly these patterns, persistent long-term memory, subagent delegation, and skill-based capabilities. Spring AI's Agentic Patterns series maps almost one to one onto what I have already built, broken, and rebuilt. So the architecture is familiar ground for me; the Java and Spring AI implementation surface is what I am ramping on, and that is coming fast.

This is early thinking and I am sure there is context I do not have yet. What have you already worked through here, and are there constraints or priorities I should be designing toward? Glad to put a fuller write-up together if that would be useful.

Thanks,
Ryan
