export type ParcelStatus = 'PRIJATA' | 'NA_DEPE' | 'ODOSLANA_NA_VYDAJ' | 'PRIPRAVENA'

export interface Parcel {
  id: string
  trackingNum: string
  senderName: string
  senderPhone: string
  recipientName: string
  recipientPhone: string
  weight: number
  status: ParcelStatus
  createdNode: string
  createdAt: string
  updatedAt: string
}

export type Channel = 'SMS' | 'EMAIL' | 'CALL'

export interface ParcelNotification {
  id: string
  parcelId: string
  channel: Channel
  message: string
  createdAt: string
}

export interface NodeInfo {
  code: string
  role: 'POINT' | 'DEPOT' | 'OFFICE'
}

export const STATUSES: ParcelStatus[] = ['PRIJATA', 'NA_DEPE', 'ODOSLANA_NA_VYDAJ', 'PRIPRAVENA']

export const STATUS_LABEL: Record<ParcelStatus, string> = {
  PRIJATA: 'Prijatá',
  NA_DEPE: 'Na depe',
  ODOSLANA_NA_VYDAJ: 'Na ceste na výdaj',
  PRIPRAVENA: 'Pripravená na výdaj',
}

export const CHANNEL_LABEL: Record<Channel, string> = {
  SMS: 'SMS',
  EMAIL: 'E-mail',
  CALL: 'Telefonát',
}

export type NodeRole = NodeInfo['role']

export const ROLE_LABEL: Record<NodeRole, string> = {
  POINT: 'Výdajné miesto',
  DEPOT: 'Triedenie a depo',
  OFFICE: 'Centrála',
}

// stav susedneho uzla z pohladu tohto uzla (GET /api/status)
export interface PeerStatus {
  url: string
  code: string | null
  role: NodeRole | null
  state: 'UP' | 'DOWN' | 'UNKNOWN'
  lastSeen: string | null
  pending: number // operacie v outboxe, ktore cakaju na tento uzol; -1 = nezname
  lastSyncOk: string | null
  lastError: string | null
}

export interface NodeStatus {
  code: string
  role: NodeRole
  dbUp: boolean
  startedAt: string
  uptimeSeconds: number
  outboxPending: number // -1 = databaza nedostupna
  peers: PeerStatus[]
}
