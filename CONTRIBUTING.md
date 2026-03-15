# Contributing Guidelines

Thank you for your interest in contributing to this project. The following guidelines help keep the codebase consistent, the workflow smooth, and collaboration effective. Please read these instructions before creating a new branch or opening a pull request.

---

## Getting Started
### 1. Clone the repository
```bash
git clone git@github.com:Team-Caffeine-ACS/acs-backend.git
cd acs-backend
```

## Branches
- Branch naming format: ` <prefix>/<ticket id>-<name of developer>-<brief explanation of what we do in branch> `.
- There are two types of branches: main and development branch. Pushing directly to main is prohibited, 
  instead work shall be done in development branch, reviewed and merged into main with Pull Request.
  Type of development branch is defined with prefix before the name of branch.
- Prefixes used in project and their explanation:
  | Branch pattern | Description                                        |
  |--------------- |----------------------------------------------------|
  | `main`         | Main development branch                            |
  | `feature/*`    | Uus funktsionaalsus                                |
  | `bugfix/*`     | Bugi parandus                                      |
  | `hotfix/*`     | Kiire ja kriitiline parandus                       |
  | `chore/*`      | Tehniline hooldus, refactor, config, cleanup       |
  | `refactor/*`   | Koodi ümberkorraldus ilma funktsionaalse muutuseta |
  | `docs/*`       | Dokumentatsioon                                    |
  | `test/*`       | Testide lisamine/parandamine                       |
  | `ci/*`         | CI/CD, GitHub Actions, pipeline                    |
  | `build/*`      | Build-süsteem, dependency’d                        |
  > Where "*" is remaining branch name.
- Branch names should clearly describe the job that is being done in that branch.

## Commits
- Commit messages must clearly describe what was changed in that commit
- There shouldn't be too many changes in one commit, for more transparent development. 
  (More often commits but smaller is better, than rarer ones and bigger)
- Commits shall be regularly pushed to GitHub, making the developing more transparent.

## Pull Request
- Are reviewed at least by one person
- Review comments shall be clear and fully describing the point of reviewer. 
  There shall not be unclear comments, they should be technically full and respectful.
- Are merged once all review comments are resolved, and all requiremens are satisfied.
- Pull Request is merged only by it's creator.
