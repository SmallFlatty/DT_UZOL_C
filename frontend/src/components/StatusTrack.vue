<script setup lang="ts">
import { computed } from 'vue'
import { STATUSES, STATUS_LABEL, type ParcelStatus } from '@/types'

const props = defineProps<{ status: ParcelStatus; compact?: boolean }>()

const current = computed(() => STATUSES.indexOf(props.status))
</script>

<template>
  <div class="track" :class="{ 'track--compact': compact }" :aria-label="`Stav: ${STATUS_LABEL[status]}`">
    <ol class="track__steps">
      <li
        v-for="(s, i) in STATUSES"
        :key="s"
        class="track__step"
        :class="{ 'is-done': i < current, 'is-current': i === current }"
      >
        <span class="track__bar" aria-hidden="true"></span>
        <span v-if="!compact" class="track__label">{{ STATUS_LABEL[s] }}</span>
      </li>
    </ol>
    <span v-if="compact" class="track__now">{{ STATUS_LABEL[status] }}</span>
  </div>
</template>

<style scoped>
.track__steps {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 4px;
}

.track__bar {
  display: block;
  height: 6px;
  border-radius: 3px;
  background: var(--line);
}

.is-done .track__bar {
  background: var(--ink-2);
}

.is-current .track__bar {
  background: var(--signal);
}

.track__label {
  display: block;
  margin-top: 6px;
  font-size: 0.8rem;
  color: var(--muted);
  line-height: 1.3;
}

.is-current .track__label {
  color: var(--ink);
  font-weight: 600;
}

.track--compact {
  min-width: 140px;
}

.track--compact .track__bar {
  height: 4px;
}

.track__now {
  display: block;
  margin-top: 5px;
  font-size: 0.85rem;
  color: var(--ink);
}
</style>
