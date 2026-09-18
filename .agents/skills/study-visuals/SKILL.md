---
name: study-visuals
description: Select, create, and validate original visuals for TestingTesting interview-study pages. Use after drafting or revising HLD, LLD, theory, or exercise content when a diagram, architecture view, or conceptual illustration could materially improve understanding.
---

# Study Visuals

## Outcome

Give a first-time reader the smallest accurate visual that answers a question prose cannot answer quickly.
Do not add a visual merely because a page looks sparse or because image generation is available.

## Choose the medium

| Reader need | Use | Do not use |
| --- | --- | --- |
| Exact request, event, retry, state, ownership, or decision flow | Inline Mermaid | Raster image as the source of technical truth. |
| Large static system context, containers, or deployment boundaries | Source-controlled SVG or C4/PlantUML rendered locally to SVG | A crowded Mermaid graph with crossing arrows. |
| Entity/static relationship or lifecycle in LLD | Mermaid class/state/sequence diagram | A decorative architecture image. |
| Intuition, analogy, or a non-technical visual anchor | AI-generated raster illustration | Labels, precise data flow, code, or claims that must be exact. |
| Simple definition or two-option comparison | Prose or a table | Any diagram. |

Use AI raster visuals only when they add intuition that a source-controlled diagram cannot provide. They must
be original, polished, watermark-free, free of fake UI/code/technical labels, and must never be the only
place a correctness-sensitive concept is explained.

Do not introduce a PlantUML/C4 rendering dependency merely for one page. Use Mermaid until a local,
source-controlled renderer is already available or a larger case-study batch justifies the toolchain.

## Workflow

1. Read the page and identify the one question a visual would answer. Check the normal path, update/failure
   path, lifecycle, ownership boundary, and selection decision separately.
2. Reuse a current visual if it already answers that question. Otherwise choose one medium from the table.
3. For Mermaid/SVG/C4, give the visual one job, introduce it in prose, and keep normal and failure outcomes
   visibly distinct. Every arrow needs a real caller, receiver, direction, and outcome.
4. For an AI raster visual, use the built-in image-generation workflow. Keep the prompt generic and never
   send repository, company, client, or unpublished content outside the workspace. Specify: educational
   purpose, composition, visual style, no text, no logos, no watermark, and the exact concepts it may imply.
   Inspect the result before saving it; reject inaccurate, generic-looking, branded, or AI-artifact-heavy
   output and make one targeted revision at a time.
5. Save project-bound assets inside the page module's `assets/` directory only when the module schema permits
   it. Otherwise keep the visual inline as Mermaid; do not create an `assets/` directory that breaks
   navigation validation.
6. Add meaningful Markdown alt text. For a complex technical visual, preserve the equivalent explanation in
   nearby prose.
7. Build the site. With a local preview at the actual documentation-column width, verify Mermaid blocks render
   to SVG, visuals have no console errors, no horizontal overflow, labels can be read without browser zoom, and
   visuals do not duplicate or distract from the nearby explanation. If labels are too small, reduce concepts or
   split the visual; do not call it done merely because it has no overflow.

For a public study page, write down the exact reader question before creating the visual and verify that the rendered
asset answers it in under ten seconds. Use a visual only for one of these jobs: ownership/context, normal path,
failure/recovery path, lifecycle, or strategy comparison. If it cannot answer its declared question independently,
remove it or rebuild it; a labeled component inventory is not an architecture explanation.

## Visual-preservation gate

Before replacing or removing an existing visual, record the reader question it answered and the source concepts it
carried. Replace it only when the new visual or nearby prose covers that same useful material more clearly. If a visual
is unreadable or redundant, remove it without losing its explanatory content; a successful build is not proof that the
learning value survived.

## Quality gates

- A flowchart explains a normal path, a branch, or recovery—not a component inventory.
- A state diagram names valid transitions and the authority that changes state when that matters.
- A sequence diagram distinguishes accepted, failed, and unknown outcomes where retries are possible.
- A broad architecture diagram separates trust, data, and workload ownership; use C4/SVG when one diagram
  would otherwise become crowded.
- An AI image is supplementary and contains no unverified text or technical claim.
- Prefer one strong visual over several overlapping ones. Split only when a reader must understand two
  different questions, such as normal request flow and failure recovery.
- A diagram must fit the rendered reading column, not only its source canvas. Use short labels and one visual
  job; move supporting detail into prose rather than shrinking text to preserve every component.
- At the rendered reading width, labels must be comfortably readable at normal browser zoom (target at least
  12 px). Inspect hierarchy, arrow direction, branch distinction, and scanability in addition to overflow.
- A complex HLD case normally needs different visuals for different reader questions: an ownership/context view
  plus either a normal/failure sequence or a strategy comparison. Do not claim that one dense picture covers all
  of those jobs.

## Scored completion

After rendered review, score visual purpose, technical accuracy, reading-column readability, and relationship
to nearby prose out of 20. A visual is complete only when every applicable dimension is 20/20. Rendering without
overflow is not a high score: the visual needs a stated reader question and reader-verifiable answer. For every lower
score, state the exact deficiency, revise the visual, and add the narrow preventive rule to this skill before the next
page is reviewed. Report the final scorecard and changed rule in the handoff.

## Batch review

For a completed batch, audit each substantive page. Record no change when its existing visual already meets
the reader need. Add or revise only a missing flow, lifecycle, selection, or ownership visual; do not
retrofit decorative imagery across every page.

## Validation

1. Run `node scripts/generate-homepage.js` after asset/module changes.
2. Run `git diff --check`.
3. Run the docs build after any visual change.
4. Run the local browser visual audit when available.
