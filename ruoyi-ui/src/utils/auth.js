// import Cookies from 'js-cookie'
//
// const TokenKey = 'Admin-Token'
//
// export function getToken() {
//   return Cookies.get(TokenKey)
// }
//
// // export function setToken(token) {
// //   return Cookies.set(TokenKey, token)
// // }
// export function setToken(token) {
//   // 设置cookie永不过期（10年）
//   return Cookies.set(TokenKey, token, { expires: 3650 })
// }
//
// export function removeToken() {
//   return Cookies.remove(TokenKey)
// }


import Cookies from 'js-cookie'

const TokenKey = 'Admin-Token'

// export function getToken() {
//   return Cookies.get(TokenKey)
// }
export function getToken() {
  return localStorage.getItem(TokenKey)
}

export function setToken(token) {
  // 针对Edge浏览器的兼容性设置
  const domain = window.location.hostname
  const isLocalhost = domain === 'localhost' || domain === '127.0.0.1'

  const cookieOptions = {
    expires: 3650, // 10年
    path: '/',
    // 针对Edge的兼容性设置
    sameSite: 'lax', // 或者 'none'，但需要secure=true
    secure: window.location.protocol === 'https:'
  }

  // 如果不是localhost，设置domain
  if (!isLocalhost) {
    // 提取主域名
    const domainParts = domain.split('.')
    if (domainParts.length > 1) {
      cookieOptions.domain = '.' + domainParts.slice(-2).join('.')
    }
  }

  // return Cookies.set(TokenKey, token, cookieOptions)
  return localStorage.setItem(TokenKey, token, cookieOptions)
}

export function removeToken() {
  const domain = window.location.hostname
  const isLocalhost = domain === 'localhost' || domain === '127.0.0.1'

  const cookieOptions = {
    path: '/'
  }

  if (!isLocalhost) {
    const domainParts = domain.split('.')
    if (domainParts.length > 1) {
      cookieOptions.domain = '.' + domainParts.slice(-2).join('.')
    }
  }

  // return Cookies.remove(TokenKey, cookieOptions)
  return localStorage.removeItem(TokenKey, cookieOptions)
}
