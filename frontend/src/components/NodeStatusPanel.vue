<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { api } from '@/api'
import { ROLE_LABEL, type NodeStatus } from '@/types'
import { formatTime, formatTimeIso, formatUptime } from '@/format'

// stavovy panel uzlov: tento uzol + susedia (dostupnost, fronta v outboxe, posledna synchronizacia)
const status = ref<NodeStatus | null>(null)
const backendDown = ref(false)
const checkedAt = ref<Date | null>(null)

async function refresh() {
  try {
    status.value = await api.status()
    backendDown.value = false
  } catch {
    backendDown.value = true
  }
  checkedAt.value = new Date()
}

function stateLabel(state: string) {
  if (state === 'UP') return 'online'
  if (state === 'DOWN') return 'nedostupný'
  return 'zisťuje sa'
}

function pendingLabel(n: number) {
  return n < 0 ? '—' : String(n)
}

let timer: ReturnType<typeof setInterval> | undefined
onMounted(() => {
  refresh()
  timer = setInterval(refresh, 3000)
})
onUnmounted(() => clearInterval(timer))
</script>

<template>
  <section class="status" aria-label="Stav uzlov">
    <div class="status__inner">
      <div class="status__head">
        <h2>Stav uzlov</h2>
        <span v-if="checkedAt" class="muted status__time">obnovené {{ formatTime(checkedAt) }}</span>
      </div>

      <p v-if="backendDown" class="status__alert" role="alert">
        Lokálny backend nedostupný — panel nemá aktuálne dáta.
      </p>

      <div v-if="status" class="status__grid" :class="{ 'status__grid--stale': backendDown }">
        <!-- tento uzol -->
        <article class="card card--self">
          <header class="card__head">
            <span class="dot" :class="status.dbUp ? 'dot--up' : 'dot--down'" aria-hidden="true"></span>
            <h3>Uzol {{ status.code }}</h3>
            <span class="tag">tento uzol</span>
          </header>
          <p class="muted card__role">{{ ROLE_LABEL[status.role] }}</p>
          <dl class="card__rows">
            <div>
              <dt>Databáza</dt>
              <dd :class="status.dbUp ? 'ok' : 'bad'">{{ status.dbUp ? 'dostupná' : 'nedostupná' }}</dd>
            </div>
            <div>
              <dt>Neodoslané operácie</dt>
              <dd :class="{ warn: status.outboxPending > 0 }">{{ pendingLabel(status.outboxPending) }}</dd>
            </div>
            <div>
              <dt>Beží</dt>
              <dd>{{ formatUptime(status.uptimeSeconds) }}</dd>
            </div>
          </dl>
        </article>

        <!-- susedia -->
        <article v-for="p in status.peers" :key="p.url" class="card">
          <header class="card__head">
            <span
              class="dot"
              :class="{ 'dot--up': p.state === 'UP', 'dot--down': p.state === 'DOWN' }"
              aria-hidden="true"
            ></span>
            <h3>Uzol {{ p.code ?? '?' }}</h3>
            <span class="tag" :class="{ 'tag--down': p.state === 'DOWN' }">{{ stateLabel(p.state) }}</span>
          </header>
          <p class="muted card__role">{{ p.role ? ROLE_LABEL[p.role] : p.url }}</p>
          <dl class="card__rows">
            <div>
              <dt>Čaká na odoslanie</dt>
              <dd :class="{ warn: p.pending > 0 }">{{ pendingLabel(p.pending) }}</dd>
            </div>
            <div>
              <dt>Posledný kontakt</dt>
              <dd>{{ formatTimeIso(p.lastSeen) }}</dd>
            </div>
            <div>
              <dt>Posledná synchronizácia</dt>
              <dd>{{ formatTimeIso(p.lastSyncOk) }}</dd>
            </div>
          </dl>
          <p v-if="p.state === 'DOWN' && p.lastError" class="card__error" :title="p.lastError">
            {{ p.lastError }}
          </p>
        </article>
      </div>
    </div>
  </section>
</template>

<style scoped>
.status {
  background: var(--surface);
  border-bottom: 1px solid var(--line);
}

.status__inner {
  max-width: var(--page);
  margin: 0 auto;
  padding: 16px var(--gutter);
}

.status__head {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 12px;
}

.status__time {
  font-size: 0.85rem;
}

.status__alert {
  margin: 0 0 12px;
  padding: 8px 12px;
  border-radius: var(--radius);
  border: 1px solid var(--danger);
  color: var(--danger);
  background: #FDF1F0;
}

.status__grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 12px;
}

.status__grid--stale {
  opacity: 0.55;
}

.card {
  border: 1px solid var(--line);
  border-radius: var(--radius);
  padding: 12px 14px;
  background: var(--paper);
}

.card--self {
  border-color: var(--ink-2);
}

.card__head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card__role {
  margin: 2px 0 8px;
  font-size: 0.9rem;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--muted);
  flex: none;
}

.dot--up {
  background: #4ADE80;
}

.dot--down {
  background: #F87171;
}

.tag {
  margin-left: auto;
  font-size: 0.75rem;
  padding: 1px 8px;
  border-radius: 999px;
  border: 1px solid var(--line);
  background: var(--surface);
  color: var(--ink-2);
}

.tag--down {
  border-color: var(--danger);
  color: var(--danger);
}

.card__rows {
  margin: 0;
  display: grid;
  gap: 2px;
  font-size: 0.9rem;
}

.card__rows div {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.card__rows dt {
  color: var(--muted);
}

.card__rows dd {
  margin: 0;
  font-weight: 600;
}

.ok {
  color: var(--ok);
}

.bad {
  color: var(--danger);
}

.warn {
  color: var(--signal-dark);
}

.card__error {
  margin: 8px 0 0;
  font-size: 0.8rem;
  color: var(--danger);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
