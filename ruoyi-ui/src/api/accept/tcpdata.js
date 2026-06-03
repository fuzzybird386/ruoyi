import request from '@/utils/request'

// 查询统计数据列表
export function getStatsList(query) {
  return request({
    url: '/api/tcpdata/stats',
    method: 'get',
    params: query
  })
}

// 查询统计图表数据
export function getStatsChart(months) {
  return request({
    url: '/api/tcpdata/stats/chart',
    method: 'get',
    params: { months }
  })
}

// 获取当月统计
export function getCurrentMonthStats() {
  return request({
    url: '/api/tcpdata/stats/current',
    method: 'get'
  })
}

// 导出统计数据
export function exportStats(query) {
  return request({
    url: '/api/tcpdata/stats/export',
    method: 'post',
    data: query,
    responseType: 'blob'
  })
}
