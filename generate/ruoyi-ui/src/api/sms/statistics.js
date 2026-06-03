// api/sms/statistics.js
import request from '@/utils/request'

// 获取实时统计
export function getRealTimeStatistics() {
  return request({
    url: '/sms/statistics/realtime',
    method: 'get'
  })
}

// 获取月度统计列表
export function getMonthlyStatistics() {
  return request({
    url: '/sms/statistics/monthly',
    method: 'get'
  })
}

// 获取指定月份统计
export function getMonthlyStatisticsByMonth(yearMonth) {
  return request({
    url: `/sms/statistics/monthly/${yearMonth}`,
    method: 'get'
  })
}

// // 重置统计
// export function resetStatistics() {
//   return request({
//     url: '/sms/statistics/reset',
//     method: 'post'
//   })
// }
