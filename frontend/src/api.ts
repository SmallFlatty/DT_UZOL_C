import type { Channel, NodeInfo, NodeStatus, Parcel, ParcelNotification, ParcelStatus } from './types'

const BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8081'

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  let res: Response
  try {
    res = await fetch(BASE + path, {
      ...options,
      // Content-Type len ak posielame telo, inak by kazdy GET robil zbytocny CORS preflight
      headers: options.body ? { 'Content-Type': 'application/json' } : undefined,
    })
  } catch {
    throw new Error('Server uzla neodpovedá. Skontrolujte, či beží backend.')
  }

  if (!res.ok) {
    let message = `Požiadavka zlyhala (${res.status})`
    try {
      const body = await res.json()
      if (body.message) message = body.message
    } catch {
      // telo nie je JSON
    }
    throw new Error(message)
  }

  const text = await res.text()
  return (text ? JSON.parse(text) : undefined) as T
}

export interface NewParcel {
  senderName: string
  senderPhone: string
  recipientName: string
  recipientPhone: string
  weight: number
}

export const api = {
  node: () => request<NodeInfo>('/api/node'),
  status: () => request<NodeStatus>('/api/status'),
  parcels: () => request<Parcel[]>('/api/parcels'),
  byTracking: (num: string) => request<Parcel>(`/api/parcels/tracking/${encodeURIComponent(num)}`),
  notifications: (id: string) => request<ParcelNotification[]>(`/api/parcels/${id}/notifications`),

  // A - vydajne miesto
  createParcel: (p: NewParcel) =>
    request<Parcel>('/api/point/parcels', { method: 'POST', body: JSON.stringify(p) }),
  handOver: (id: string) => request<void>(`/api/point/parcels/${id}/hand-over`, { method: 'POST' }),
  storno: (id: string) => request<void>(`/api/point/parcels/${id}`, { method: 'DELETE' }),

  // B - depo
  changeStatus: (id: string, status: ParcelStatus) =>
    request<Parcel>(`/api/depot/parcels/${id}/status?status=${status}`, { method: 'PUT' }),

  // C - centrala
  notify: (parcelId: string, channel: Channel, message: string) =>
    request<ParcelNotification>('/api/office/notifications', {
      method: 'POST',
      body: JSON.stringify({ parcelId, channel, message }),
    }),
}
