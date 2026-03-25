# Claude Usage Guidelines

This document describes how Claude should be used when contributing to this project.  
Claude is a powerful AI assistant, but it must follow the project’s rules, architecture, and development workflow.

These guidelines ensure that Claude supports the team effectively without introducing inconsistencies or unexpected changes.

---

## Purpose of Claude in This Project

Claude may be used to:

- generate code following project conventions  
- refactor existing code for clarity or performance  
- write documentation or improve existing text  
- assist with tests, edge cases, and error handling  
- explain complex parts of the codebase  
- help with brainstorming or architectural discussions  

Claude **is not** allowed to override project decisions or introduce new technologies without approval.

---

## General Rules

### 1. Claude must follow project conventions
Claude must respect:

- coding style  
- architecture rules  
- naming conventions  
- file structure  
- linting and formatting rules  
- branch and commit guidelines  

If Claude suggests something that violates these rules, the developer must correct it.

---

### 2. Claude must not introduce new dependencies
Claude may **not** add new libraries, frameworks, or tools unless:

- the team explicitly approves it  
- the change is documented and justified  

---

### 3. Claude must not modify architecture without approval
Claude should not:

- reorganize folders  
- rename core components  
- change API structure  
- introduce new patterns (e.g., switching from REST to GraphQL)  

Unless the team has agreed on such changes.

---

### 4. All AI‑generated code must be reviewed
Developers must:

- read and understand the generated code  
- verify correctness  
- ensure security and performance  
- run linting and tests  

Claude is a helper, not an autonomous contributor.

---

## How to Work With Claude Effectively

### Provide clear instructions
Good prompts include:

- context  
- file paths  
- constraints  
- examples  
- expected output format  

**Example:**
> “Claude, refactor this component to reduce duplication. Keep the API unchanged. Follow the project’s ESLint rules.”

---

### Ask Claude to explain its reasoning
This helps validate correctness and catch mistakes early.

**Example:**
> “Explain why you chose this approach and list any potential risks.”

---

### Use Claude for documentation
Claude is excellent at:

- summarizing complex logic  
- generating README sections  
- writing comments  
- improving clarity  

---

## 🧩 Interaction With Other AI Agents

This project may use multiple AI tools (Copilot, Cursor, etc.).  
Claude must follow the unified rules defined in:

**AGENTS.md**

If there is a conflict between tools, **AGENTS.md takes priority**.

---

## Things Claude Must Not Do

Claude must **not**:

- generate code that bypasses security practices  
- commit directly to `main`  
- generate large PRs without explanation  
- remove or rewrite existing logic without context  
- hallucinate APIs or functions that do not exist  
- guess missing requirements  

If Claude is unsure, it must ask for clarification.

---

## Final Notes

Claude is a powerful assistant, but human developers remain responsible for:

- architectural decisions  
- code quality  
- security  
- final approval of changes  

Use Claude to speed up development — not to replace engineering judgment.
