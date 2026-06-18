# SDE-2 Mastery — study reference site

A clean, fast **static** reference site over this repo's study material (`.md`) and its Java
implementations (`.java`): read the theory, browse the code next to it. It's a reader — there's
**no backend, no code execution, and no AI** (run the examples in your IDE).

## Run it

```bash
npm install          # first time
npm run dev          # http://localhost:5173  (VitePress dev server)
npm run docs:build   # static production build → docs/.vitepress/dist
npm run docs:preview # preview the built site
```

It's a pure static site — nothing runs on your machine beyond the dev/preview server, and the
built `dist/` can be served as plain files anywhere.

## Features

- **Read / Code tabs** appear on any page whose folder (or a subfolder) contains `.java`.
  - *Read*: the doc — rendered Mermaid diagrams (click to zoom), styled tables, clean typography.
  - *Code*: a **read-only** browser of that folder's Java files (grouped by subfolder;
    `*Practice.java` flagged as **exercise**), syntax-highlighted. To run them, open the project
    in IntelliJ.
- **Hub pages** (Java, Design Patterns, etc.) as card grids; colored domain eyebrows; local search.

## Where things live

- `docs/.vitepress/` — `config.mjs`, `theme/Layout.vue`, `theme/components/{Playground,CodeEditor}.vue`,
  `theme/lib/fileDiscovery.mjs`, `theme/custom.css`.
- Study content — `src/main/java/org/example/backend_fundamentals/` (the site's `srcDir`).
- Design/plans — `docs/superpowers/specs/` and `docs/superpowers/plans/`.

## Running the Java

The site shows the code for reference only. To compile/run an example, open the Maven project in
IntelliJ (or `mvn -q exec:java -Dexec.mainClass="<FQCN>"` from the repo root) as before.
