# Memory File Schema (DESIGN-STAGE) — the 4-type curated canon format

> Ports Bishop's memory schema 1:1; identical to Spring AI `AutoMemoryTools`' four typed categories.
> One fact per file. YAML frontmatter + a Markdown body. Indexed by `MEMORY.md` (one line per file).
> No company data here (#145) — examples are synthetic/generic.

## Frontmatter contract (all four types)

```yaml
---
name: <short-kebab-case-slug>          # required; unique; matches MEMORY.md pointer
description: <one-line summary>          # required; used for relevance during recall
metadata:
  type: user | feedback | project | reference   # required; exactly one of the four
---
```

The `MemoryCanonGate` (see `../../hardening/MemoryCanonGate.java`) validates: frontmatter parses,
`name` present + unique, `description` present, `type` is one of the four, and a matching `MEMORY.md`
pointer exists.

## The four types

| Type | Holds | Volatility (freshness gate) |
|---|---|---|
| `user` | Who the user/org is: role, preferences, standing identity | low |
| `feedback` | Standing guidance on how the agent should work; corrections + confirmed approaches (with the why) | medium |
| `project` | Ongoing work, goals, constraints not derivable from code/history (relative dates converted to absolute) | high — expected to change |
| `reference` | Pointers to external resources (URLs, dashboards, tickets) | medium |

## Templates

### `user`
```markdown
---
name: org-support-policy-owner
description: Support policy decisions are owned by the Ops lead, not individual agents.
metadata:
  type: user
---

The Ops lead owns support-policy decisions. Agents propose; the lead approves. Related: [[escalation-path]].
```

### `feedback`
```markdown
---
name: cite-before-asserting
description: Always cite the source row before asserting a policy answer.
metadata:
  type: feedback
---

Cite the source record before stating a policy answer.

**Why:** unsourced policy answers caused two wrong escalations.
**How to apply:** retrieve the canonical record, quote it, then answer. Related: [[org-support-policy-owner]].
```

### `project`
```markdown
---
name: q3-knowledge-base-migration
description: Migrating the KB into the vector store; phase 2 of 3 as of 2026-06-22.
metadata:
  type: project
---

KB migration is in phase 2 of 3 (embedding the archived tickets) as of 2026-06-22. Blocker: index
backfill cadence. Related: [[vector-store-runbook]].
```

### `reference`
```markdown
---
name: vector-store-runbook
description: Runbook URL + dashboard for the Atlas vector index.
metadata:
  type: reference
---

Runbook: <internal-url-placeholder>. Dashboard: <internal-url-placeholder>. Related: [[q3-knowledge-base-migration]].
```

## Wikilinks

Bodies link related facts with `[[name]]` (the other file's `name:` slug). A `[[name]]` that has no
file yet is allowed — it marks a fact worth writing later, not an error.
