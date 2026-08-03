<template>
  <section class="desk-reset" aria-label="Anytime desk pain reset">
    <p class="desk-reset__rule">
      Quick break: pick <strong>2 to 3</strong> moves from the painful area. If everything feels tight,
      do <strong>1 back + 1 shoulder + 1 neck</strong>. Use Mobility A/B for a fuller session.
    </p>

    <div class="desk-reset__columns">
      <article v-for="group in groups" :key="group.title" class="desk-reset__group">
        <h2>{{ group.title }}</h2>
        <p>{{ group.note }}</p>
        <ul>
          <li v-for="move in group.moves" :key="move.name">
            <div>
              <strong>{{ move.name }}</strong>
              <span>{{ move.dose }}</span>
            </div>
            <button type="button" @click="selected = move">View</button>
          </li>
        </ul>
      </article>
    </div>

    <div v-if="selected" class="desk-reset__overlay" role="presentation" @click.self="selected = null">
      <section
        class="desk-reset__modal"
        role="dialog"
        aria-modal="true"
        :aria-label="selected.name"
      >
        <button type="button" class="desk-reset__close" aria-label="Close preview" @click="selected = null">
          Close
        </button>
        <h2>{{ selected.name }}</h2>
        <p class="desk-reset__dose">{{ selected.dose }}</p>
        <div class="desk-reset__diagram" role="img" :aria-label="`${selected.name} movement diagram`">
          <article v-for="frame in selected.frames" :key="frame.label" class="desk-reset__frame">
            <svg viewBox="0 0 160 150" aria-hidden="true">
              <circle cx="80" cy="34" r="12" class="figure" />
              <path :d="frame.torso" class="figure-line" />
              <path :d="frame.arms" class="figure-line" />
              <path :d="frame.legs" class="figure-line" />
              <path v-if="frame.arrow" :d="frame.arrow" class="arrow" />
              <circle v-if="frame.target" :cx="frame.target[0]" :cy="frame.target[1]" r="13" class="target" />
            </svg>
            <strong>{{ frame.label }}</strong>
            <span>{{ frame.cue }}</span>
          </article>
        </div>
        <ol>
          <li v-for="step in selected.steps" :key="step">{{ step }}</li>
        </ol>
      </section>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'

const selected = ref(null)

const poses = {
  stand: {
    torso: 'M80 48 L80 88',
    arms: 'M54 68 L80 58 L106 68',
    legs: 'M80 88 L60 124 M80 88 L100 124',
  },
  reachBack: {
    torso: 'M80 48 C88 66 88 78 80 94',
    arms: 'M58 75 L78 82 L102 76',
    legs: 'M80 94 L62 124 M80 94 L104 124',
  },
  hinge: {
    torso: 'M80 50 C93 70 108 80 122 88',
    arms: 'M92 72 L130 74 M92 72 L128 90',
    legs: 'M80 88 L60 124 M80 88 L102 124',
  },
  tuck: {
    torso: 'M80 48 L80 90',
    arms: 'M56 70 L80 66 L104 70',
    legs: 'M80 90 L62 124 M80 90 L100 124',
  },
  rotate: {
    torso: 'M80 48 L80 90',
    arms: 'M60 68 L80 62 L108 58',
    legs: 'M80 90 L62 124 M80 90 L100 124',
  },
  sideBend: {
    torso: 'M80 48 C70 62 68 76 74 92',
    arms: 'M55 68 L74 68 L96 58',
    legs: 'M74 92 L58 124 M74 92 L96 124',
  },
  shoulder: {
    torso: 'M80 48 L80 90',
    arms: 'M50 58 C64 50 96 50 110 58',
    legs: 'M80 90 L62 124 M80 90 L100 124',
  },
  retract: {
    torso: 'M80 48 L80 90',
    arms: 'M48 70 C62 62 98 62 112 70',
    legs: 'M80 90 L62 124 M80 90 L100 124',
  },
  doorway: {
    torso: 'M88 48 L88 92',
    arms: 'M88 66 L124 42 M88 66 L124 90',
    legs: 'M88 92 L68 124 M88 92 L108 124',
  },
  wallSlide: {
    torso: 'M80 48 L80 90',
    arms: 'M58 58 L58 28 M102 58 L102 28',
    legs: 'M80 90 L62 124 M80 90 L100 124',
  },
  neck: {
    torso: 'M80 50 L80 92',
    arms: 'M56 70 L80 66 L104 70',
    legs: 'M80 92 L62 124 M80 92 L100 124',
  },
}

function frame(label, pose, cue, arrow = '', target = null) {
  return { label, cue, ...poses[pose], arrow, target }
}

const groups = [
  {
    title: 'Back Relief',
    note: 'Low-key standing options for office breaks.',
    moves: [
      {
        name: 'Walk and reset',
        dose: '1 to 2 min',
        frames: [
          frame('Stand', 'stand', 'Tall posture'),
          frame('Step', 'stand', 'Slow easy walk', 'M58 118 C78 104 88 104 108 118'),
        ],
        steps: ['Stand tall.', 'Walk slowly.', 'Let arms swing and breathe normally.'],
      },
      {
        name: 'Tall-stand glute squeeze',
        dose: '10 reps, 3 sec hold',
        frames: [
          frame('Neutral', 'stand', 'Ribs over hips'),
          frame('Squeeze', 'tuck', 'Glutes on, low back quiet', '', [80, 92]),
        ],
        steps: ['Stand as if waiting for a call.', 'Squeeze both glutes gently.', 'Release without arching the back.'],
      },
      {
        name: 'Standing march',
        dose: '10/side',
        frames: [
          frame('Tall', 'stand', 'Stay upright'),
          frame('March', 'stand', 'Lift knee gently', 'M68 118 C72 88 80 78 92 82'),
        ],
        steps: ['Stand tall near your desk.', 'Lift one knee a little.', 'Switch sides slowly without leaning back.'],
      },
      {
        name: 'Desk hip-flexor shift',
        dose: '20 sec/side',
        frames: [
          frame('Step back', 'stand', 'One foot behind'),
          frame('Shift', 'tuck', 'Front of hip opens', 'M66 96 L92 96', [70, 94]),
        ],
        steps: ['Hold the desk lightly.', 'Step one foot back.', 'Tuck pelvis slightly and shift forward until the front of the hip opens.'],
      },
      {
        name: 'Standing back bends',
        dose: '6 to 8 reps',
        frames: [
          frame('Hands on hips', 'stand', 'Brace lightly'),
          frame('Lean back', 'reachBack', 'Small comfortable arc', 'M91 58 C112 64 116 84 102 98'),
        ],
        steps: ['Place hands on hips.', 'Gently lean chest back.', 'Return to tall standing without forcing the low back.'],
      },
      {
        name: 'Desk hip-hinge stretch',
        dose: '20 sec',
        frames: [
          frame('Hands on desk', 'stand', 'Soft knees'),
          frame('Hinge', 'hinge', 'Spine long, hips back', 'M98 74 L124 74'),
        ],
        steps: ['Hands on desk.', 'Step back and hinge at hips.', 'Keep knees soft and spine long.'],
      },
      {
        name: 'Standing pelvic tilts',
        dose: '8 to 10 reps',
        frames: [
          frame('Neutral', 'stand', 'Find middle'),
          frame('Tilt', 'tuck', 'Tuck gently', 'M67 93 C78 105 90 105 101 93', [80, 90]),
        ],
        steps: ['Hands on hips.', 'Tuck pelvis slightly under.', 'Return to neutral slowly.'],
      },
    ],
  },
  {
    title: 'Shoulder Relief',
    note: 'Use when shoulders round forward or feel heavy.',
    moves: [
      {
        name: 'Shoulder rolls',
        dose: '10 reps',
        frames: [
          frame('Lift', 'shoulder', 'Shoulders up'),
          frame('Back/down', 'retract', 'Roll away from ears', 'M54 54 C80 34 108 54 104 76'),
        ],
        steps: ['Lift shoulders gently.', 'Roll them back.', 'Drop them down away from ears.'],
      },
      {
        name: 'Scapular retractions',
        dose: '12 reps',
        frames: [
          frame('Relaxed', 'stand', 'Ribs down'),
          frame('Retract', 'retract', 'Shoulder blades back/down', '', [80, 66]),
        ],
        steps: ['Keep ribs down.', 'Pull shoulder blades back and slightly down.', 'Release without shrugging.'],
      },
      {
        name: 'Doorway chest stretch',
        dose: '30 sec',
        frames: [
          frame('Set arm', 'doorway', 'Forearm on frame'),
          frame('Step through', 'doorway', 'Chest opens gently', 'M104 76 L122 76', [106, 68]),
        ],
        steps: ['Forearm on door frame.', 'Step through lightly.', 'Keep neck relaxed.'],
      },
      {
        name: 'Wall slides',
        dose: '8 to 10 reps',
        frames: [
          frame('Start', 'retract', 'Back near wall'),
          frame('Slide', 'wallSlide', 'Arms travel up', 'M62 70 L62 34 M98 70 L98 34'),
        ],
        steps: ['Back near wall.', 'Slide arms upward as far as comfortable.', 'Keep shoulders down.'],
      },
      {
        name: 'Desk lat stretch',
        dose: '20 sec/side',
        frames: [
          frame('Hand on desk', 'stand', 'Anchor one hand'),
          frame('Sit back', 'hinge', 'Reach long through side', '', [104, 76]),
        ],
        steps: ['One hand on desk.', 'Sit hips back slightly.', 'Reach long through the side body.'],
      },
    ],
  },
  {
    title: 'Neck Relief',
    note: 'Keep all neck work light and slow.',
    moves: [
      {
        name: 'Chin tucks',
        dose: '8 to 10 reps',
        frames: [
          frame('Forward', 'neck', 'Eyes level'),
          frame('Tuck', 'neck', 'Slide head straight back', 'M99 34 L75 34', [80, 34]),
        ],
        steps: ['Eyes level.', 'Slide head straight back.', 'Make a gentle double chin without looking down.'],
      },
      {
        name: 'Neck rotations',
        dose: '5/side',
        frames: [
          frame('Center', 'neck', 'Sit tall'),
          frame('Turn', 'rotate', 'Rotate slowly', 'M70 30 C88 16 108 24 112 42'),
        ],
        steps: ['Sit or stand tall.', 'Turn head slowly to one side.', 'Return through center and switch.'],
      },
      {
        name: 'Levator stretch',
        dose: '20 sec/side',
        frames: [
          frame('Turn', 'rotate', 'Look toward armpit'),
          frame('Nod', 'sideBend', 'Gentle diagonal stretch', 'M76 34 C66 44 62 54 64 66', [72, 50]),
        ],
        steps: ['Look toward one armpit.', 'Gently nod down.', 'Keep the opposite shoulder relaxed.'],
      },
      {
        name: 'Neck side bend',
        dose: '20 sec/side',
        frames: [
          frame('Tall', 'neck', 'Shoulders low'),
          frame('Side bend', 'sideBend', 'Ear toward shoulder', 'M78 34 C62 38 58 52 60 66', [70, 48]),
        ],
        steps: ['Keep shoulders low.', 'Bring ear gently toward shoulder.', 'Do not pull hard.'],
      },
    ],
  },
]
</script>

<style scoped>
.desk-reset {
  margin-top: 20px;
}

.desk-reset__rule {
  max-width: 980px;
  padding: 14px 16px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 8px;
  background: var(--vp-c-brand-soft);
}

.desk-reset__columns {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-top: 18px;
}

.desk-reset__group {
  border: 1px solid var(--vp-c-divider);
  border-radius: 8px;
  background: var(--vp-c-bg);
}

.desk-reset__group h2 {
  margin: 0;
  padding: 14px 16px 4px;
  border: 0;
  font-size: 1.05rem;
}

.desk-reset__group h2::before {
  display: none;
}

.desk-reset__group p {
  margin: 0;
  padding: 0 16px 10px;
  color: var(--vp-c-text-2);
  font-size: 0.92rem;
  line-height: 1.45;
}

.desk-reset__group ul {
  margin: 0;
  padding: 0;
  list-style: none;
}

.desk-reset__group li {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
  align-items: center;
  padding: 11px 16px;
  border-top: 1px solid var(--vp-c-divider);
}

.desk-reset__group strong,
.desk-reset__group span {
  display: block;
}

.desk-reset__group strong {
  line-height: 1.25;
}

.desk-reset__group span {
  margin-top: 2px;
  color: var(--vp-c-text-2);
  font-size: 0.88rem;
}

.desk-reset button {
  min-width: 64px;
  min-height: 34px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 7px;
  background: var(--vp-c-bg-soft);
  color: var(--vp-c-text-1);
  font: inherit;
  font-weight: 600;
  cursor: pointer;
}

.desk-reset button:hover {
  border-color: var(--vp-c-brand-1);
  color: var(--vp-c-brand-1);
}

.desk-reset__overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: grid;
  place-items: center;
  padding: 20px;
  background: rgba(10, 15, 25, 0.55);
}

.desk-reset__modal {
  position: relative;
  width: min(560px, 100%);
  max-height: min(760px, calc(100vh - 40px));
  overflow: auto;
  border: 1px solid var(--vp-c-divider);
  border-radius: 8px;
  background: var(--vp-c-bg);
  padding: 20px;
  box-shadow: 0 22px 60px rgba(0, 0, 0, 0.28);
}

.desk-reset__modal h2 {
  margin: 0;
  padding-right: 82px;
  border: 0;
  font-size: 1.35rem;
}

.desk-reset__modal h2::before {
  display: none;
}

.desk-reset__dose {
  margin: 4px 0 14px;
  color: var(--vp-c-text-2);
  font-weight: 600;
}

.desk-reset__close {
  position: absolute;
  top: 16px;
  right: 16px;
}

.desk-reset__modal ol {
  margin: 14px 0 0;
  padding-left: 22px;
}

.desk-reset__diagram {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.desk-reset__frame {
  border: 1px solid var(--vp-c-divider);
  border-radius: 8px;
  background: var(--vp-c-bg-soft);
  padding: 10px;
}

.desk-reset__frame svg {
  display: block;
  width: 100%;
  aspect-ratio: 16 / 15;
}

.desk-reset__frame strong,
.desk-reset__frame span {
  display: block;
}

.desk-reset__frame span {
  color: var(--vp-c-text-2);
  font-size: 0.9rem;
}

.figure {
  fill: #dbeafe;
  stroke: #2563eb;
  stroke-width: 5;
}

.figure-line {
  fill: none;
  stroke: #1f2937;
  stroke-width: 8;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.arrow {
  fill: none;
  stroke: #ea580c;
  stroke-width: 5;
  stroke-linecap: round;
  stroke-dasharray: 7 7;
}

.target {
  fill: rgba(22, 163, 74, 0.22);
  stroke: #16a34a;
  stroke-width: 3;
}

@media (max-width: 900px) {
  .desk-reset__columns {
    grid-template-columns: 1fr;
  }
}
</style>
