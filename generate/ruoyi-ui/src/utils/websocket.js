// utils/websocket.js
// utils/websocket.js
// import SockJS from 'sockjs-client'
// import Stomp from 'stompjs'

// class WebSocketClient {
//   constructor() {
//     this.stompClient = null
//     this.connected = false
//     this.subscriptions = new Map()
//     this.reconnectAttempts = 0
//     this.maxReconnectAttempts = 5
//     // 🔥 添加基础URL配置
//     this.getBaseUrl = () => {
//       const baseUrl = process.env.VUE_APP_BASE_API || 'http://l127.0.0.1:8787'
//       return baseUrl.replace('http', 'ws')
//
//     }
//
//   }
//
//
//
//   connect() {
//     return new Promise((resolve, reject) => {
//       try {
//         // 🔥 修改连接URL
//         const wsUrl = `${this.getBaseUrl()}/ws`
//         console.log('🔄 尝试连接WebSocket:', wsUrl)
//
//         const socket = new SockJS(wsUrl)
//         this.stompClient = Stomp.over(socket)
//
//

import SockJS from 'sockjs-client'
import Stomp from 'stompjs'

const baseUrl = 'http://63.56.3.194:8787';  // ✅ SpringBoot 后端地址

let stompClient = null;

export function connectWebSocket() {
  const socket = new SockJS(`${baseUrl}/ws`, null, { withCredentials: false });
  stompClient = Stomp.over(socket);

  stompClient.connect({}, () => {
    console.log("✅ WebSocket连接成功");
    stompClient.subscribe('/topic/qrCodeUpdate', (msg) => {
      console.log("📩 收到推送:", msg.body);
    });
  }, (err) => {
    console.error("❌ WebSocket连接失败:", err);
  });
}

class WebSocketClient {
  constructor() {
    this.stompClient = null
    this.connected = false
    this.subscriptions = new Map()
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
  }

  connect() {
    return new Promise((resolve, reject) => {
      try {
        // const socket = new SockJS('http://127.0.0.1:8787/ws')
        // const socket = new SockJS('http://127.0.0.1:8787/ws');
        const socket = new SockJS('http://63.56.3.194:8081/ws', null, {
          transports: ['websocket', 'xhr-streaming', 'xhr-polling'],
          withCredentials: false
        });
        this.stompClient = Stomp.over(socket)

        // 关闭调试信息
        this.stompClient.debug = null

        this.stompClient.connect({}, (frame) => {
          this.connected = true
          this.reconnectAttempts = 0
          console.log('WebSocket连接成功')
          this.resubscribeAll()
          resolve(frame)
        }, (error) => {
          this.connected = false
          console.error('WebSocket连接失败:', error)
          this.handleReconnect()
          reject(error)
        })
      } catch (error) {
        console.error('WebSocket初始化失败:', error)
        reject(error)
      }
    })
  }

  subscribe(topic, callback) {
    if (this.connected && this.stompClient) {
      const subscription = this.stompClient.subscribe(topic, (message) => {
        try {
          const body = JSON.parse(message.body)
          callback(body)
        } catch (e) {
          console.error('消息解析失败:', e)
          callback(message.body)
        }
      })
      this.subscriptions.set(topic, { callback, subscription })
    } else {
      this.subscriptions.set(topic, { callback, subscription: null })
    }
  }

  unsubscribe(topic) {
    const subscriptionInfo = this.subscriptions.get(topic)
    if (subscriptionInfo && subscriptionInfo.subscription) {
      subscriptionInfo.subscription.unsubscribe()
    }
    this.subscriptions.delete(topic)
  }

  resubscribeAll() {
    this.subscriptions.forEach((value, topic) => {
      if (value.callback) {
        this.subscribe(topic, value.callback)
      }
    })
  }

  handleReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      console.log(`尝试重连... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`)
      setTimeout(() => {
        this.connect()
      }, 3000 * this.reconnectAttempts)
    }
  }

  disconnect() {
    if (this.stompClient) {
      this.stompClient.disconnect()
    }
    this.connected = false
    this.subscriptions.clear()
  }

  isConnected() {
    return this.connected
  }
}

const websocketClient = new WebSocketClient()

export default websocketClient

