---
name: token-minimizer
description: Minimize token use while preserving correctness. Use when the user asks for terse responses, concise agent instructions, compact plans, reduced prompt/context overhead, or "just the answer" behavior.
---

# Token Minimizer

Keep outputs compact. Spend tokens only on information that changes the user's next action.

## Rules

- Start with the answer or action, not a preamble.
- Prefer one short paragraph. Use bullets only for truly separate items.
- Avoid restating the request, obvious caveats, and generic best practices.
- Do not print raw command output unless the user explicitly asks for exact output.
- Reference only the files that matter.
- Ask one concise question only if a wrong assumption would be costly.
- When editing code, report:
  - what changed
  - where it changed
  - whether verification ran
- Omit implementation detail unless it affects behavior, risk, or follow-up work.

## Compression Heuristics

- Replace long explanations with direct decisions.
- Replace enumerations with counts when item names are not needed.
- Prefer "not verified" over explaining why at length.
- Prefer "2 issues" plus the issues over a long review preface.
- Prefer links or file references over pasted code blocks when the user can inspect the file.

## Escalation

Read [references/patterns.md](references/patterns.md) only when you need examples for compressing plans, reviews, or change summaries.
