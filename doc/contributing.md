# Contributing

Thank you for your interest in contributing to Edmondson! This guide will help you get started.

## Getting Started

For comprehensive development setup instructions, please see:

**[DEVELOPMENT.md](../DEVELOPMENT.md)** - Complete guide covering:
- Development environment setup (local, Docker, Codespaces)
- Running tests
- Building the project
- Using development scripts
- Project structure

For Jupyter-specific development, see [./jupyter.md](./jupyter.md).

## Development Environment

This project assumes familiarity with Clojure development.

### Recommended Setup

Example supported environment:

- [Emacs Prelude](https://prelude.emacsredux.com/en/latest/) (contains CIDER and clojure-mode)
- Clojure CLI / tools.deps / [cider-nrepl](https://github.com/clojure-emacs/cider-nrepl)
- `cider-jack-in` command in Emacs (`C-u M-x cider-jack-in`):

        /usr/local/bin/clojure -Sdeps '{:deps {nrepl {:mvn/version "0.8.3"} cider/cider-nrepl {:mvn/version "0.25.5"}}}' -m nrepl.cmdline --middleware '["cider.nrepl/cider-middleware"]'

### Alternative IDEs

The project works with any Clojure-compatible development environment:
- IntelliJ IDEA + Cursive
- VS Code + Calva
- Vim/Neovim + vim-fireplace
- Any editor with nREPL support

## Making Contributions

1. **Fork and clone** the repository
2. **Set up your development environment** (see [DEVELOPMENT.md](../DEVELOPMENT.md))
3. **Create a branch** for your changes
4. **Make your changes** and add tests if applicable
5. **Run tests** to ensure everything works: `clojure -X:test`
6. **Submit a pull request** with a clear description of your changes

## Code Style

- Follow standard Clojure style conventions
- Use meaningful variable and function names
- Add docstrings to public functions
- Keep functions focused and concise

## Testing

Please add tests for new functionality. Tests should be placed in `src/test/clojure/` following the pattern `<namespace>-test.clj`.

Run tests with:
```bash
clojure -X:test
```

## Questions?

If you have questions or need help, please open an issue on GitHub.

