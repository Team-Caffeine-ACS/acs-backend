# AI Agent Guidelines

This document defines how all AI coding agents (Claude, GitHub Copilot, Cursor, Continue, etc.) should behave when contributing to this project.  
The goal is to ensure consistency, maintainability, and architectural integrity across all AI-assisted work.

These rules apply to **every AI tool** used in this repository.

---

## Purpose of AI Agents

AI agents may be used to:

- generate code that follows project conventions  
- refactor existing code for clarity, performance, or maintainability  
- write or improve documentation  
- assist with tests, edge cases, and error handling  
- explain complex logic or architecture  
- support developers during implementation  

AI agents **do not replace engineering judgment**.  
Human developers are responsible for reviewing and approving all changes.

---

## Core Principles

### 1. Follow project rules and conventions
AI agents must respect:

- coding style  
- linting and formatting rules  
- file and folder structure  
- naming conventions  
- architectural decisions  
- commit message standards  
- branch naming conventions  
- security practices  

If unsure, the agent must ask for clarification instead of guessing.

---

### 2. Do not introduce new dependencies
AI agents must **not** add new libraries, frameworks, or tools unless:

- the team explicitly approves it  
- the change is documented and justified  

Examples of disallowed actions:

- adding a new UI library  
- switching state management tools  
- introducing new build or runtime dependencies  

---

### 3. Do not modify architecture without approval
AI agents must not:

- reorganize folders  
- rename core components  
- change API structure  
- introduce new patterns (e.g., switching from REST to GraphQL)  
- rewrite large parts of the codebase  

Unless the team has explicitly agreed on such changes.

---

### 4. All AI-generated code must be reviewed
Developers must:

- read and understand the generated code  
- verify correctness  
- ensure security and performance  
- run linting and tests  
- confirm that the code aligns with project goals  

AI output is a suggestion, not an authoritative source.

---

## Interaction Between Multiple AI Agents

This project may use several AI tools simultaneously.  
To avoid conflicts:

- **AGENTS.md is the single source of truth**  
- If two agents disagree, follow this document  
- If an agent proposes changes that violate these rules, the developer must override it  

Agents should not attempt to “correct” each other unless explicitly asked.

---

## Allowed Actions

AI agents **may**:

- generate new components that follow existing patterns  
- refactor code without changing behavior  
- improve readability and maintainability  
- write documentation or comments  
- propose optimizations with clear reasoning  
- generate tests for new or existing code  
- help with debugging or error analysis  
- summarize or explain complex logic  

---

## Forbidden Actions

AI agents must **not**:

- hallucinate APIs, functions, or modules that do not exist  
- bypass security or validation logic  
- generate large PRs without explanation  
- commit directly to `main`  
- remove or rewrite logic without context  
- guess missing requirements  
- introduce breaking changes without approval  
- generate code that fails linting or formatting rules  

---

## Prompting Guidelines for Developers

To get the best results from AI agents:

- provide context (file paths, architecture, constraints)  
- specify the expected output format  
- ask for reasoning when needed  
- request small, incremental changes  
- avoid vague or open-ended prompts  

**Good example:**
> “Refactor this component to reduce duplication. Keep the API unchanged. Follow the project’s ESLint rules.”

**Bad example:**
> “Rewrite this however you want.”

---

## 📄 Relationship With Other Documentation

AI agents must follow:

- `CONTRIBUTING.md`  
- `README.md`  
- `CLAUDE.md` (Claude-specific rules)  
- project architecture documentation  
- coding standards defined by the team  

If any document conflicts with another, the priority is:

1. **AGENTS.md**  
2. CONTRIBUTING.md  
3. Architecture documentation  
4. README.md  

---

## Final Notes

AI agents are powerful tools that can significantly speed up development, but they must operate within the boundaries of this project’s standards.

Human developers remain responsible for:

- architectural decisions  
- code quality  
- security  
- final approval of changes  

Use AI to enhance productivity — not to replace engineering judgment.
