# Contributing

See [./jupyter.md](./jupyter.md) and follow the instructions for local or Codespaces development.

## Development Environment
This assumes familiarity with Clojure development.

Example supported environment:

- [Emacs prelude](https://prelude.emacsredux.com/en/latest/) (contains CIDER and
  clojure-mode)
- Clojure CLI / tools.deps /
  [cider-nrepl](https://github.com/clojure-emacs/cider-nrepl) / 
- `cider-jack-in` command in emacs (`C-u M-x cider-jack-in`): 

        /usr/local/bin/clojure -Sdeps '{:deps {nrepl {:mvn/version "0.8.3"} cider/cider-nrepl {:mvn/version "0.25.5"}}}' -m nrepl.cmdline --middleware '["cider.nrepl/cider-middleware"]'

