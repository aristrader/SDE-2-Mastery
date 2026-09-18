# Backend Fundamentals — interview-prep reference site

A VitePress study site for Java backend fundamentals, low-level design, system design, databases,
networking, Spring, and interview practice. It renders the repository's Markdown study material and
keeps runnable Java examples beside the relevant topic.

## Run it

```bash
npm install             # first time
npm run start           # lightweight local site at http://127.0.0.1:5173
npm run start:java      # enable local Java execution
npm run start:lsp       # enable local Java execution and Java language support
npm run end             # stop a site started with npm run start*
npm run docs:build      # static production build → docs/.vitepress/dist
npm run docs:preview    # preview the built site
```

`npm run start` is intentionally lightweight: it does not run Java or start the Java language
server. `start:java` requires a local JDK; `start:lsp` additionally requires `jdtls` on `PATH` (or
`JAVA_LSP_COMMAND` set to its command). `docs:build` produces a static site that can be served as
plain files anywhere.

## Features

- **Generated curriculum navigation** drives the homepage, Curriculum menu, sidebars, and domain
  cards from the same Markdown frontmatter.
- **Topic actions** expose the learning path that exists for a module: Read, Code, Practice,
  Solution, or Design.
- **Interactive code workspaces** keep Java files editable in the browser, retain local edits in
  browser storage, and can compile trusted examples through the local JDK when Java mode is enabled.
- **Study pages** include original diagrams, focused explanations, quick recall sections, local
  search, and structured practice where it is useful.

## Where things live

- `src/main/java/org/example/backend_fundamentals/` — study content; this is the site's `srcDir`.
- `src/main/java/org/example/backend_fundamentals/todo/study_plan/` — learning plan and references.
- `docs/.vitepress/` — site configuration, custom theme, local Java runner/LSP bridge, and generated
  navigation metadata.
- `scripts/` — navigation generation, site lifecycle, and validation helpers.
- `.agents/skills/` — reusable workflows for HLD, LLD, theory, exercises, and study visuals.

## Local Java execution

Run `npm run start:java` before using **Run** in a Code or Practice workspace. The dev server copies
the selected workspace to a temporary directory, compiles it with `javac`, runs the selected `main`
class with `java`, returns output and diagnostics, then removes the temporary directory.

Run only code you trust. Browser edits are local to that browser; they do not modify repository files.
The static build cannot execute Java unless a separate hosted runner is configured.

## Validate changes

```bash
node scripts/generate-homepage.js
npm run docs:build
mvn -q compile
```
