---
name: study-visuals
description: Select, create, and validate original visuals for TestingTesting interview-study pages. Use after drafting or revising HLD, LLD, theory, or exercise content when a diagram, architecture view, or conceptual illustration could materially improve understanding.
---

# Study Visuals

For substantial public study-content work, follow [efficient study execution](references/efficient_execution.md).

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

State the reader question first. Reuse a current visual if it answers it; otherwise choose one medium above. Every
arrow needs a real caller, receiver, direction, and outcome; make normal and failure paths distinct. Use AI raster
only for non-technical intuition, with a generic no-text/no-logo/no-watermark prompt. Keep assets in a valid module
only, preserve nearby prose/alt text, and accept the rendered visual only when it answers its question in under ten
seconds at normal reading width.

## Visual-preservation gate

Before replacing or removing an existing visual, record the reader question it answered and the source concepts it
carried. Replace it only when the new visual or nearby prose covers that same useful material more clearly. If a visual
is unreadable or redundant, remove it without losing its explanatory content; a successful build is not proof that the
learning value survived.

## Acceptance

Use one strong visual rather than overlapping ones. A flowchart explains a path or recovery, a state diagram names
valid transitions, and an architecture view shows ownership—not inventories. Keep labels at least roughly 12 px at
desktop reading width; split crowded diagrams. A complex HLD case normally needs an ownership view plus a distinct
flow/recovery or comparison view. The shared contract owns scoring and validation.
