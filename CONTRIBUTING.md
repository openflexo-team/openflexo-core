# Contributing to openflexo-core

Thanks for your interest in contributing to **openflexo-core**! 🚀
This document explains how to report issues, propose new features, and submit contributions.

---

## Table of contents

* [Who can contribute](#who-can-contribute)
* [How to contribute](#how-to-contribute)

  * [Report a bug](#report-a-bug)
  * [Suggest a feature](#suggest-a-feature)
  * [Submit a pull request](#submit-a-pull-request)
* [Project setup / development environment](#project-setup--development-environment)
* [Running tests](#running-tests)
* [Code style and conventions](#code-style-and-conventions)
* [Review process](#review-process)
* [Code of conduct](#code-of-conduct)
* [Other ways to contribute](#other-ways-to-contribute)
* [Acknowledgements](#acknowledgements)

---

## Who can contribute

Anyone! Contributions are welcome from developers, testers, technical writers, and users.
Every contribution, big or small, helps improve the project.

---

## How to contribute

### Report a bug

If you encounter a bug:

1. Check the [Issues](../../issues) to see if it has already been reported.
2. If not, create a new issue and include:

   * What you expected to happen
   * What actually happened
   * Steps to reproduce the bug
   * Your environment (OS, Java version, dependencies, etc.)

### Suggest a feature

1. Check the [Issues](../../issues) or project roadmap first.
2. When opening a new feature request, please include:

   * A clear description of the feature and its use case
   * Why it would be valuable for openflexo-core
   * (Optional) Example API, mockup, or prototype code

### Submit a pull request (PR)

1. Fork the repository and create a new branch (e.g. `feature/xxx` or `bugfix/xxx`).
2. Make your changes, making sure:

   * Existing tests pass
   * New tests are added if relevant
   * Code follows project conventions (see [Code style](#code-style-and-conventions))
3. Write clear commit messages.
4. Open a pull request:

   * Describe the changes and link to related issues
   * Explain the motivation and context
   * Request a review

---

## Project setup / development environment

* **Requirements**:

  * Java 8
  * Gradle

* **Setup**:

  ```bash
  git clone https://github.com/openflexo/openflexo-core.git
  cd openflexo-core
  ./gradlew build
  ```

* Build artifacts will be generated in `target/`.

---

## Running tests

To run the test suite:

```bash
  ./gradlew testAll
```

Please ensure all tests pass before submitting a PR.
Adding unit tests for new functionality is strongly encouraged.

---

## Code style and conventions

* Follow standard **Java** conventions:

  * 4 spaces indentation
  * `camelCase` for variables and methods, `PascalCase` for classes
  * Constants in `UPPER_CASE`
* Keep code modular and avoid duplication.
* Write Javadoc for public methods and classes.
* Commit messages:
  * Prefix commit message with IMPORTANT / MEDIUM / LOW indicating potential impact of the commit
  * Use imperative mood: `MEDIUM / Fix bug in FlexoModelResource`
  * Reference issues if applicable: `LOW / Fixes #42`

---

## Review process

* PRs are reviewed by maintainers.
* Reviews ensure:
  * Code correctness and quality
  * Test coverage
  * Documentation and comments where needed
* We aim to review PRs within **a few business days**.
* If changes are requested, please update your PR accordingly.

---

## Code of conduct

All contributors are expected to follow the [Code of Conduct](CODE_OF_CONDUCT.md).
Be respectful, inclusive, and constructive in all project interactions.

---

## Other ways to contribute

Not only code matters! You can also help by:

* Improving documentation and tutorials
* Testing the framework in different environments
* Reporting usability issues or edge cases
* Helping newcomers understand the project
* Promoting the project (talks, blogs, social media)

---

## Acknowledgements

Thanks to everyone who helps improve **openflexo-core**:
whether by writing code, fixing docs, reporting bugs, or sharing feedback.

---
