<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { api } from '@/api'
import { CHANNEL_LABEL, STATUS_LABEL, STATUSES, type Channel, type Parcel, type ParcelNotification } from '@/types'
import { useParcels } from '@/composables/useParcels'
import { useToast } from '@/composables/useToast'
import { formatDateTime, formatWeight } from '@/format'
import StatusTrack from '@/components/StatusTrack.vue'

const { parcels, loaded } = useParcels()
const toast = useToast()

// ---------- prehlad ----------
const counts = computed(() =>
  STATUSES.map((s) => ({ status: s, count: parcels.value.filter((p) => p.status === s).length })),
)

// ---------- vyhladavanie ----------
const query = ref('')

const visible = computed(() => {
  const q = query.value.trim().toLowerCase()
  if (!q) return parcels.value
  return parcels.value.filter(
    (p) =>
      p.trackingNum.toLowerCase().includes(q) ||
      p.recipientName.toLowerCase().includes(q) ||
      p.senderName.toLowerCase().includes(q),
  )
})

async function findExact() {
  const q = query.value.trim()
  if (!q) return
  try {
    const p = await api.byTracking(q.toUpperCase())
    selectedId.value = p.id
  } catch (e) {
    toast.error(e)
  }
}

// ---------- detail ----------
const selectedId = ref<string | null>(null)
const selected = computed<Parcel | null>(() => parcels.value.find((p) => p.id === selectedId.value) ?? null)

const notifications = ref<ParcelNotification[]>([])

async function loadNotifications() {
  if (!selectedId.value) return
  try {
    const list = await api.notifications(selectedId.value)
    notifications.value = list.sort((a, b) => b.createdAt.localeCompare(a.createdAt))
  } catch (e) {
    toast.error(e)
  }
}

const channel = ref<Channel>('SMS')
const message = ref('')
const sending = ref(false)

function template(p: Parcel): string {
  if (p.status === 'PRIPRAVENA') {
    return `Dobrý deň, vaša zásielka ${p.trackingNum} je pripravená na vyzdvihnutie na výdajnom mieste.`
  }
  return `Dobrý deň, vaša zásielka ${p.trackingNum} je na ceste. Aktuálny stav: ${STATUS_LABEL[p.status].toLowerCase()}.`
}

watch(selectedId, () => {
  notifications.value = []
  if (selected.value) message.value = template(selected.value)
  loadNotifications()
})

async function send() {
  if (!selected.value || !message.value.trim()) return
  sending.value = true
  try {
    await api.notify(selected.value.id, channel.value, message.value.trim())
    toast.ok(`${CHANNEL_LABEL[channel.value]} pre ${selected.value.recipientName} zaznamenaná`)
    loadNotifications()
  } catch (e) {
    toast.error(e)
  } finally {
    sending.value = false
  }
}
</script>

<template>
  <main class="page">
    <section class="summary" aria-label="Prehľad zásielok podľa stavu">
      <div class="summary__total">
        <span class="summary__num">{{ parcels.length }}</span>
        <span>zásielok v sieti</span>
      </div>
      <div v-for="c in counts" :key="c.status" class="summary__item">
        <span class="summary__num summary__num--small">{{ c.count }}</span>
        <span class="muted">{{ STATUS_LABEL[c.status] }}</span>
      </div>
    </section>

    <div class="layout">
      <section class="panel">
        <form class="search" @submit.prevent="findExact">
          <input v-model="query" type="search" placeholder="Číslo zásielky alebo meno" aria-label="Hľadať zásielku" />
          <button class="btn btn--dark">Nájsť</button>
        </form>

        <ul class="list">
          <li v-for="p in visible" :key="p.id">
            <button class="row" :class="{ 'is-selected': p.id === selectedId }" @click="selectedId = p.id">
              <span class="tracking">{{ p.trackingNum }}</span>
              <span class="row__who">{{ p.recipientName }}</span>
              <StatusTrack :status="p.status" compact />
            </button>
          </li>
          <li v-if="loaded && visible.length === 0" class="empty">
            {{ query ? 'Nič sa nenašlo. Skontrolujte číslo zásielky.' : 'V sieti zatiaľ nie sú žiadne zásielky.' }}
          </li>
        </ul>
      </section>

      <section class="panel detail">
        <template v-if="selected">
          <div class="detail__head">
            <h2 class="detail__num">{{ selected.trackingNum }}</h2>
            <span class="muted">prijatá {{ formatDateTime(selected.createdAt) }} na uzle {{ selected.createdNode }}</span>
          </div>

          <StatusTrack :status="selected.status" />

          <dl class="facts">
            <div>
              <dt>Odosielateľ</dt>
              <dd>{{ selected.senderName }}<br /><span class="muted">{{ selected.senderPhone }}</span></dd>
            </div>
            <div>
              <dt>Príjemca</dt>
              <dd>{{ selected.recipientName }}<br /><span class="muted">{{ selected.recipientPhone }}</span></dd>
            </div>
            <div>
              <dt>Hmotnosť</dt>
              <dd>{{ formatWeight(selected.weight) }}</dd>
            </div>
          </dl>

          <form class="compose" @submit.prevent="send">
            <h3>Kontaktovať príjemcu</h3>
            <div class="channels" role="radiogroup" aria-label="Spôsob kontaktu">
              <label v-for="(label, key) in CHANNEL_LABEL" :key="key" class="channel" :class="{ 'is-active': channel === key }">
                <input v-model="channel" type="radio" :value="key" />
                {{ label }}
              </label>
            </div>
            <div class="field">
              <label for="msg">{{ channel === 'CALL' ? 'Poznámka k hovoru' : 'Správa' }}</label>
              <textarea id="msg" v-model="message"></textarea>
            </div>
            <button class="btn btn--primary" :disabled="sending || !message.trim()">
              {{ channel === 'CALL' ? 'Zaznamenať hovor' : `Odoslať ${CHANNEL_LABEL[channel]}` }}
            </button>
          </form>

          <div class="history">
            <h3>História kontaktu</h3>
            <ul v-if="notifications.length">
              <li v-for="n in notifications" :key="n.id">
                <span class="history__meta">{{ CHANNEL_LABEL[n.channel] }}, {{ formatDateTime(n.createdAt) }}</span>
                <span>{{ n.message }}</span>
              </li>
            </ul>
            <p v-else class="muted">Príjemca zatiaľ nebol kontaktovaný.</p>
          </div>
        </template>

        <div v-else class="empty">Vyberte zásielku zo zoznamu alebo ju vyhľadajte podľa čísla.</div>
      </section>
    </div>
  </main>
</template>

<style scoped>
/* prehlad */
.summary {
  display: flex;
  gap: 32px;
  align-items: flex-end;
  flex-wrap: wrap;
  margin-bottom: 24px;
}

.summary__total,
.summary__item {
  display: flex;
  flex-direction: column;
}

.summary__total {
  padding-right: 32px;
  border-right: 1px solid var(--line);
}

.summary__num {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 3rem;
  line-height: 1;
}

.summary__num--small {
  font-size: 1.8rem;
}

/* rozlozenie */
.layout {
  display: grid;
  grid-template-columns: 420px 1fr;
  gap: 24px;
  align-items: start;
}

.search {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
}

.list {
  list-style: none;
  margin: 0 -10px;
  padding: 0;
  max-height: 620px;
  overflow-y: auto;
}

.row {
  font: inherit;
  color: inherit;
  width: 100%;
  text-align: left;
  background: transparent;
  border: none;
  border-left: 4px solid transparent;
  border-radius: 4px;
  padding: 10px 12px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2px 12px;
  cursor: pointer;
}

.row:hover {
  background: var(--paper);
}

.row.is-selected {
  background: var(--paper);
  border-left-color: var(--signal);
}

.row__who {
  text-align: right;
  align-self: center;
}

.row :deep(.track) {
  grid-column: 1 / -1;
}

/* detail */
.detail {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.detail__head {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.detail__num {
  font-size: 2.4rem;
  letter-spacing: 0.04em;
}

.facts {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin: 0;
}

dt {
  font-size: 0.85rem;
  color: var(--muted);
}

dd {
  margin: 2px 0 0;
}

.compose {
  border-top: 1px solid var(--line);
  padding-top: 20px;
}

.compose h3 {
  margin-bottom: 12px;
}

.channels {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
}

.channel {
  border: 1px solid var(--line);
  border-radius: 4px;
  padding: 6px 14px;
  cursor: pointer;
  font-weight: 600;
  font-size: 0.95rem;
}

.channel input {
  position: absolute;
  opacity: 0;
  width: 1px;
}

.channel.is-active {
  background: var(--ink);
  border-color: var(--ink);
  color: #fff;
}

.channel:has(input:focus-visible) {
  outline: 3px solid var(--signal);
}

.history h3 {
  margin-bottom: 10px;
}

.history ul {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history li {
  display: flex;
  flex-direction: column;
  padding-left: 12px;
  border-left: 3px solid var(--line);
}

.history__meta {
  font-size: 0.85rem;
  color: var(--muted);
}

@media (max-width: 1000px) {
  .layout {
    grid-template-columns: 1fr;
  }
  .facts {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
