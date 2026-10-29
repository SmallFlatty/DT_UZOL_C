const dateTime = new Intl.DateTimeFormat('sk-SK', {
  day: 'numeric',
  month: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
})

const time = new Intl.DateTimeFormat('sk-SK', {
  hour: '2-digit',
  minute: '2-digit',
  second: '2-digit',
})

export function formatDateTime(iso: string): string {
  return dateTime.format(new Date(iso))
}

export function formatTime(d: Date): string {
  return time.format(d)
}

export function formatWeight(kg: number): string {
  return `${Number(kg).toLocaleString('sk-SK')} kg`
}

export function formatTimeIso(iso: string | null): string {
  return iso ? time.format(new Date(iso)) : '—'
}

export function formatUptime(totalSeconds: number): string {
  const s = Math.max(0, Math.floor(totalSeconds))
  const d = Math.floor(s / 86400)
  const h = Math.floor((s % 86400) / 3600)
  const m = Math.floor((s % 3600) / 60)
  if (d > 0) return `${d} d ${h} h`
  if (h > 0) return `${h} h ${m} min`
  if (m > 0) return `${m} min`
  return `${s} s`
}
