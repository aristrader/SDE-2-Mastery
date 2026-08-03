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
      <section class="desk-reset__modal" role="dialog" aria-modal="true" :aria-label="selected.name">
        <button type="button" class="desk-reset__close" aria-label="Close preview" @click="selected = null">
          Close
        </button>
        <h2>{{ selected.name }}</h2>
        <p class="desk-reset__dose">{{ selected.dose }}</p>
        <img class="desk-reset__photo" :src="selected.image" :alt="`${selected.name} reference image`" />
        <a class="desk-reset__source" :href="selected.source" target="_blank" rel="noreferrer">
          Open image source
        </a>
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
const lcsd = 'https://www.lcsd.gov.hk/en/healthy/common/graphics/exercise/'
const lcsdSource = 'https://www.lcsd.gov.hk/en/healthy/exercise/exercise2.html'

const groups = [
  {
    title: 'Back Relief',
    note: 'Office-friendly moves with confirmed matching reference images.',
    moves: [
      {
        name: 'Standing knee lift',
        dose: '8/side',
        image: `${lcsd}27.jpg`,
        source: lcsdSource,
        steps: ['Stand tall and hold the desk lightly.', 'Lift one knee as high as comfortable.', 'Stretch the same leg slightly back, then switch sides.'],
      },
      {
        name: 'Standing waist extension',
        dose: '6 to 8 reps',
        image: `${lcsd}30.jpg`,
        source: lcsdSource,
        steps: ['Stand tall with hands on the back of your waist.', 'Push hips and waist slightly forward.', 'Keep neck and upper body controlled; do not over-lean backward.'],
      },
      {
        name: 'Wall back press',
        dose: '8 to 10 reps',
        image: `${lcsd}32.jpg`,
        source: lcsdSource,
        steps: ['Stand with upper back near a wall.', 'Draw abdomen in slightly.', 'Press the back gently toward the wall, then return to neutral.'],
      },
      {
        name: 'Upper body rotation',
        dose: '4/side',
        image: `${lcsd}23.jpg`,
        source: lcsdSource,
        steps: ['Stand with feet apart and knees soft.', 'Turn upper body slowly to one side.', 'Return through center and switch sides.'],
      },
      {
        name: 'Standing side bend',
        dose: '20 sec/side',
        image: `${lcsd}25.jpg`,
        source: lcsdSource,
        steps: ['Stand tall, optionally near a wall.', 'Raise one arm.', 'Bend trunk slowly to the opposite side, then switch.'],
      },
      {
        name: 'Seated hamstring hinge',
        dose: '20 sec/side',
        image: `${lcsd}35.jpg`,
        source: lcsdSource,
        steps: ['Sit at the edge of a chair.', 'Straighten one leg with heel down.', 'Hinge forward with a straight back, then switch sides.'],
      },
    ],
  },
  {
    title: 'Shoulder Relief',
    note: 'Use when shoulders round forward or feel heavy.',
    moves: [
      {
        name: 'Shoulder rolls',
        dose: '8 each way',
        image: `${lcsd}05.jpg`,
        source: lcsdSource,
        steps: ['Bring shoulders forward and inward.', 'Lift shoulders and rotate toward the back.', 'Repeat the other direction.'],
      },
      {
        name: 'Shoulders back and forth',
        dose: '8 reps',
        image: `${lcsd}14.jpg`,
        source: lcsdSource,
        steps: ['Bring shoulders forward and inward.', 'Return to neutral.', 'Bring shoulders backward, then relax.'],
      },
      {
        name: 'Doorframe arm stretch',
        dose: '20 to 30 sec',
        image: `${lcsd}18.jpg`,
        source: lcsdSource,
        steps: ['Hold the doorframe with both hands.', 'Step one leg forward.', 'Lean forward gently without forcing the shoulders.'],
      },
      {
        name: 'Chest lift with arms behind',
        dose: '8 reps',
        image: `${lcsd}16.jpg`,
        source: lcsdSource,
        steps: ['Stand tall with fingers interlocked behind your back.', 'Raise arms slowly.', 'Bring shoulders backward without leaning forward.'],
      },
      {
        name: 'Elbow-pull shoulder stretch',
        dose: '20 sec/side',
        image: `${lcsd}06.jpg`,
        source: lcsdSource,
        steps: ['Rest one arm across the opposite shoulder.', 'Pull the elbow lightly toward the body.', 'Switch sides.'],
      },
    ],
  },
  {
    title: 'Neck Relief',
    note: 'Keep all neck work light and slow.',
    moves: [
      {
        name: 'Neck rotations',
        dose: '4/side',
        image: `${lcsd}02.jpg`,
        source: lcsdSource,
        steps: ['Look forward.', 'Turn neck slowly to one side.', 'Return through center and switch sides.'],
      },
      {
        name: 'Neck side stretch',
        dose: '20 sec/side',
        image: `${lcsd}03.jpg`,
        source: lcsdSource,
        steps: ['Look forward.', 'Stretch neck slowly to one side.', 'Return to center and switch sides.'],
      },
      {
        name: 'Looking up and down',
        dose: '4 reps',
        image: `${lcsd}04.jpg`,
        source: lcsdSource,
        steps: ['Draw chin gently toward the neck.', 'Lower the head.', 'Return to center, then lean back only slightly.'],
      },
      {
        name: 'Arm and neck stretch',
        dose: '20 sec/side',
        image: `${lcsd}09.jpg`,
        source: lcsdSource,
        steps: ['Stand tall.', 'Hold one wrist near the opposite waist or hip.', 'Stretch neck gently away from that side, then switch.'],
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

.desk-reset__photo {
  display: block;
  width: 100%;
  max-height: 420px;
  object-fit: contain;
  border: 1px solid var(--vp-c-divider);
  border-radius: 8px;
  background: var(--vp-c-bg-soft);
}

.desk-reset__source {
  display: inline-block;
  margin-top: 8px;
  font-size: 0.9rem;
}

.desk-reset__modal ol {
  margin: 14px 0 0;
  padding-left: 22px;
}

@media (max-width: 900px) {
  .desk-reset__columns {
    grid-template-columns: 1fr;
  }
}
</style>
