# Mnemosyne

Design-and-build workspace for Ryan Gonzales's Winsupply AI work: a persistent, multi-domain, long-term-memory layer for AI agents built on **Spring AI** (Java), and the multi-department enterprise agent that grows on top of it.

Named for Mnemosyne, the Greek goddess of memory. Long-term memory is the heart of the work.

## The bet

Spring AI's official "Agentic Patterns" (Agent Skills, AutoMemoryTools, Session API, Subagents, A2A) describe, pattern for pattern, the architecture Ryan already built and hardened in **Bishop**. The framework's long-term-memory tool (`AutoMemoryTools`) is near-identical to Bishop's memory system. So this project is not "learn to build an agent." It is **port a proven, battle-tested architecture onto the enterprise stack, and add the production hardening the tutorials omit** (quarantine-before-canon, deterministic freshness gates, a second vector-recall layer for semantic search at scale).

## Hard boundary (read this)

This is a **personal, Bishop-managed design repo**. It holds architecture, research, specs, and learning ONLY.

- **No company data. No company source code. Ever.** Not in files, not in commits, not in any model prompt run from this repo.
- The real implementation runs on Ryan's **work Anthropic Enterprise seat** against company infrastructure, governed by company policy.
- This repo is the brain (design + decisions). The work seat is the hands (implementation on real systems).

## Structure

- `PROJECT.md` — canonical state-of-the-project. Load this first.
- `research/` — research briefs (the CRPE-R Research phase outputs).
- `wiki/` — synthesized knowledge docs.
- `CLAUDE.md` — thin seam so a Claude Code session rooted here loads Bishop.

## Status

Phase: **Research** (CRPE-R). Concept locked 2026-06-22. Next gate: the gate-1 grill to design the long-term-memory architecture.
