# LMS Architecture Governance Rules

## 1. Schema Enforcement
You MUST adhere strictly to the dual-schema architecture:
**Schema A (Interactive Modules):**
- `/index.md` (Theory, must have `order: X`)
- `/playground/` (Contains only `.java` files, NO `.md` files)
- `/exercise/index.md` (Contains problem statement, `search: false`, `order: 10`)
- `/solution/index.md` (Contains solution, `search: false`, `order: 20`)

**Schema B (System Design / Architecture Modules):**
- `/index.md` (Theory, must have `order: X`)
- `/design/index.md` (Contains architecture diagrams and rubrics, `search: false`, `order: 20`)
- `/exercise/index.md` (Contains scenario, `search: false`, `order: 10`)
- `/assets/` (Images, `.drawio` files)

## 2. Navigation Rules
- **DO NOT MANUAL EDIT `config.mjs` NAV OR SIDEBAR:** Navigation is completely automated by `scripts/generate-homepage.js`.
- If you need to add a new section, create a folder, add an `index.md` with `order: X`, and run `node scripts/generate-homepage.js`.

## 3. Anti-Ghost Folder Rule
- **NEVER CREATE EMPTY FOLDERS OR PLACEHOLDERS:** Do not create a folder unless you are actively populating it with an `index.md`. Empty folders will fatally crash the `generate-homepage.js` build script.
- Ensure all markdown files contain valid YAML frontmatter.

## 4. Playgrounds
- All Java code goes into the `playground/` subdirectory of the module.
- Do not place `Main.java` alongside `index.md` in the root of the module.
