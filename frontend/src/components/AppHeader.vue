<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { api } from '@/api'
import type { NodeInfo } from '@/types'
import { formatTime } from '@/format'

defineProps<{ title: string }>()

const node = ref<NodeInfo | null>(null)
const online = ref(true)
const checkedAt = ref<Date | null>(null)

async function check() {
  try {
    node.value = await api.node()
    online.value = true
  } catch {
    online.value = false
  }
  checkedAt.value = new Date()
}

let timer: ReturnType<typeof setInterval> | undefined
onMounted(() => {
  check()
  timer = setInterval(check, 5000)
})
onUnmounted(() => clearInterval(timer))
</script>

<template>
  <header class="header">
    <div class="header__inner">
      <div class="brand">
        <span class="brand__name">Dunaj Express</span>
        <span class="brand__place">{{ title }}</span>
      </div>

      <div class="node" :class="{ 'node--down': !online }" role="status">
        <span class="node__dot" aria-hidden="true"></span>
        <span v-if="online && node">Uzol {{ node.code }} je online</span>
        <span v-else>Server uzla nedostupný</span>
        <span v-if="checkedAt" class="node__time">kontrola {{ formatTime(checkedAt) }}</span>
      </div>
    </div>
  </header>
</template>

<style scoped>
.header {
  background: var(--ink);
  color: #fff;
  border-bottom: 6px solid var(--signal);
}

.header__inner {
  max-width: var(--page);
  margin: 0 auto;
  padding: 18px var(--gutter);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.brand {
  display: flex;
  align-items: baseline;
  gap: 14px;
  flex-wrap: wrap;
}

.brand__name {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 1.6rem;
  letter-spacing: 0.01em;
}

.brand__place {
  font-family: var(--font-display);
  font-size: 1.25rem;
  color: var(--signal);
}

.node {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.95rem;
}

.node__dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #4ADE80;
}

.node--down .node__dot {
  background: #F87171;
}

.node__time {
  color: #9FB3C4;
  font-size: 0.85rem;
}
</style>
