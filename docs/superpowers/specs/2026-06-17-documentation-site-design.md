# SDE-2 Mastery: Site Layout & Execution Design

## 1. Overview
This spec defines the architecture for transforming the VitePress documentation site from a simple markdown reader into an interactive, structured learning platform. It addresses navigation clutter and enables in-browser execution of local Java implementation files.

## 2. Navigation Architecture: Hub Pages + Contextual Sidebars
To prevent sidebar bloat, the site will use a hybrid navigation strategy:

* **Top Navigation:** Defines the core domains (e.g., Study Plan, System Design, Design Patterns).
* **Hub Pages (Landing Pages):** Clicking a top-level domain opens a full-width Hub Page with **no sidebar** (`sidebar: false`). These pages act as study guides, using a visual grid of clickable cards ordered chronologically by learning path.
* **Contextual Sidebars:** Once a user clicks into a specific topic from the Hub Page (e.g., entering the "Singleton" lesson), a specialized sidebar appears. This sidebar is scoped *only* to the current domain, allowing easy jumping between related topics without overwhelming the UI.
* **Flexibility:** Sidebars can be toggled off per-page using VitePress frontmatter (`sidebar: false`) if a pure full-width experience is desired later.

## 3. Interactive Code Execution: `<CodePlayground>`
Since implementation files (`.java`) live alongside the theory files (`.md`), we will build an "Embedded IDE" component to bridge them natively.

### 3.1 Component Design
A reusable Vue component (`<CodePlayground />`) dropped at the bottom of any markdown file.
* **Auto-Discovery:** Uses Vite's `import.meta.glob('./*.java', { as: 'raw' })` to automatically discover, load, and render any Java files living in the exact same directory as the markdown file.
* **Split-Pane UI:**
  * **Left Pane (Explorer):** A list of discovered `.java` files.
  * **Right Pane (Editor):** Syntax-highlighted display of the selected file's raw code.
  * **Top Right (Action):** A prominent "Run Code Locally" button.
  * **Bottom Pane (Terminal):** A collapsible console window displaying `stdout`, `stderr`, and exit statuses.

### 3.2 Execution Data Flow
1. User clicks "Run Code Locally" for a selected Java file.
2. The component parses the `package ...;` declaration from the raw Java file content to automatically construct the Fully Qualified Class Name (FQCN).
3. The component sends a `POST /api/run-java` request containing the FQCN.
4. The custom Vite backend plugin (already hooked into `config.mjs`) receives the request.
4. The plugin executes `mvn -q compile && mvn -q exec:java -Dexec.mainClass="<FQCN>"` directly on the host machine using Node's `child_process.exec`.
5. The raw output is streamed back to the browser and displayed in the terminal UI.

## 4. Testing & Maintenance
* No external dependencies or online execution environments (e.g., JDoodle) are used. Everything remains entirely local and secure.
* If directories are refactored, the component automatically adapts since it uses relative directory globs.
