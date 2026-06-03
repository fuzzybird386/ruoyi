// // src/api/system/qrcode.js
import request from '@/utils/request'

const qrCodeRequest = request;
qrCodeRequest.defaults.baseURL = 'http://63.56.3.194:8787';


// 生成二维码（返回Base64）
export function generateQrCodeBase64(data) {
  return request({
    url: '/smsQrCode/generateBase64',
    method: 'post',
    data: data,
    responseType: 'blob' // 重要：接收二进制图片流
  })
}

// 生成二维码（返回图片流）
export function generateQrCodeStream(data) {
  return request({
    url: '/smsQrCode/generate',
    method: 'post',
    data: data,
    responseType: 'blob'
  })
}

/**
 * 自动生成二维码API
 * 只要调用这个接口并传入数据，就自动生成二维码
 */

// 自动生成二维码（返回图片流）
export function autoGenerateQrCode(data) {
  return request({
    url: '/smsQrCode/autoGenerate',
    method: 'post',
    data: data,
    responseType: 'blob'
  })
}

// // 自动生成二维码（返回Base64）
// export function autoGenerateQrCodeBase64(data) {
//   return request({
//     url: '/smsQrCodeautoGenerateBase64',
//     method: 'post',
//     data: data
//   })
// }
// // 自动生成二维码（返回Base64，自定义返回值，0：成功）
// export function autoGenerateQrCodeBase64B(data) {
//   return request({
//     url: '/smsQrCodeautoGenerateBase64B',
//     method: 'post',
//     data: data
//   })
// }
//
// // GET方式自动生成二维码
// export function autoGenerateQrCodeByGet(params) {
//   return request({
//     url: '/smsQrCode/autoGenerate',
//     method: 'get',
//     params: params,
//     responseType: 'blob'
//   })
// }

// // 批量自动生成二维码
// export function batchAutoGenerateQrCode(data) {
//   return request({
//     url: '/smsQrCode/batchAutoGenerate',
//     method: 'post',
//     data: data
//   })
// }

// 获取最新生成的二维码
// export function getLatestQrCode() {
//   return request({
//     url: '/smsQrCode/getLatestQrCode',
//     method: 'get'
//   })
// }

// // 清空二维码数据
// export function clearQrCode() {
//   return request({
//     url: '/smsQrCode/clearQrCode',
//     method: 'post'
//   })
// }

// // 存储并生成二维码
// export function generateAndStore(data) {
//   return request({
//     url: '/smsQrCode/generateAndStore',
//     method: 'post',
//     data: data
//   })
// }


// // 获取当前二维码（TCP信号控制）
// export function getCurrentQrCode() {
//   return request({
//     url: '/smsQrCode/getCurrentQrCode',
//     method: 'get'
//   })
// }

// // 设置数据队列
// export function setDataQueue(data) {
//   return request({
//     url: '/smsQrCode/setDataQueue',
//     method: 'post',
//     data: data
//   })
// }
//
// // 获取TCP状态
// export function getTcpStatus() {
//   return request({
//     url: '/smsQrCode/getTcpStatus',
//     method: 'get'
//   })
// }

// // 手动切换
// export function manualNext() {
//   return request({
//     url: '/smsQrCode/manualNext',
//     method: 'post'
//   })
// }

// // 清空队列
// export function clearQueue() {
//   return request({
//     url: '/smsQrCode/clearQueue',
//     method: 'post'
//   })
// }

// // 重启TCP服务器
// export function restartTcpServer(port = 1919) {
//   return request({
//     url: '/smsQrCode/restartTcpServer',
//     method: 'post',
//     params: { port }
//   })
// }

// // 检查最新二维码数据
// export function checkLatestQrCode() {
//   return request({
//     url: '/smsQrCode/check-latest-qrcode',
//     method: 'get'
//   })
// }


//*************修改需求后********************************
// 接收数据
export function receiveData(data) {
  return request({
    url: '/smsQrCode/receive',
    method: 'post',
    data: data
  })
}



// 导出未成功数据
export function exportFailedData() {
  return request({
    url: '/smsQrCode/exportFailedData',
    method: 'post',
    responseType: 'blob'
  })
}

// 获取队列状态
export function getQueueStatus() {
  return request({
    url: '/smsQrCode/queueStatus',
    method: 'get'
  })
}

// 启动队列处理
export function startQueue() {
  return request({
    url: '/smsQrCode/startQueue',
    method: 'post'
  })
}

// 停止队列处理
export function stopQueue() {
  return request({
    url: '/smsQrCode/stopQueue',
    method: 'post'
  })
}

// 清空所有队列
export function clearAllQueues() {
  return request({
    url: '/smsQrCode/clearAllQueues',
    method: 'post'
  })
}



// 检查系统状态
export function checkStatus() {
  return request({
    url: '/smsQrCode/checkStatus',
    method: 'get'
  })
}


// 接收运维平台数据（新增接口）
export function receiveDataFromPlatform(data) {
  return request({
    url: '/smsQrCode/receiveData',
    method: 'post',
    data: data
  })
}

// 获取所有队列状态（新增接口）
export function getAllQueueStatus() {
  return request({
    url: '/smsQrCode/getAllQueueStatus',
    method: 'get'
  })
}

// 导出并清空失败数据（新增接口）
export function exportAndClearFailedData() {
  return request({
    url: '/smsQrCode/exportAndClearFailedData',
    method: 'post'
  })
}

// 选择性导出数据（新增接口）
export function exportSelectedData(dataIds) {
  return request({
    url: '/smsQrCode/exportSelectedData',
    method: 'post',
    data: dataIds
  })
}

//统计接收数据和生成二维码数量
// 获取统计数据
export function getStatistics(params) {
  return request({
    url: '/smsQrCode/getStatistics',
    method: 'get',
    params: params
  })
}

//定时清理相关API*********************
// 已完成数据清理相关API
export function cleanupCompletedData(params) {
  return request({
    url: '/smsQrCode/cleanupCompleted',
    method: 'post',
    data: params
  });
}

export function getCompletedStats() {
  return request({
    url: '/smsQrCode/completedStats',
    method: 'get'
  });
}

export function getCleanupPreview(retentionDays) {
  return request({
    url: '/smsQrCode/cleanupPreview',
    method: 'get',
    params: { retentionDays }
  });
}

export function getCleanupSchedule() {
  return request({
    url: '/smsQrCode/cleanupSchedule',
    method: 'get'
  });
}
