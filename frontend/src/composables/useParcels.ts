import { onMounted, onUnmounted, ref } from 'vue'
import { api } from '@/api'
import type { Parcel } from '@/types'

// zoznam zasielok, obnovuje sa kazdych 5 s, aby bolo vidiet synchronizaciu
export function useParcels(intervalMs = 5000) {
  const parcels = ref<Parcel[]>([])
  const loaded = ref(false)

  async function refresh() {
    try {
      const data = await api.parcels()
      parcels.value = data.sort((a, b) => b.createdAt.localeCompare(a.createdAt))
    } catch {
      // stav spojenia ukazuje hlavicka
    } finally {
      loaded.value = true
    }
  }

  let timer: ReturnType<typeof setInterval> | undefined
  onMounted(() => {
    refresh()
    timer = setInterval(refresh, intervalMs)
  })
  onUnmounted(() => clearInterval(timer))

  return { parcels, loaded, refresh }
}
