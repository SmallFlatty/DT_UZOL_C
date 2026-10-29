import { ref } from 'vue'

export interface Toast {
  id: number
  text: string
  kind: 'ok' | 'error'
}

const toasts = ref<Toast[]>([])
let nextId = 1

export function useToast() {
  function show(text: string, kind: Toast['kind'] = 'ok') {
    const id = nextId++
    toasts.value.push({ id, text, kind })
    setTimeout(() => {
      toasts.value = toasts.value.filter((t) => t.id !== id)
    }, 4000)
  }

  return {
    toasts,
    ok: (text: string) => show(text, 'ok'),
    error: (e: unknown) => show(e instanceof Error ? e.message : String(e), 'error'),
  }
}
