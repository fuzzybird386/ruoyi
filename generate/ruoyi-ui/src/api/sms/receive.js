import request from '@/utils/request'

// 开始从API接收短信
export function startReceiveFromApi() {
  return request({
    url: '/sms/receive/start-receive',
    method: 'post'
  })
}

// 红外信号触发
export function handleInfraredSignal() {
  return request({
    url: '/sms/receive/infrared-signal',
    method: 'post'
  })
}

// 获取当前显示的二维码
export function getCurrentQrCode() {
  return request({
    url: '/sms/receive/current',
    method: 'get'
  })
}

export function getQueueSize() {
  return request({
    url: '/sms/receive/queue-size',
    method: 'get'
  })
}

// 获取队列信息
export function getQueueInfo() {
  return request({
    url: '/sms/receive/queue-info',
    method: 'get'
  })
}

// 验证二维码
export function verifyQrCode(qrContent) {
  return request({
    url: '/sms/receive/verify',
    method: 'post',
    // data: { qrContent }
    data: qrContent
  })
}

// 清空所有数据
export function clearAllQrCodes() {
  return request({
    url: '/sms/receive/clear',
    method: 'delete'
  })
}

// 模拟多条数据
export function simulateMultipleMessages() {
  return request({
    url: '/sms/receive/simulate-multiple',
    method: 'post'
  })
}

export function getAutoStatus() {
  return request({
    url: '/sms/receive/auto-status',
    method: 'get'
  })
}
