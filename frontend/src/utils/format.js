export const STATUS = {
  PENDING: { label: '待确认', type: 'warning' },
  CONFIRMED: { label: '已确认', type: 'primary' },
  ACCEPTED: { label: '已接单', type: 'success' },
  IN_SERVICE: { label: '服务中', type: 'danger' },
  COMPLETED: { label: '已完成', type: 'info' },
  CANCELLED: { label: '已取消', type: 'info' },
  REFUNDED: { label: '已退款', type: 'info' }
}
export const statusLabel = s => (STATUS[s] ? STATUS[s].label : s)
export const money = v => (v == null ? '0.00' : Number(v).toFixed(2))
