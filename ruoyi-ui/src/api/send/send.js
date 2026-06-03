import request from '@/utils/request'

// 获取TCP服务器状态
export function getServerStatus() {
  return request({
    url: '/api/data/server/status',
    method: 'get'
  })
}

// 获取所有数据列表
export function getDataList() {
  return request({
    url: '/api/data/list',
    method: 'get'
  })
}

// 根据数据ID获取数据
export function getDataById(dataId) {
  return request({
    url: `/api/data/${dataId}`,
    method: 'get'
  })
}

// 获取队列一数据
export function getQueue1() {
  return request({
    url: '/api/data/queue1',
    method: 'get'
  })
}

// 获取队列二数据
export function getQueue2() {
  return request({
    url: '/api/data/queue2',
    method: 'get'
  })
}

// 手动导入数据到队列二
export function importToQueue2(data) {
  return request({
    url: '/api/data/queue2/import',
    method: 'post',
    data: data
  })
}

// 手动重试队列二数据
export function retryQueue2Data(dataId) {
  return request({
    url: `/api/data/queue2/retry/${dataId}`,
    method: 'post'
  })
}


// 调试接口
export function debugAllData() {
  return request({
    url: '/api/data/debug/all-data',
    method: 'get'
  })
}
