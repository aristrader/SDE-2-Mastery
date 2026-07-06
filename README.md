# SDE-2 Mastery — study reference site

A clean, fast reference site over this repo's study material (`.md`) and Java implementations
(`.java`): read the theory, browse the code next to it, and run editable Java examples in local
study mode.

## Run it

```bash
npm install          # first time
npm run dev          # http://localhost:5173  (VitePress dev server)
npm run docs:build   # static production build → docs/.vitepress/dist
npm run docs:preview # preview the built site
```

`docs:build` produces a static site that can be served as plain files anywhere. Local Java
execution is available only through the VitePress dev server because it uses the local JDK.

## Features

- **Generated curriculum navigation** powers the homepage, Curriculum dropdown, sidebars, and
  domain hub cards from the same `order` frontmatter.
- **Read / Code tabs** appear on any page whose folder (or a subfolder) contains `.java`.
  - *Read*: the doc — rendered Mermaid diagrams (click to zoom), styled tables, clean typography.
  - *Code*: an editable browser of that folder's Java files (grouped by subfolder;
    `*Practice.java` flagged as **exercise**), syntax-highlighted with a local Run button.
- **Hub pages** (Java, Design Patterns, etc.) as card grids; colored domain eyebrows; local search.

## Where things live

- `docs/.vitepress/` — `config.mjs`, `theme/Layout.vue`, `theme/components/{Playground,CodeEditor}.vue`,
  `theme/lib/fileDiscovery.mjs`, `theme/custom.css`.
- Study content — `src/main/java/org/example/backend_fundamentals/` (the site's `srcDir`).
- Design/plans — `docs/superpowers/specs/` and `docs/superpowers/plans/`.

## Running Java From The Site

During `npm run dev` / `npm run docs:dev`, the Code tab posts to `/api/run-java`. The dev server
copies the edited playground files into a temporary directory, compiles them with `javac`, runs the
selected `main` with `java`, returns stdout/stderr, and deletes the temp directory. Do not run
untrusted code.

The built static site cannot execute Java unless a separate hosted runner is configured.
