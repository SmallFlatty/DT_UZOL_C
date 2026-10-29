<script setup lang="ts">
import { useToast } from '@/composables/useToast'

const { toasts } = useToast()
</script>

<template>
  <div class="toasts" aria-live="polite">
    <div v-for="t in toasts" :key="t.id" class="toast" :class="`toast--${t.kind}`">
      {{ t.text }}
    </div>
  </div>
</template>

<style scoped>
.toasts {
  position: fixed;
  right: 20px;
  bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 10;
  max-width: min(380px, calc(100vw - 40px));
}

.toast {
  padding: 12px 16px;
  border-radius: 6px;
  background: var(--ink);
  color: #fff;
  border-left: 5px solid var(--ok);
  font-size: 0.95rem;
}

.toast--error {
  border-left-color: var(--danger);
}
</style>
