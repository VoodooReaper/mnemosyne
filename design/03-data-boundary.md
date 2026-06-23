# 03 — Data Boundary & Backend-Agnostic Design (AC4)

**Status:** v0 design-stage. No compile/run claim.
**Date:** 2026-06-22

## Purpose

Two boundaries, kept distinct:
1. **The data wall** — what may and may not enter this design repo (a Mnemosyne governance rule).
2. **The inference boundary** — where the chat model actually runs, and how the architecture stays agnostic to that choice until company policy is confirmed.

## Design

### The data wall (governance, not code)

This repo holds design, architecture, and reference prototypes only. No company data or source ever enters it. The real implementation runs on Ryan's work Anthropic Enterprise seat, against company infrastructure, under company policy. The reference code here uses only placeholder paths, synthetic examples, and abstract config; it never assumes a company system, schema, or secret. (Bishop decision #145; `CLAUDE.md` local rule.)

### The inference boundary — backend-agnostic by construction (TC1)

Company AI-backend policy is unknown at design time, so the architecture must not bake in a provider. Spring AI's `ChatModel` / `ChatClient` abstraction makes the provider a **dependency + config choice**, not a code change: every advisor in `01-architecture.md` operates on the `ChatClient` interface and is indifferent to the model behind it. Swapping backends is swapping a starter and properties.

| Rail | Inference in company tenancy? | Spring AI starter | Notes |
|---|---|---|---|
| AWS **Bedrock** (Converse) | Yes (AWS account + region) | `spring-ai-starter-model-bedrock-converse` | `us.anthropic.claude-*` model IDs; IAM `bedrock:InvokeModel*`; PrivateLink available |
| GCP **Vertex** | Yes (GCP project + region) | `spring-ai-starter-model-vertex-ai-gemini` | Gemini-native; routing *Claude* through Vertex via Spring AI is not native (custom base-url / Anthropic SDK) — **Unverified**, confirm |
| **Anthropic** direct / Enterprise | No (Anthropic US infra) | `spring-ai-starter-model-anthropic` | Governed by API DPA; zero-data-retention is a *negotiated* enterprise option, not automatic |

### The decision the design defers (deliberately)

The choice between "**inference must stay in company-owned cloud**" (-> Bedrock or Vertex) and "**a governed channel/DPA is sufficient**" (-> Anthropic Enterprise) is a **policy question, not an architecture question.** The design supports all three and flags the pick as a confirm-at-work precondition. This is the right call because:

- choosing wrong is expensive to unwind (IAM, networking, contracts),
- the abstraction makes deferral free (no code is wasted by waiting),
- the policy may differ per data classification (some data may require in-tenant compute, some may not).

### What the architecture asserts regardless of backend

- The **memory layers (A/B/S) are backend-independent.** File canon is plain files; the vector store (Mongo Atlas) is its own service; the session store is JDBC. None of them changes when the chat model changes.
- The **hardening gates are backend-independent.** They operate on files and indexes, not on the model.
- Only the **embedding model** for Layer B is a second provider choice (it can differ from the chat model). Keep it configurable alongside the chat backend.

## Deterministic vs model-judgment

- **Deterministic:** backend selection is config; the data wall is a governance rule enforced by review (and by the fact that no company connector exists in this repo).
- **Model-judgment:** none in this layer. Provider choice is policy + config, not a model decision.

## Confirm-at-work / open items

- **The policy question:** must inference stay in-tenant (Bedrock/Vertex), or is the Anthropic Enterprise DPA channel sufficient? Verify the tenancy requirement and the zero-data-retention terms at agreement time.
- **Claude-on-Vertex via Spring AI** routing is Unverified (Vertex Spring AI starter is Gemini-native). If the policy is Vertex + Claude, confirm the integration path before committing.
- The embedding-model provider and whether it must also be in-tenant.

## Sources

- `research/2026-06-22-spring-ai-api-deep-dive.md` Gap 6 (sanctioned backend routing table).
- Bishop decision #145 (Mnemosyne data boundary).
