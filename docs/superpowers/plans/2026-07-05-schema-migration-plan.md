# Curriculum Schema Migration Plan

> **Goal:** Migrate the entire legacy repository to the new strict LMS Architecture (Schema A and Schema B) meticulously, one step at a time, to avoid hallucinations and data loss. Do not use parallel subagents.

## Phase 0: Branching & Sanitization
*Isolate the migration work from the main Git history and setup safe git ignores.*
- [ ] Create branch `chore/schema-migration`.
- [ ] **Git Hygiene:** Ensure the root `.gitignore` explicitly ignores `docs/.vitepress/dist`, `docs/.vitepress/cache`, and `docs/.vitepress/navigation_map.json` so we don't accidentally commit thousands of generated HTML and JSON files to the repository.
- [ ] **Sanitization:** Ensure all legacy folders conform to regex `^[a-zA-Z0-9_-]+$` before validation scripts run. Scan and rename any legacy folders that contain spaces, periods, or special characters.
- [ ] Ensure the main validation script (`generate-homepage.js`) is completed and successfully executes the structural requirements.
- [ ] **Commit Strategy:** Make iterative, atomic commits at the end of each Phase (or folder). DO NOT use a single "God Commit" during migration, as a mid-phase failure will require a hard reset that wipes all prior phases. Once the migration is complete and tested, squash all iterative commits when merging back to `main`.

## Phase 1: Root & Domain Level
*Ensure every top-level domain folder has an `index.md` with an `order` tag.*
- [x] Migrate `/java/index.md` *(Completed)*
- [x] Migrate `/spring/index.md`
- [x] Migrate `/spring_boot/index.md`
- [x] Migrate `/system_design/index.md`
- [x] Migrate `/design_patterns/index.md`
- [x] Migrate `/databases/index.md`
- [x] Migrate `/networking/index.md`
- [x] Migrate `/messaging/index.md`
- [x] Migrate `/security/index.md`
- [x] Migrate `/engineering_practice/index.md`
- [x] Migrate `/deployments/index.md`
- [x] Migrate `/performance/index.md`

## Phase 2: Category Level (Java & Spring)
*Ensure all intermediate categories have an `index.md` with an `order` tag.*
- [x] Migrate `/java/oop/index.md` *(Completed)*
- [x] Migrate `/java/collections/index.md`
- [x] Migrate `/java/concurrency/index.md`
- [x] Migrate `/java/coding_fluency/index.md`
- [x] Migrate `/spring/spring_core/index.md`
- [x] Migrate `/spring/spring_mvc/index.md`
- [x] Migrate `/spring/spring_data_jpa/index.md`
- [x] Migrate `/spring/spring_security/index.md`

## Phase 3: Module Level (Java - Schema A)
*Convert all Java topics into Schema A (index.md, playground/, exercise/, solution/, assets/).*
***Critical Rule 1:** MUST use `git mv` instead of `mv`.*
***Critical Rule 2:** Immediately update the `package` declarations and `import` statements inside the `.java` files to reflect their new `playground` directory paths to prevent `mvn compile` failures.*
- [x] Migrate `java/oop/*` *(Completed & Packages Patched)*
- [x] Migrate `java/collections/*`
- [x] Migrate `java/concurrency/*`
- [x] Migrate `java/coding_fluency/*`
- [x] Migrate `java/jvm/*`

## Phase 4: Module Level (Spring - Schema A)
*Convert all Spring topics into Schema A.*
***Critical Rule 1:** MUST use `git mv` to preserve history.*
***Critical Rule 2:** Immediately update `package` and `import` statements.*
***Critical Rule 3:** You MUST update fully qualified class names in `application.yml`, XML contexts, or `@ComponentScan` configurations that point to these moved beans.*
- [x] Migrate `spring/spring_core/*`
- [x] Migrate `spring/spring_mvc/*`
- [x] Migrate `spring/spring_data_jpa/*`
- [x] Migrate `spring/spring_security/*`
- [x] Migrate `spring_boot/*`

## Phase 5: Module Level (System Design - Schema B)
*Convert System Design into Schema B (index.md, design/, exercise/, assets/).*
- [x] Migrate `system_design/foundations/*`
- [x] Migrate `system_design/components/*`
- [x] Migrate `system_design/architectures/*`

## Phase 6: Theory Domains (Schema A - Theory Only)
*Convert theory domains (no code/playgrounds needed, just `index.md` and empty `exercise` folders).*
***Critical Rule:** Git ignores empty folders. You MUST create empty `exercise/index.md` and `solution/index.md` files (with `order: X`) in every topic to satisfy the automation scripts and ensure they track in Git.*
- [x] Migrate `design_patterns/*`
- [x] Migrate `databases/*`
- [x] Migrate `networking/*`
- [x] Migrate `messaging/*`
- [x] Migrate `security/*` (General security, not Spring Security)
- [x] Migrate `engineering_practice/*`
- [x] Migrate `deployments/*`
- [x] Migrate `performance/*`

## Phase 7: Global Link & References Reconciliation
*Fix all broken markdown links and cross-references.*
- [x] **Curriculum Links:** Search and update all `.md` files in `todo/study_plan/parts/` and `todo/study_plan/phases/` to point to the new Schema A/B URLs.
- [x] **Static Assets (CRITICAL):** You MUST move all `.png`, `.jpg`, and `.svg` files from their legacy folders into the new `assets/` directory of their respective module. Do NOT leave images orphaned in legacy directories, or the legacy directories can never be safely deleted. Update all markdown `![image](path)` links to point to the new local `assets/` folder.
- [x] **Code Snippets & Line Numbers:** If markdown files import code snippets via VitePress (`<<< @/path/to/file.java`) or link to specific line numbers, these must be updated. *Warning: Updating package/import statements in Java files shifts line numbers down, which will break existing line number references!*
- [x] **Java Imports:** Run a final `mvn clean compile` to catch any cross-package Java `import` statements that were broken during the folder moves and patch them.

## Phase 8: Legacy Directory Cleanup (Ghost Folders)
*Remove the empty husks of the legacy architecture to prevent validation crashes.*
- [x] **Sanitize Untracked Files:** Ensure no stray `.DS_Store`, `Thumbs.db`, or random `.txt` notes were left behind in the old folder structures.
- [x] **Legacy Deletion:** Run a cleanup to delete all empty legacy directories. Check `design_patterns/foundations/oop_pillars/` in particular to make sure it's gone.
- [x] **Validation Script Enablement:** Will be done in Automation Plan Task 1.ld.
