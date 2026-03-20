# Repo Working Rules

## External AI Suggestions

The user may use external AI tools such as Trae or other assistants for debugging, design, review, or implementation ideas.

- Treat Trae or any other AI suggestion as a hypothesis, not as ground truth.
- Never apply an external AI suggestion blindly.
- First restate the suggestion in concrete technical terms.
- Then verify it against the current codebase, product requirements, runtime behavior, and test results.
- If only part of a suggestion is valid, keep the valid part and explicitly revise or reject the rest.
- Prefer evidence over confidence.

When the user says "AI said..." or "Trae suggested..." or similar:

1. Inspect the relevant code paths directly.
2. Reproduce the issue or validate the assumption where practical.
3. Check whether the suggestion fits this repository's architecture and constraints.
4. Adopt only the parts that are actually correct and beneficial.
5. Summarize what was accepted, what was rejected, and why.

## Planning Research Expectations

When producing implementation plans, architecture proposals, or major refactor directions, do not design in isolation.

- Research existing solutions on the internet before finalizing a plan, unless the user explicitly says not to browse.
- Prioritize GitHub repositories, official documentation, and strong technical writeups.
- Look for similar projects, especially those with comparable product scope, architecture, or platform constraints.
- Extract useful patterns, tradeoffs, and failure modes from those references.
- Adapt ideas to this repository instead of copying them mechanically.

For meaningful planning work:

1. Search for a few relevant external references.
2. Prefer at least one GitHub implementation when available.
3. Compare the references briefly before choosing a direction.
4. Mention the external influences when they materially shaped the plan.

## Scope Guidance

- These rules are repository defaults and should be followed for future work in this project.
- If these behaviors later become cross-project habits with richer workflows, they can be promoted into reusable skills.
