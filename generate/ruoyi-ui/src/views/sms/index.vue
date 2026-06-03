<!--<template>-->
<!--  <div class="qr-code-container">-->
<!--    &lt;!&ndash; 显示二维码 &ndash;&gt;-->
<!--    <div v-if="currentImageUrl" class="qrcode-display">-->
<!--      <div class="qrcode-wrapper">-->
<!--        <img-->
<!--          :src="currentImageUrl"-->
<!--          class="qrcode-image"-->
<!--          alt="二维码"-->
<!--          @load="onImageLoad"-->
<!--          @error="onImageError"-->
<!--        />-->
<!--        <div class="refresh-indicator" v-if="isRefreshing">-->
<!--          <div class="spinner"></div>-->
<!--          <span>正在检查更新...</span>-->
<!--        </div>-->
<!--      </div>-->
<!--      <div class="data-info">-->
<!--        <div class="update-time">生成时间: {{ lastUpdateTime }}</div>-->
<!--        <div class="data-content" v-if="currentDataContent">-->
<!--&lt;!&ndash;          数据内容: {{ currentDataContent }}&ndash;&gt;-->
<!--        </div>-->
<!--&lt;!&ndash;        <div class="request-source">来源: {{ requestSource }}</div>&ndash;&gt;-->
<!--        <div class="request-count">调用次数: {{ requestCount }}</div>-->
<!--        <div class="polling-info">-->
<!--          轮询状态: {{ pollingStatus }} ({{ pollingCount }}次)-->
<!--        </div>-->
<!--        <div class="queue-info">-->
<!--          扫描总数: {{ pendingQrQueue.length }}-->
<!--&lt;!&ndash;          等待信号: {{ waitingForReceiveSignal ? '是' : '否' }}&ndash;&gt;-->
<!--        </div>-->
<!--      </div>-->
<!--    </div>-->

<!--    &lt;!&ndash; 空白状态 &ndash;&gt;-->
<!--    <div v-else class="empty-state">-->
<!--      <div class="empty-content">-->
<!--        <div class="empty-icon">📭</div>-->
<!--        <div class="empty-text">暂无二维码数据</div>-->
<!--        <div class="empty-tip">等待API接口调用生成二维码</div>-->

<!--        <div class="request-status" :class="statusClass">-->
<!--          {{ lastRequestStatus }}-->
<!--        </div>-->

<!--        <div class="polling-controls">-->
<!--          <button @click="togglePolling" class="control-btn" :class="{ 'active': isPollingActive }">-->
<!--            {{ isPollingActive ? '停止轮询' : '开始轮询' }}-->
<!--          </button>-->
<!--          <button @click="forceCheck" class="control-btn secondary">立即检查</button>-->
<!--          <span class="polling-timer" v-if="isPollingActive">-->
<!--            下次检查: {{ countdown }}秒-->
<!--          </span>-->
<!--        </div>-->

<!--        <div class="empty-actions">-->
<!--          <button @click="testGenerate" class="test-btn">测试生成二维码</button>-->
<!--          <button @click="simulateExternalCall" class="test-btn secondary">模拟外部调用</button>-->
<!--        </div>-->
<!--      </div>-->
<!--    </div>-->

<!--    &lt;!&ndash; 调试信息 &ndash;&gt;-->
<!--&lt;!&ndash;    <div class="debug-info">&ndash;&gt;-->
<!--&lt;!&ndash;      <h4>实时监控</h4>&ndash;&gt;-->
<!--&lt;!&ndash;      <p>轮询状态: {{ pollingStatus }}</p>&ndash;&gt;-->
<!--&lt;!&ndash;      <p>轮询次数: {{ pollingCount }}</p>&ndash;&gt;-->
<!--&lt;!&ndash;      <p>最后检查: {{ lastCheckTime || '无' }}</p>&ndash;&gt;-->
<!--&lt;!&ndash;      <p>API调用状态: {{ lastRequestStatus }}</p>&ndash;&gt;-->
<!--&lt;!&ndash;      <p>图片URL: {{ currentImageUrl ? '✅ 已设置' : '❌ 未设置' }}</p>&ndash;&gt;-->
<!--&lt;!&ndash;      <p>当前数据: {{ currentDataContent || '无' }}</p>&ndash;&gt;-->
<!--&lt;!&ndash;      <p>请求来源: {{ requestSource }}</p>&ndash;&gt;-->
<!--&lt;!&ndash;      <p>调用次数: {{ requestCount }}</p>&ndash;&gt;-->
<!--&lt;!&ndash;      <p>等待信号: {{ waitingForReceiveSignal ? '✅ 是' : '❌ 否' }}</p>&ndash;&gt;-->
<!--&lt;!&ndash;      <p>最后处理ID: {{ lastProcessedId || '无' }}</p>&ndash;&gt;-->
<!--&lt;!&ndash;      <p>队列长度: {{ pendingQrQueue.length }}</p>&ndash;&gt;-->
<!--&lt;!&ndash;      <p>队列内容: {{ pendingQrQueue.length > 0 ? '✅ 有数据' : '📭 空' }}</p>&ndash;&gt;-->
<!--&lt;!&ndash;      <p>最后错误: {{ lastError || '无' }}</p>&ndash;&gt;-->
<!--&lt;!&ndash;      <p>WebSocket状态: {{ wsStatus }}</p>&ndash;&gt;-->
<!--&lt;!&ndash;    </div>&ndash;&gt;-->
<!--  </div>-->
<!--</template>-->

<!--<script>-->
<!--import { autoGenerateQrCode, checkLatestQrCode } from "@/api/sms/qrcode";-->
<!--import websocketClient from "@/utils/websocket";-->

<!--export default {-->
<!--  name: "QrCodeDisplay",-->
<!--  data() {-->
<!--    return {-->
<!--      currentImageUrl: "",-->
<!--      currentDataContent: "",-->
<!--      currentData: null,-->
<!--      lastUpdateTime: null,-->
<!--      requestSource: "未接收",-->
<!--      lastRequestStatus: "等待API调用...",-->
<!--      lastError: "",-->
<!--      requestCount: 0,-->
<!--      wsStatus: "未连接",-->
<!--      isWsConnected: false,-->
<!--      isPollingActive: true,-->
<!--      pollingInterval: 2000,-->
<!--      pollingTimer: null,-->
<!--      pollingCount: 0,-->
<!--      pollingStatus: "未开始",-->
<!--      lastCheckTime: null,-->
<!--      countdown: 2,-->
<!--      countdownTimer: null,-->
<!--      isRefreshing: false,-->
<!--      lastProcessedId: null,-->
<!--      waitingForReceiveSignal: false,-->
<!--      pendingQrQueue: []-->
<!--    };-->
<!--  },-->

<!--  computed: {-->
<!--    statusClass() {-->
<!--      if (this.lastRequestStatus.includes('成功')) return 'status-success';-->
<!--      if (this.lastRequestStatus.includes('失败')) return 'status-error';-->
<!--      if (this.lastRequestStatus.includes('正在')) return 'status-loading';-->
<!--      return 'status-default';-->
<!--    }-->
<!--  },-->

<!--  mounted() {-->
<!--    console.log("=== 启动二维码显示系统 ===");-->
<!--    this.initWebSocket().then(() => this.fetchInitialQueue());-->
<!--    this.startPolling();-->
<!--  },-->

<!--  beforeUnmount() {-->
<!--    this.disconnectWebSocket();-->
<!--    this.stopPolling();-->
<!--    this.revokeImageUrl();-->
<!--  },-->

<!--  methods: {-->
<!--    /** 初始化WebSocket */-->
<!--    async initWebSocket() {-->
<!--      try {-->
<!--        this.wsStatus = "连接中...";-->
<!--        await websocketClient.connect();-->
<!--        this.isWsConnected = true;-->
<!--        this.wsStatus = "已连接";-->

<!--        websocketClient.subscribe('/topic/qrCodeUpdates', (message) => {-->
<!--          console.log('📨 接收到WebSocket消息:', message);-->
<!--          this.handleQrCodeMessage(message);-->
<!--        });-->

<!--        this.lastRequestStatus = "WebSocket已连接，等待二维码...";-->
<!--      } catch (error) {-->
<!--        console.error("WebSocket连接失败:", error);-->
<!--        this.wsStatus = `连接失败: ${error.message}`;-->
<!--        this.lastRequestStatus = "WebSocket连接失败，使用轮询模式";-->
<!--      }-->
<!--    },-->

<!--    /** 处理WebSocket消息 */-->
<!--    async handleQrCodeMessage(message) {-->
<!--      try {-->
<!--        console.log('📨 接收到WebSocket消息:', message);-->

<!--        if (message.clear) {-->
<!--          this.clearDisplay();-->
<!--          this.waitingForReceiveSignal = false;-->
<!--          this.lastProcessedId = null; // 重置已处理ID-->
<!--          console.log("🗑️ 收到清空指令，重置所有状态");-->
<!--          return;-->
<!--        }-->

<!--        // 处理接收确认信号-->
<!--        if (message.receive_signal === true) {-->
<!--          console.log("✅ 收到接收确认信号，准备显示下一条二维码");-->
<!--          this.waitingForReceiveSignal = false;-->
<!--          this.lastProcessedId = null; // 关键：重置已处理ID，允许显示下一条-->

<!--          // 短暂延迟后处理下一条，确保状态完全更新-->
<!--          setTimeout(() => {-->
<!--            this.processNextQrInQueue();-->
<!--          }, 100);-->
<!--          return;-->
<!--        }-->

<!--        if (message.type === 'QR_CODE_UPDATE' && message.data) {-->
<!--          console.log("📨 收到新二维码数据:", message.data);-->
<!--          this.pendingQrQueue.push(message.data);-->

<!--          // 如果没有在等待信号，立即处理-->
<!--          if (!this.waitingForReceiveSignal) {-->
<!--            this.processNextQrInQueue();-->
<!--          } else {-->
<!--            console.log("⏳ 当前等待信号中，新数据已加入队列等待");-->
<!--          }-->
<!--        }-->
<!--      } catch (error) {-->
<!--        console.error("处理WebSocket消息失败:", error);-->
<!--      }-->
<!--    },-->

<!--    /** 顺序处理队列二维码 */-->
<!--    async processNextQrInQueue() {-->
<!--      // 如果正在等待信号或者已经在处理，则跳过-->
<!--      if (this.waitingForReceiveSignal || this.isRefreshing) {-->
<!--        console.log("⏳ 等待信号中或正在刷新，暂不处理新二维码");-->
<!--        return;-->
<!--      }-->

<!--      if (this.pendingQrQueue.length === 0) {-->
<!--        console.log("📭 队列为空，无待处理二维码");-->
<!--        return;-->
<!--      }-->

<!--      const nextData = this.pendingQrQueue[0]; // 只看第一个，不立即shift-->
<!--      const dataId = this.generateDataId(nextData);-->

<!--      // 检查是否已经处理过这个ID-->
<!--      if (this.lastProcessedId === dataId) {-->
<!--        console.log("🔄 跳过已处理的二维码:", dataId);-->
<!--        this.pendingQrQueue.shift(); // 移除已处理的-->
<!--        // 递归处理下一条-->
<!--        setTimeout(() => this.processNextQrInQueue(), 50);-->
<!--        return;-->
<!--      }-->

<!--      console.log("➡️ 处理队列中下一条二维码:", nextData);-->
<!--      this.pendingQrQueue.shift(); // 确认要处理时才移除-->

<!--      await this.processNewQrCode(nextData, "队列数据");-->
<!--    },-->

<!--    async processNewQrCode(data, source = "未知来源") {-->
<!--      try {-->
<!--        const dataId = this.generateDataId(data);-->

<!--        // 更新状态前再次检查-->
<!--        if (this.waitingForReceiveSignal) {-->
<!--          console.log("⏳ 仍在等待前一条确认信号，暂缓处理新数据");-->
<!--          // 将数据重新放回队列开头-->
<!--          this.pendingQrQueue.unshift(data);-->
<!--          return;-->
<!--        }-->

<!--        this.lastProcessedId = dataId;-->
<!--        this.requestCount++;-->
<!--        this.requestSource = source;-->

<!--        console.log(`📡 生成二维码中 (${source})`, data);-->
<!--        this.lastRequestStatus = "正在生成二维码...";-->
<!--        this.isRefreshing = true;-->

<!--        await this.generateAndDisplayQrCode(data);-->

<!--        // 只有在成功生成后才设置等待信号-->
<!--        this.waitingForReceiveSignal = true;-->
<!--        console.log("🕓 等待外部信号确认 (receive_signal=true)");-->

<!--      } catch (e) {-->
<!--        console.error("❌ 处理二维码失败:", e);-->
<!--        this.isRefreshing = false;-->
<!--        // 处理失败时也继续处理下一条-->
<!--        this.waitingForReceiveSignal = false;-->
<!--        setTimeout(() => this.processNextQrInQueue(), 100);-->
<!--      }-->
<!--    },-->

<!--    /** 生成更精确的唯一ID */-->
<!--    generateDataId(data) {-->
<!--      if (!data) return 'empty_' + Date.now();-->

<!--      // 优先使用明确的标识字段-->
<!--      if (data.id) return `id_${data.id}`;-->
<!--      if (data.qrId) return `qr_${data.qrId}`;-->
<!--      if (data.orderId) return `order_${data.orderId}`;-->
<!--      if (data.timestamp) return `ts_${data.timestamp}`;-->

<!--      // 对于复杂对象，生成更稳定的hash-->
<!--      try {-->
<!--        const str = JSON.stringify(data, Object.keys(data).sort());-->
<!--        let hash = 0;-->
<!--        for (let i = 0; i < str.length; i++) {-->
<!--          const char = str.charCodeAt(i);-->
<!--          hash = ((hash << 5) - hash) + char;-->
<!--          hash = hash & hash; // Convert to 32bit integer-->
<!--        }-->
<!--        return `hash_${Math.abs(hash)}`;-->
<!--      } catch (e) {-->
<!--        // 如果序列化失败，使用时间戳-->
<!--        return `fallback_${Date.now()}`;-->
<!--      }-->
<!--    },-->

<!--    /** 生成并显示二维码 */-->
<!--    async generateAndDisplayQrCode(data) {-->
<!--      try {-->
<!--        if (!data) throw new Error("数据为空");-->
<!--        this.lastRequestStatus = "调用API生成二维码中...";-->

<!--        const response = await autoGenerateQrCode(data);-->
<!--        if (!response || response.size === 0) throw new Error("API返回空响应或图片大小为0");-->

<!--        const blob = new Blob([response], { type: 'image/png' });-->
<!--        const imageUrl = URL.createObjectURL(blob);-->

<!--        this.revokeImageUrl();-->
<!--        this.currentImageUrl = imageUrl;-->
<!--        this.currentDataContent = this.formatDataContent(data);-->
<!--        this.currentData = data;-->
<!--        this.lastUpdateTime = new Date().toLocaleTimeString();-->
<!--        this.lastRequestStatus = `✅ 二维码显示成功! (${new Date().toLocaleTimeString()})`;-->
<!--        this.lastError = "";-->
<!--        this.$message.success('检测到新二维码并已显示');-->
<!--      } catch (error) {-->
<!--        console.error("❌ 生成二维码失败:", error);-->
<!--        this.lastError = error.message;-->
<!--        this.lastRequestStatus = `❌ 生成失败: ${error.message}`;-->
<!--        this.$message.error(`二维码生成失败: ${error.message}`);-->
<!--        throw error;-->
<!--      }-->
<!--    },-->

<!--    /** 获取后端队列初始数据 */-->
<!--    async fetchInitialQueue() {-->
<!--      try {-->
<!--        const response = await checkLatestQrCode();-->
<!--        if (response.data && response.data.qrQueue) {-->
<!--          this.pendingQrQueue.push(...response.data.qrQueue);-->
<!--          console.log(`📥 获取到初始队列: ${this.pendingQrQueue.length} 条数据`);-->
<!--          this.processNextQrInQueue();-->
<!--        }-->
<!--      } catch (err) {-->
<!--        console.error("获取初始二维码队列失败", err);-->
<!--      }-->
<!--    },-->

<!--    /** 格式化数据内容 */-->
<!--    formatDataContent(data) {-->
<!--      if (!data) return "";-->
<!--      try {-->
<!--        const str = typeof data === 'string' ? data : JSON.stringify(data);-->
<!--        return str.length > 60 ? str.substring(0, 60) + '...' : str;-->
<!--      } catch (e) { return "数据格式异常"; }-->
<!--    },-->

<!--    /** 释放旧图片URL */-->
<!--    revokeImageUrl() {-->
<!--      if (this.currentImageUrl && this.currentImageUrl.startsWith('blob:')) {-->
<!--        URL.revokeObjectURL(this.currentImageUrl);-->
<!--        console.log("🔄 释放旧图片URL");-->
<!--      }-->
<!--    },-->

<!--    /** 清空显示 */-->
<!--    clearDisplay() {-->
<!--      this.revokeImageUrl();-->
<!--      this.currentImageUrl = "";-->
<!--      this.currentDataContent = "";-->
<!--      this.currentData = null;-->
<!--      this.lastProcessedId = null;-->
<!--      this.waitingForReceiveSignal = false;-->
<!--      this.isRefreshing = false;-->
<!--      console.log("🗑️ 清空显示区域");-->
<!--    },-->

<!--    /** 图片加载成功/失败 */-->
<!--    onImageLoad() {-->
<!--      console.log("✅ 二维码图片加载完成");-->
<!--      this.lastError = "";-->
<!--      this.isRefreshing = false;-->
<!--    },-->

<!--    onImageError(event) {-->
<!--      console.error("❌ 图片加载失败!", event);-->
<!--      this.lastError = "图片加载失败";-->
<!--      this.isRefreshing = false;-->
<!--      this.waitingForReceiveSignal = false; // 加载失败时也重置等待状态-->

<!--      // 加载失败时继续处理下一条-->
<!--      setTimeout(() => this.processNextQrInQueue(), 100);-->
<!--    },-->

<!--    /** WebSocket断开 */-->
<!--    disconnectWebSocket() {-->
<!--      websocketClient.disconnect();-->
<!--      this.isWsConnected = false;-->
<!--      this.wsStatus = "已断开";-->
<!--    },-->

<!--    /** 轮询机制 */-->
<!--    startPolling() {-->
<!--      if (this.pollingTimer) {-->
<!--        clearInterval(this.pollingTimer);-->
<!--        clearInterval(this.countdownTimer);-->
<!--      }-->
<!--      this.isPollingActive = true;-->
<!--      this.pollingStatus = "轮询中...";-->
<!--      this.checkForNewQrCode();-->
<!--      this.pollingTimer = setInterval(() => this.checkForNewQrCode(), this.pollingInterval);-->
<!--      this.startCountdown();-->
<!--    },-->

<!--    stopPolling() {-->
<!--      if (this.pollingTimer) {-->
<!--        clearInterval(this.pollingTimer);-->
<!--        clearInterval(this.countdownTimer);-->
<!--      }-->
<!--      this.isPollingActive = false;-->
<!--      this.pollingStatus = "已停止";-->
<!--      this.isRefreshing = false;-->
<!--    },-->

<!--    togglePolling() {-->
<!--      this.isPollingActive ? this.stopPolling() : this.startPolling();-->
<!--    },-->

<!--    startCountdown() {-->
<!--      this.countdown = this.pollingInterval / 1000;-->
<!--      if (this.countdownTimer) clearInterval(this.countdownTimer);-->
<!--      this.countdownTimer = setInterval(() => {-->
<!--        this.countdown&#45;&#45;;-->
<!--        if (this.countdown <= 0) this.countdown = this.pollingInterval / 1000;-->
<!--      }, 1000);-->
<!--    },-->

<!--    async forceCheck() {-->
<!--      console.log("🔍 手动触发立即检查");-->
<!--      await this.checkForNewQrCode();-->
<!--    },-->

<!--    async checkForNewQrCode() {-->
<!--      if (!this.isWsConnected) {-->
<!--        try {-->
<!--          this.pollingCount++;-->
<!--          this.lastCheckTime = new Date().toLocaleTimeString();-->
<!--          const response = await checkLatestQrCode();-->
<!--          if (response.data && response.data.success && response.data.hasNew) {-->
<!--            console.log("🔄 通过轮询接收到新二维码数据");-->
<!--            this.pendingQrQueue.push(response.data.qrCodeData);-->
<!--            this.processNextQrInQueue();-->
<!--          }-->
<!--        } catch (error) {-->
<!--          console.error("轮询检查失败:", error);-->
<!--          this.lastError = `轮询失败: ${error.message}`;-->
<!--        }-->
<!--      }-->
<!--    },-->

<!--    /** 测试生成二维码 */-->
<!--    async testGenerate() {-->
<!--      const testData = {-->
<!--        orderId: "TEST_" + Date.now(),-->
<!--        customerName: "测试用户",-->
<!--        amount: Math.random() * 1000,-->
<!--        status: "测试状态",-->
<!--        timestamp: new Date().toLocaleString(),-->
<!--        message: "这是测试生成的二维码",-->
<!--        testId: Math.random().toString(36).substr(2, 9)-->
<!--      };-->
<!--      await this.generateAndDisplayQrCode(testData);-->
<!--    },-->

<!--    /** 模拟外部调用 */-->
<!--    async simulateExternalCall() {-->
<!--      const externalData = {-->
<!--        orderId: "EXTERNAL_" + Date.now(),-->
<!--        source: "模拟外部系统调用",-->
<!--        data: {-->
<!--          product: "测试产品",-->
<!--          price: (Math.random() * 500 + 100).toFixed(2),-->
<!--          user: "外部用户" + Math.random().toString(36).substr(2,5)-->
<!--        },-->
<!--        timestamp: new Date().toISOString(),-->
<!--        randomId: Math.random().toString(36).substr(2, 8)-->
<!--      };-->
<!--      await autoGenerateQrCode(externalData);-->
<!--      setTimeout(() => this.forceCheck(), 500);-->
<!--    }-->
<!--  }-->
<!--};-->
<!--</script>-->

<!--<style scoped>-->
<!--.qr-code-container {-->
<!--  max-width: 800px;-->
<!--  margin: 0 auto;-->
<!--  padding: 20px;-->
<!--  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;-->
<!--}-->

<!--.qrcode-display {-->
<!--  text-align: center;-->
<!--  background: #f8f9fa;-->
<!--  border-radius: 12px;-->
<!--  padding: 24px;-->
<!--  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);-->
<!--}-->

<!--.qrcode-wrapper {-->
<!--  position: relative;-->
<!--  display: inline-block;-->
<!--  margin-bottom: 20px;-->
<!--}-->

<!--.qrcode-image {-->
<!--  max-width: 300px;-->
<!--  max-height: 300px;-->
<!--  border: 2px solid #e9ecef;-->
<!--  border-radius: 8px;-->
<!--  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);-->
<!--}-->

<!--.refresh-indicator {-->
<!--  position: absolute;-->
<!--  top: 50%;-->
<!--  left: 50%;-->
<!--  transform: translate(-50%, -50%);-->
<!--  background: rgba(255, 255, 255, 0.9);-->
<!--  padding: 12px 20px;-->
<!--  border-radius: 6px;-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  gap: 8px;-->
<!--  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);-->
<!--}-->

<!--.spinner {-->
<!--  width: 20px;-->
<!--  height: 20px;-->
<!--  border: 2px solid #f3f3f3;-->
<!--  border-top: 2px solid #007bff;-->
<!--  border-radius: 50%;-->
<!--  animation: spin 1s linear infinite;-->
<!--}-->

<!--@keyframes spin {-->
<!--  0% { transform: rotate(0deg); }-->
<!--  100% { transform: rotate(360deg); }-->
<!--}-->

<!--.data-info {-->
<!--  background: white;-->
<!--  padding: 16px;-->
<!--  border-radius: 8px;-->
<!--  text-align: left;-->
<!--  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);-->
<!--}-->

<!--.data-info div {-->
<!--  margin-bottom: 8px;-->
<!--  padding: 4px 0;-->
<!--  border-bottom: 1px solid #f1f3f4;-->
<!--}-->

<!--.data-info div:last-child {-->
<!--  border-bottom: none;-->
<!--  margin-bottom: 0;-->
<!--}-->

<!--.update-time {-->
<!--  color: #6c757d;-->
<!--  font-size: 14px;-->
<!--}-->

<!--.data-content {-->
<!--  font-weight: 500;-->
<!--  color: #495057;-->
<!--  word-break: break-all;-->
<!--}-->

<!--.request-source {-->
<!--  color: #007bff;-->
<!--  font-weight: 500;-->
<!--}-->

<!--.request-count {-->
<!--  color: #28a745;-->
<!--  font-weight: 500;-->
<!--}-->

<!--.polling-info {-->
<!--  color: #6c757d;-->
<!--  font-size: 14px;-->
<!--}-->

<!--.queue-info {-->
<!--  color: #fd7e14;-->
<!--  font-weight: 500;-->
<!--  font-size: 14px;-->
<!--}-->

<!--.empty-state {-->
<!--  text-align: center;-->
<!--  padding: 60px 20px;-->
<!--  background: #f8f9fa;-->
<!--  border-radius: 12px;-->
<!--  border: 2px dashed #dee2e6;-->
<!--}-->

<!--.empty-content {-->
<!--  max-width: 400px;-->
<!--  margin: 0 auto;-->
<!--}-->

<!--.empty-icon {-->
<!--  font-size: 64px;-->
<!--  margin-bottom: 16px;-->
<!--}-->

<!--.empty-text {-->
<!--  font-size: 20px;-->
<!--  font-weight: 600;-->
<!--  color: #6c757d;-->
<!--  margin-bottom: 8px;-->
<!--}-->

<!--.empty-tip {-->
<!--  color: #adb5bd;-->
<!--  margin-bottom: 24px;-->
<!--}-->

<!--.request-status {-->
<!--  padding: 12px;-->
<!--  border-radius: 6px;-->
<!--  margin-bottom: 20px;-->
<!--  font-weight: 500;-->
<!--}-->

<!--.status-success {-->
<!--  background: #d4edda;-->
<!--  color: #155724;-->
<!--  border: 1px solid #c3e6cb;-->
<!--}-->

<!--.status-error {-->
<!--  background: #f8d7da;-->
<!--  color: #721c24;-->
<!--  border: 1px solid #f5c6cb;-->
<!--}-->

<!--.status-loading {-->
<!--  background: #cce7ff;-->
<!--  color: #004085;-->
<!--  border: 1px solid #b3d7ff;-->
<!--}-->

<!--.status-default {-->
<!--  background: #e2e3e5;-->
<!--  color: #383d41;-->
<!--  border: 1px solid #d6d8db;-->
<!--}-->

<!--.polling-controls {-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  justify-content: center;-->
<!--  gap: 12px;-->
<!--  margin-bottom: 24px;-->
<!--  flex-wrap: wrap;-->
<!--}-->

<!--.control-btn {-->
<!--  padding: 10px 20px;-->
<!--  border: none;-->
<!--  border-radius: 6px;-->
<!--  background: #007bff;-->
<!--  color: white;-->
<!--  font-weight: 500;-->
<!--  cursor: pointer;-->
<!--  transition: all 0.2s;-->
<!--}-->

<!--.control-btn:hover {-->
<!--  background: #0056b3;-->
<!--  transform: translateY(-1px);-->
<!--}-->

<!--.control-btn.active {-->
<!--  background: #dc3545;-->
<!--}-->

<!--.control-btn.active:hover {-->
<!--  background: #c82333;-->
<!--}-->

<!--.control-btn.secondary {-->
<!--  background: #6c757d;-->
<!--}-->

<!--.control-btn.secondary:hover {-->
<!--  background: #545b62;-->
<!--}-->

<!--.polling-timer {-->
<!--  color: #6c757d;-->
<!--  font-size: 14px;-->
<!--  padding: 8px 12px;-->
<!--  background: white;-->
<!--  border-radius: 4px;-->
<!--  border: 1px solid #dee2e6;-->
<!--}-->

<!--.empty-actions {-->
<!--  display: flex;-->
<!--  gap: 12px;-->
<!--  justify-content: center;-->
<!--  flex-wrap: wrap;-->
<!--}-->

<!--.test-btn {-->
<!--  padding: 10px 20px;-->
<!--  border: 1px solid #007bff;-->
<!--  border-radius: 6px;-->
<!--  background: white;-->
<!--  color: #007bff;-->
<!--  font-weight: 500;-->
<!--  cursor: pointer;-->
<!--  transition: all 0.2s;-->
<!--}-->

<!--.test-btn:hover {-->
<!--  background: #007bff;-->
<!--  color: white;-->
<!--  transform: translateY(-1px);-->
<!--}-->

<!--.test-btn.secondary {-->
<!--  border-color: #28a745;-->
<!--  color: #28a745;-->
<!--}-->

<!--.test-btn.secondary:hover {-->
<!--  background: #28a745;-->
<!--  color: white;-->
<!--}-->

<!--.debug-info {-->
<!--  margin-top: 30px;-->
<!--  padding: 20px;-->
<!--  background: #f8f9fa;-->
<!--  border-radius: 8px;-->
<!--  border-left: 4px solid #007bff;-->
<!--}-->

<!--.debug-info h4 {-->
<!--  margin: 0 0 16px 0;-->
<!--  color: #495057;-->
<!--  font-size: 16px;-->
<!--}-->

<!--.debug-info p {-->
<!--  margin: 6px 0;-->
<!--  padding: 4px 0;-->
<!--  font-size: 14px;-->
<!--  color: #6c757d;-->
<!--  border-bottom: 1px solid #e9ecef;-->
<!--}-->

<!--.debug-info p:last-child {-->
<!--  border-bottom: none;-->
<!--}-->
<!--</style>-->


<template>
  <div class="qr-code-container">
    <!-- 主显示区域 -->
    <div class="display-area">
      <!-- 当前二维码显示 -->
      <div class="current-qrcode-section">
        <div class="section-header">
          <h3>当前扫描二维码</h3>
          <div class="refresh-info">
            <span class="refresh-count">刷新次数: {{ currentRefreshCount }}/10</span>
            <span class="refresh-timer">刷新: {{ refreshCountdown }}秒</span>
          </div>
        </div>

        <div v-if="currentImageUrl || (framebufferMode && currentData)" class="qrcode-display">
          <div class="qrcode-wrapper">
            <img
              v-if="currentImageUrl"
              :src="currentImageUrl"
              class="qrcode-image"
              alt="二维码"
              @load="onImageLoad"
              @error="onImageError"
            />
            <div v-else class="mipi-display-hint">
              <div class="mipi-icon">🖥️</div>
              <p>二维码已在 MIPI 屏显示</p>
              <p class="mipi-sub">/dev/fb0 · show_qr.py</p>
            </div>
            <div class="refresh-indicator" v-if="isRefreshing">
              <div class="spinner"></div>
              <span>刷新中...</span>
            </div>
          </div>

          <div class="current-data-info">
<!--            <div class="info-item">-->
<!--              <label>数据ID:</label>-->
<!--              <span>{{ currentDataId || '暂无' }}</span>-->
<!--            </div>-->
<!--            <div class="info-item">-->
<!--              <label>内容:</label>-->
<!--              <span class="content-text">{{ currentDataContent }}</span>-->
<!--            </div>-->
<!--            <div class="info-item">-->
<!--              <label>接收手机:</label>-->
<!--              <span>{{ currentReceivePhone || '暂无' }}</span>-->
<!--            </div>-->
            <div class="info-item">
              <label>生成时间:</label>
              <span>{{ lastUpdateTime }}</span>
            </div>
            <div class="info-item status" :class="currentStatusClass">
              <label>状态:</label>
              <span>{{ currentStatusText }}</span>
            </div>
          </div>
        </div>

        <!-- 空白状态 -->
        <div v-else class="empty-state">
          <div class="empty-content">
            <div class="empty-icon">📭</div>
            <div class="empty-text">暂无待扫描二维码</div>
            <div class="empty-tip">等待队列一数据生成二维码</div>
            <div class="request-status" :class="statusClass">
              {{ lastRequestStatus }}
            </div>
          </div>
        </div>
      </div>

      <!-- 队列状态显示 -->
      <div class="queues-section">
        <div class="queues-container">
          <!-- 队列一：待处理数据 -->
          <div class="queue-card queue1">
            <div class="queue-header">
              <h4>队列一 - 待处理数据</h4>
              <span class="queue-count">{{ queue1Data.length }}</span>
            </div>
            <div class="queue-list">
              <div v-for="(item, index) in queue1Data" :key="item._dataId"
                   class="queue-item" :class="{ 'processing': index === 0 }">
                <div class="item-index">#{{ index + 1 }}</div>
                <div class="item-content">
                  <div class="item-id">{{ item._dataId }}</div>
                  <div class="item-text">{{ item.content }}</div>
                  <div class="item-phone">{{ item.receivePhone }}</div>
                </div>
                <div class="item-status" v-if="index === 0">即将处理</div>
              </div>
              <div v-if="queue1Data.length === 0" class="empty-queue">
                暂无待处理数据
              </div>
            </div>
          </div>

          <!-- 队列二：未成功数据 -->
          <div class="queue-card queue2">
            <div class="queue-header">
              <h4>队列二 - 未成功数据</h4>
              <span class="queue-count">{{ queue2Data.length }}</span>
              <div class="queue-actions">
                <button @click="exportFailedData" class="export-btn"
                        :disabled="queue2Data.length === 0">
                  导出全部
                </button>
              </div>
            </div>
            <div class="queue-list">
              <div v-for="(item, index) in queue2Data" :key="item._dataId"
                   class="queue-item failed">
                <div class="item-index">#{{ index + 1 }}</div>
                <div class="item-content">
                  <div class="item-id">{{ item._dataId }}</div>
                  <div class="item-text">{{ item.content }}</div>
                  <div class="item-phone">{{ item.receivePhone }}</div>
                  <div class="fail-reason">{{ item._failReason }}</div>
                </div>
                <div class="item-status failed">失败</div>
              </div>
              <div v-if="queue2Data.length === 0" class="empty-queue">
                暂无未成功数据
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 控制面板 -->
    <div class="control-panel">
      <div class="panel-section">
        <h4>系统控制</h4>
        <div class="control-buttons">
          <button @click="startQueueProcessing" class="control-btn success"
                  :disabled="queueProcessingActive">
            启动队列处理
          </button>
          <button @click="stopQueueProcessing" class="control-btn danger"
                  :disabled="!queueProcessingActive">
            停止队列处理
          </button>
          <button @click="clearAllQueues" class="control-btn warning">
            清空所有队列
          </button>
          <button @click="refreshQueueStatus" class="control-btn info">
            刷新状态
          </button>
        </div>
      </div>

      <div class="panel-section">
        <h4>轮询控制</h4>
        <div class="polling-controls">
          <button @click="togglePolling" class="control-btn"
                  :class="{ 'active': isPollingActive }">
            {{ isPollingActive ? '停止轮询' : '开始轮询' }}
          </button>
          <span class="polling-info">
            状态: {{ pollingStatus }} ({{ pollingCount }}次)
          </span>
        </div>
      </div>

      <div class="panel-section">
        <h4>系统状态</h4>
        <div class="status-info">
          <div class="status-item">
            <label>WebSocket:</label>
            <span :class="wsStatusClass">{{ wsStatus }}</span>
          </div>
          <div class="status-item">
            <label>队列处理:</label>
            <span :class="queueStatusClass">{{ queueProcessingStatus }}</span>
          </div>
          <div class="status-item">
            <label>总调用次数:</label>
            <span>{{ requestCount }}</span>
          </div>
          <div class="status-item">
            <label>最后检查:</label>
            <span>{{ lastCheckTime || '从未检查' }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 调试信息（可选） -->
    <div v-if="showDebug" class="debug-info">
      <h4>调试信息</h4>
      <div class="debug-content">
        <p>当前处理ID: {{ lastProcessedId || '无' }}</p>
        <p>等待信号: {{ waitingForReceiveSignal ? '是' : '否' }}</p>
        <p>待处理队列: {{ pendingQrQueue.length }} 条</p>
        <p>最后请求状态: {{ lastRequestStatus }}</p>
      </div>
    </div>
  </div>
</template>

<script>
import {
  autoGenerateQrCode,
  getDisplayConfig,
  getAllQueueStatus,
  exportAndClearFailedData,
  exportFailedData,
  startQueue,
  stopQueue,
  clearAllQueues
} from "@/api/sms/qrcode";
import websocketClient from "@/utils/websocket";
import item from "@/layout/components/Sidebar/Item.vue";

export default {
  name: "QrCodeDisplay",
  data() {
    return {
      // 当前二维码数据
      currentImageUrl: "",
      currentDataContent: "",
      currentData: null,
      currentDataId: "",
      currentReceivePhone: "",
      currentRefreshCount: 0,
      currentStatus: "waiting", // waiting, processing, success, failed

      // 队列数据
      queue1Data: [],
      queue2Data: [],

      // 系统状态
      lastUpdateTime: null,
      lastRequestStatus: "系统初始化中...",
      requestCount: 0,
      wsStatus: "未连接",
      isWsConnected: false,

      // 轮询控制
      isPollingActive: true,
      pollingInterval: 3000, // 3秒
      pollingTimer: null,
      pollingCount: 0,
      pollingStatus: "未开始",
      lastCheckTime: null,
      refreshCountdown: 3,
      countdownTimer: null,

      // 处理状态
      isRefreshing: false,
      lastProcessedId: null,
      waitingForReceiveSignal: false,
      pendingQrQueue: [],

      // 队列处理状态
      queueProcessingActive: false,
      queueProcessingStatus: "未启动",

      // 调试
      showDebug: false,

      autoRefreshTimer: null,
      refreshInterval: 3000, // 3秒刷新一次

      // MIPI framebuffer 显示模式（后端 show_qr.py）
      framebufferMode: false,
    };
  },

  computed: {
    statusClass() {
      if (this.lastRequestStatus.includes('成功')) return 'status-success';
      if (this.lastRequestStatus.includes('失败')) return 'status-error';
      if (this.lastRequestStatus.includes('正在')) return 'status-loading';
      return 'status-default';
    },

    currentStatusClass() {
      switch (this.currentStatus) {
        case 'processing': return 'status-processing';
        case 'success': return 'status-success';
        case 'failed': return 'status-failed';
        default: return 'status-waiting';
      }
    },

    currentStatusText() {
      switch (this.currentStatus) {
        case 'processing': return `处理中 (${this.currentRefreshCount}/10)`;
        case 'success': return '成功';
        case 'failed': return '失败';
        default: return '等待中';
      }
    },

    wsStatusClass() {
      return this.isWsConnected ? 'status-connected' : 'status-disconnected';
    },

    queueStatusClass() {
      return this.queueProcessingActive ? 'status-active' : 'status-inactive';
    },

    autoRefreshStatus() {
      return this.autoRefreshTimer ? '运行中' : '已停止';
    },
    autoRefreshStatusClass() {
      return this.autoRefreshTimer ? 'status-active' : 'status-inactive';
    }
  },

  mounted() {
    console.log("=== 启动二维码显示系统 ===");
    this.loadDisplayConfig();
    this.initWebSocket();
    this.startPolling();
    this.refreshQueueStatus();

    this.startAutoRefresh(); // 启动自动刷新
  },

  beforeUnmount() {
    this.disconnectWebSocket();
    this.stopPolling();
    this.revokeImageUrl();
    this.stopAutoRefresh(); // 停止自动刷新
  },

  methods: {
    /** 加载显示模式配置 */
    async loadDisplayConfig() {
      try {
        const response = await getDisplayConfig();
        if (response.data) {
          this.framebufferMode = response.data.framebufferMode === true;
          if (this.framebufferMode) {
            this.lastRequestStatus = "MIPI 屏显示模式（/dev/fb0）";
          }
        }
      } catch (error) {
        console.warn("获取显示配置失败，使用浏览器模式", error);
        this.framebufferMode = false;
      }
    },

    /** 初始化WebSocket */
    async initWebSocket() {
      try {
        this.wsStatus = "连接中...";
        await websocketClient.connect();
        this.isWsConnected = true;
        this.wsStatus = "已连接";

        websocketClient.subscribe('/topic/qrCodeUpdates', (message) => {
          console.log('📨 接收到WebSocket消息:', message);
          this.handleQrCodeMessage(message);
        });

        this.lastRequestStatus = "WebSocket已连接";
      } catch (error) {
        console.error("WebSocket连接失败:", error);
        this.wsStatus = `连接失败: ${error.message}`;
        this.lastRequestStatus = "WebSocket连接失败，使用轮询模式";
      }
    },

    /** 处理WebSocket消息 */
    async handleQrCodeMessage(message) {
      try {
        console.log('📨 接收到WebSocket消息:', message);

        // 处理清空指令
        if (message.clear || message.type === 'CLEAR_ALL') {
          this.clearDisplay();
          this.waitingForReceiveSignal = false;
          this.lastProcessedId = null;
          console.log("🗑️ 收到清空指令，重置所有状态");
          return;
        }

        // 处理接收确认信号
        if (message.receive_signal === true || message.type === 'DATA_SUCCESS') {
          console.log("✅ 收到成功信号，完成当前数据处理");
          this.currentStatus = 'success';
          this.waitingForReceiveSignal = false;
          this.lastProcessedId = null;

          // 短暂延迟后处理下一条
          setTimeout(() => {
            this.processNextQrInQueue();
          }, 500);
          return;
        }

        // 处理数据失败
        if (message.type === 'DATA_FAILED') {
          console.log("❌ 数据标记为失败:", message.dataId);
          this.currentStatus = 'failed';
          // 继续处理下一条
          setTimeout(() => {
            this.processNextQrInQueue();
          }, 1000);
          return;
        }

        // 处理队列状态更新
        if (message.type === 'QUEUE_STATUS_UPDATE') {
          this.refreshQueueStatus();
          return;
        }

        // 处理二维码更新
        if (message.type === 'QR_CODE_UPDATE' && message.data) {
          console.log("📨 收到新二维码数据:", message.data);
          this.handleNewQrCodeData(message.data);
        }
      } catch (error) {
        console.error("处理WebSocket消息失败:", error);
      }
    },
    /** 启动自动刷新 */
    startAutoRefresh() {
      this.stopAutoRefresh();
      console.log("🔄 启动自动刷新，间隔:", this.refreshInterval + "ms");

      this.autoRefreshTimer = setInterval(() => {
        this.autoRefreshQrCode();
      }, this.refreshInterval);
    },

    /** 停止自动刷新 */
    stopAutoRefresh() {
      if (this.autoRefreshTimer) {
        clearInterval(this.autoRefreshTimer);
        this.autoRefreshTimer = null;
        console.log("⏹️ 停止自动刷新");
      }
    },

    /** 自动刷新二维码 */
    async autoRefreshQrCode() {
      // 如果没有当前数据，不进行刷新
      if (!this.currentData) {
        return;
      }

      // 如果正在刷新中，跳过
      if (this.isRefreshing) {
        return;
      }

      try {
        this.isRefreshing = true;
        this.currentRefreshCount++;

        console.log(`🔄 自动刷新二维码 (${this.currentRefreshCount}/10)`, this.currentData);

        // 重新生成二维码
        await this.generateAndDisplayQrCode(this.currentData);

        // 如果达到10次刷新，标记为失败
        if (this.currentRefreshCount >= 10) {
          console.log("❌ 刷新达到10次，标记为失败");
          this.currentStatus = 'failed';
          this.stopAutoRefresh();
          // 延迟后处理下一条
          setTimeout(() => {
            this.processNextQrInQueue();
          }, 1000);
        }

      } catch (error) {
        console.error("❌ 自动刷新失败:", error);
      } finally {
        this.isRefreshing = false;
      }
    },

    /** 处理新二维码数据 */
    handleNewQrCodeData(data) {
      console.log("📨 收到新二维码数据:", data);

      // const exists = this.pendingQrQueue.some(item =>
      //   item._dataId === data._dataId
      // );
      // if(!exists){
      //   this.pendingQrQueue.push(data);
      //   console.log(`数据加入队列，ID：${data._dataId}`)
      // }

      // 设置当前数据
      this.currentData = data;
      this.currentDataId = this.generateDataId(data);
      this.currentReceivePhone = data.receivePhone;
      this.currentDataContent = data.content;
      this.currentStatus = 'processing';

      // 立即生成二维码
      this.generateAndDisplayQrCode(data);

      // 启动自动刷新
      this.startAutoRefresh();
      // 如果没有在等待信号，立即处理
      if (!this.waitingForReceiveSignal) {
        this.processNextQrInQueue();
      }
    },

    /** 生成数据ID */
    generateDataId(data) {
      return 'DATA_' + JSON.stringify(data).hashCode() + '_' + Date.now();
    },

    /** 处理WebSocket消息 */
    // async handleQrCodeMessage(message) {
    //   try {
    //     console.log('📨 接收到WebSocket消息:', message);
    //
    //     // 处理二维码更新
    //     if (message.type === 'QR_CODE_UPDATE' && message.data) {
    //       this.handleNewQrCodeData(message.data);
    //       return;
    //     }
    //
    //     // 处理数据成功
    //     if (message.type === 'DATA_SUCCESS') {
    //       console.log("✅ 收到成功信号，完成当前数据处理");
    //       this.currentStatus = 'success';
    //       this.stopAutoRefresh();
    //
    //       // 短暂延迟后清空显示
    //       setTimeout(() => {
    //         this.clearCurrentDisplay();
    //       }, 1000);
    //       return;
    //     }
    //
    //     // 处理数据失败
    //     if (message.type === 'DATA_FAILED') {
    //       console.log("❌ 数据标记为失败:", message.dataId);
    //       this.currentStatus = 'failed';
    //       this.stopAutoRefresh();
    //
    //       // 延迟后清空显示
    //       setTimeout(() => {
    //         this.clearCurrentDisplay();
    //       }, 2000);
    //       return;
    //     }
    //
    //     // 处理队列状态更新
    //     if (message.type === 'QUEUE_STATUS_UPDATE') {
    //       this.refreshQueueStatus();
    //       return;
    //     }
    //
    //   } catch (error) {
    //     console.error("处理WebSocket消息失败:", error);
    //   }
    // },



    // /** 处理新二维码数据 */
    // handleNewQrCodeData(data) {
    //   // 检查是否已经在队列中
    //   const exists = this.pendingQrQueue.some(item =>
    //     item._dataId === data._dataId
    //   );
    //
    //   if (!exists) {
    //     this.pendingQrQueue.push(data);
    //     console.log(`📥 新数据加入队列，ID: ${data._dataId}`);
    //   }
    //
    //   // 如果没有在等待信号，立即处理
    //   if (!this.waitingForReceiveSignal) {
    //     this.processNextQrInQueue();
    //   }
    // },

    /** 处理队列中的下一条二维码 */
    async processNextQrInQueue() {
      if (this.waitingForReceiveSignal || this.isRefreshing) {
        console.log("⏳ 等待信号中或正在刷新，暂不处理新二维码");
        return;
      }

      if (this.pendingQrQueue.length === 0) {
        console.log("📭 队列为空，无待处理二维码");
        this.clearCurrentDisplay();
        return;
      }

      const nextData = this.pendingQrQueue[0];
      const dataId = nextData._dataId;

      // 检查是否已经处理过这个ID
      if (this.lastProcessedId === dataId) {
        console.log("🔄 跳过已处理的二维码:", dataId);
        this.pendingQrQueue.shift();
        setTimeout(() => this.processNextQrInQueue(), 50);
        return;
      }

      console.log("➡️ 处理队列中下一条二维码:", nextData);
      this.pendingQrQueue.shift();

      await this.processNewQrCode(nextData);
    },

    /** 处理新二维码 */
    async processNewQrCode(data) {
      try {
        const dataId = data._dataId;

        this.lastProcessedId = dataId;
        this.requestCount++;
        this.currentStatus = 'processing';
        this.currentRefreshCount = data._refreshCount || 1;
        this.currentDataId = dataId;
        this.currentReceivePhone = data.receivePhone;
        this.currentDataContent = data.content;

        console.log(`📡 生成二维码中，刷新次数: ${this.currentRefreshCount}`, data);
        this.lastRequestStatus = "正在生成二维码...";
        this.isRefreshing = true;

        await this.generateAndDisplayQrCode(data);

        // 设置等待信号状态
        this.waitingForReceiveSignal = true;
        console.log(`🕓 等待外部信号确认 (${this.currentRefreshCount}/10)`);

      } catch (e) {
        console.error("❌ 处理二维码失败:", e);
        this.isRefreshing = false;
        this.waitingForReceiveSignal = false;
        setTimeout(() => this.processNextQrInQueue(), 100);
      }
    },

    /** 生成并显示二维码 */
    async generateAndDisplayQrCode(data) {
      try {
        this.lastUpdateTime = new Date().toLocaleTimeString();

        if (this.framebufferMode) {
          // 后端 onQrCodeUpdate 已调用 show_qr.py 写入 /dev/fb0
          this.revokeImageUrl();
          this.currentImageUrl = "";
          this.lastRequestStatus = "✅ MIPI 屏二维码已刷新";
          return;
        }

        this.lastRequestStatus = "调用API生成二维码中...";

        const response = await autoGenerateQrCode(data);
        if (!response || response.size === 0) {
          throw new Error("API返回空响应");
        }

        const blob = new Blob([response], { type: 'image/png' });
        const imageUrl = URL.createObjectURL(blob);

        this.revokeImageUrl();
        this.currentImageUrl = imageUrl;
        this.lastRequestStatus = `✅ 二维码显示成功!`;

        this.$message.success('二维码已更新');

      } catch (error) {
        console.error("❌ 生成二维码失败:", error);
        this.lastRequestStatus = `❌ 生成失败: ${error.message}`;
        this.$message.error(`二维码生成失败: ${error.message}`);
        throw error;
      }
    },

    /** 刷新队列状态 */
    async refreshQueueStatus() {
      try {
        const response = await getAllQueueStatus();
        if (response.data) {
          this.queue1Data = response.data.queue1Data || [];
          this.queue2Data = response.data.queue2Data || [];
          this.queueProcessingActive = response.data.currentProcessing || false;
          this.queueProcessingStatus = this.queueProcessingActive ? '运行中' : '已停止';

          // 更新当前处理数据的刷新次数
          if (response.data.currentDataDetail) {
            this.currentRefreshCount = response.data.currentDataDetail._refreshCount || 0;
          }
        }
      } catch (error) {
        console.error("刷新队列状态失败:", error);
      }
    },

    /** 导出失败数据 */
    // async exportFailedData() {
    //   try {
    //     await exportAndClearFailedData();
    //     this.$message.success('失败数据导出成功');
    //     this.refreshQueueStatus();
    //   } catch (error) {
    //     this.$message.error('导出失败: ' + error.message);
    //   }
    // },
    /** 导出失败数据为JSON文件 - 完整错误处理版本 */
    // async exportFailedData() {
    //   if (this.queue2Data.length === 0) {
    //     this.$message.warning('队列二中暂无失败数据');
    //     return;
    //   }
    //
    //   // 添加加载状态
    //   const loading = this.$loading({
    //     lock: true,
    //     text: `正在导出 ${this.queue2Data.length} 条数据...`,
    //     spinner: 'el-icon-loading',
    //     background: 'rgba(0, 0, 0, 0.7)'
    //   });
    //
    //   try {
    //     // 调用API
    //     const blobResponse = await exportFailedData();
    //
    //     // 检查是否是有效的Blob
    //     if (!(blobResponse instanceof Blob)) {
    //       throw new Error('服务器返回的数据格式不正确');
    //     }
    //
    //     // 检查Blob大小
    //     if (blobResponse.size === 0) {
    //       throw new Error('导出的文件为空');
    //     }
    //
    //     // 创建下载
    //     const blob = new Blob([blobResponse], {
    //       type: 'application/json'
    //     });
    //
    //     const downloadUrl = URL.createObjectURL(blob);
    //     const link = document.createElement('a');
    //     link.href = downloadUrl;
    //
    //     // 使用时间戳生成文件名
    //     const now = new Date();
    //     const fileName = `失败数据导出_${now.getFullYear()}${(now.getMonth()+1).toString().padStart(2, '0')}${now.getDate().toString().padStart(2, '0')}_${now.getHours().toString().padStart(2, '0')}${now.getMinutes().toString().padStart(2, '0')}${now.getSeconds().toString().padStart(2, '0')}.json`;
    //
    //     link.download = fileName;
    //     document.body.appendChild(link);
    //     link.click();
    //     document.body.removeChild(link);
    //
    //     // 清理URL
    //     setTimeout(() => {
    //       URL.revokeObjectURL(downloadUrl);
    //     }, 100);
    //
    //     this.$message.success({
    //       message: `JSON文件导出成功！\n文件名: ${fileName}\n数据量: ${this.queue2Data.length}条`,
    //       duration: 5000
    //     });
    //
    //     console.log('📁 导出完成:', fileName);
    //
    //   } catch (error) {
    //     console.error('导出失败:', error);
    //
    //     let errorMessage = '导出失败';
    //
    //     // 处理不同类型的错误
    //     if (error instanceof Blob) {
    //       // 如果是Blob类型的错误响应
    //       try {
    //         const errorText = await new Promise((resolve, reject) => {
    //           const reader = new FileReader();
    //           reader.onload = () => resolve(reader.result);
    //           reader.onerror = reject;
    //           reader.readAsText(error);
    //         });
    //         const errorData = JSON.parse(errorText);
    //         errorMessage = errorData.message || errorMessage;
    //       } catch {
    //         errorMessage = '服务器返回了错误信息';
    //       }
    //     } else if (error.response) {
    //       errorMessage = error.response.data?.message || errorMessage;
    //     } else if (error.message) {
    //       errorMessage = error.message;
    //     }
    //
    //     this.$message.error(errorMessage);
    //
    //   } finally {
    //     loading.close();
    //   }
    // },

    /** 导出失败数据为纯JSON文件 */
    async exportFailedData() {
      if (this.queue2Data.length === 0) {
        this.$message.warning('队列二中暂无失败数据可导出');
        return;
      }

      try {
        this.$message.info(`正在导出 ${this.queue2Data.length} 条失败数据...`);

        // 调用API获取blob数据
        const blobData = await exportFailedData();

        // 创建下载链接
        const blob = new Blob([blobData], {
          type: 'application/json; charset=utf-8'
        });

        const downloadUrl = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = downloadUrl;

        // 生成文件名
        const timestamp = new Date().getTime();
        const fileName = `failed_data_${timestamp}.json`;

        link.download = fileName;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);

        // 释放URL对象
        window.URL.revokeObjectURL(downloadUrl);

        this.$message.success(`JSON文件导出成功，共${this.queue2Data.length}条数据`);

      } catch (error) {
        console.error('导出失败:', error);
        this.$message.error('导出失败: ' + (error.message || '未知错误'));
      }
    },
    /** 读取Blob为文本 */
    readBlobAsText(blob) {
      return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(reader.result);
        reader.onerror = reject;
        reader.readAsText(blob);
      });
    },

    /** 启动队列处理 */
    async startQueueProcessing() {
      try {
        await startQueue();
        this.queueProcessingActive = true;
        this.queueProcessingStatus = '运行中';
        this.$message.success('队列处理已启动');
      } catch (error) {
        this.$message.error('启动失败: ' + error.message);
      }
    },

    /** 停止队列处理 */
    async stopQueueProcessing() {
      try {
        await stopQueue();
        this.queueProcessingActive = false;
        this.queueProcessingStatus = '已停止';
        this.$message.success('队列处理已停止');
      } catch (error) {
        this.$message.error('停止失败: ' + error.message);
      }
    },

    /** 清空所有队列 */
    async clearAllQueues() {
      try {
        await clearAllQueues();
        this.clearDisplay();
        this.pendingQrQueue = [];
        this.refreshQueueStatus();
        this.$message.success('所有队列已清空');
      } catch (error) {
        this.$message.error('清空失败: ' + error.message);
      }
    },

    /** 清空当前显示 */
    clearCurrentDisplay() {
      this.currentImageUrl = "";
      this.currentDataContent = "";
      this.currentData = null;
      this.currentDataId = "";
      this.currentReceivePhone = "";
      this.currentStatus = "waiting";
      this.currentRefreshCount = 0;
    },

    /** 清空显示 */
    clearDisplay() {
      this.revokeImageUrl();
      this.clearCurrentDisplay();
      this.lastProcessedId = null;
      this.waitingForReceiveSignal = false;
      this.isRefreshing = false;
    },

    /** 释放图片URL */
    revokeImageUrl() {
      if (this.currentImageUrl && this.currentImageUrl.startsWith('blob:')) {
        URL.revokeObjectURL(this.currentImageUrl);
      }
    },

    /** 图片加载事件 */
    onImageLoad() {
      console.log("✅ 二维码图片加载完成");
      this.isRefreshing = false;
    },

    onImageError(event) {
      console.error("❌ 图片加载失败!", event);
      this.isRefreshing = false;
      this.waitingForReceiveSignal = false;
      setTimeout(() => this.processNextQrInQueue(), 100);
    },

    /** WebSocket断开 */
    disconnectWebSocket() {
      websocketClient.disconnect();
      this.isWsConnected = false;
      this.wsStatus = "已断开";
    },

    /** 轮询机制 */
    startPolling() {
      this.stopPolling();
      this.isPollingActive = true;
      this.pollingStatus = "轮询中...";
      this.checkForUpdates();
      this.pollingTimer = setInterval(() => this.checkForUpdates(), this.pollingInterval);
      this.startCountdown();
    },

    stopPolling() {
      if (this.pollingTimer) {
        clearInterval(this.pollingTimer);
        clearInterval(this.countdownTimer);
      }
      this.isPollingActive = false;
      this.pollingStatus = "已停止";
      this.isRefreshing = false;
    },

    togglePolling() {
      this.isPollingActive ? this.stopPolling() : this.startPolling();
    },

    startCountdown() {
      this.refreshCountdown = this.pollingInterval / 1000;
      if (this.countdownTimer) clearInterval(this.countdownTimer);
      this.countdownTimer = setInterval(() => {
        this.refreshCountdown--;
        if (this.refreshCountdown <= 0) this.refreshCountdown = this.pollingInterval / 1000;
      }, 1000);
    },

    async checkForUpdates() {
      if (!this.isWsConnected) {
        try {
          this.pollingCount++;
          this.lastCheckTime = new Date().toLocaleTimeString();
          await this.refreshQueueStatus();
        } catch (error) {
          console.error("轮询检查失败:", error);
        }
      }
    }
  }
};
</script>

<style scoped>
/* 队列样式 - 保持原有高度，添加滚动 */
.queues-section {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.queues-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  /* 移除固定高度，让内容自然扩展 */
}

.queue-card {
  background: white;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  /* 设置队列卡片固定高度 */
  height: 400px;
}

.queue1 {
  border-left: 4px solid #007bff;
}

.queue2 {
  border-left: 4px solid #dc3545;
}

.queue-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e9ecef;
  flex-shrink: 0; /* 防止头部被压缩 */
}

.queue-header h4 {
  margin: 0;
  font-size: 16px;
}

.queue-count {
  background: #6c757d;
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.queue1 .queue-count {
  background: #007bff;
}

.queue2 .queue-count {
  background: #dc3545;
}

.queue-actions {
  display: flex;
  gap: 8px;
}

.export-btn {
  padding: 4px 8px;
  background: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
}

.export-btn:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

/* 队列列表滚动样式 - 关键修改 */
.queue-list {
  flex: 1; /* 占据剩余空间 */
  overflow-y: auto; /* 启用垂直滚动 */
  min-height: 0; /* 重要：允许内容区域收缩 */
}

/* 自定义滚动条样式 */
.queue-list::-webkit-scrollbar {
  width: 6px;
}

.queue-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.queue-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.queue-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* Firefox 滚动条样式 */
.queue-list {
  scrollbar-width: thin;
  scrollbar-color: #c1c1c1 #f1f1f1;
}

.queue-item {
  display: flex;
  align-items: flex-start;
  padding: 10px;
  margin-bottom: 8px;
  border-radius: 6px;
  border: 1px solid #e9ecef;
  background: #f8f9fa;
  min-height: 60px;
}

.queue-item.processing {
  background: #e3f2fd;
  border-color: #007bff;
}

.queue-item.failed {
  background: #f8d7da;
  border-color: #dc3545;
}

.item-index {
  background: #6c757d;
  color: white;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  margin-right: 10px;
  flex-shrink: 0;
}

.processing .item-index {
  background: #007bff;
}

.failed .item-index {
  background: #dc3545;
}

.item-content {
  flex: 1;
  min-width: 0; /* 重要：防止内容溢出 */
  overflow: hidden;
}

.item-id {
  font-size: 12px;
  color: #6c757d;
  margin-bottom: 4px;
  word-break: break-all;
}

.item-text {
  font-size: 14px;
  color: #495057;
  margin-bottom: 4px;
  word-break: break-all;
  line-height: 1.4;
}

.item-phone {
  font-size: 12px;
  color: #007bff;
  word-break: break-all;
}

.fail-reason {
  font-size: 11px;
  color: #dc3545;
  margin-top: 4px;
  word-break: break-all;
  font-style: italic;
}

.item-status {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 4px;
  background: #6c757d;
  color: white;
  flex-shrink: 0;
  align-self: flex-start;
}

.item-status.failed {
  background: #dc3545;
}

.empty-queue {
  text-align: center;
  color: #6c757d;
  font-style: italic;
  padding: 40px 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

/* 其他样式保持不变 */
.qr-code-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.display-area {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.current-qrcode-section {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #e9ecef;
}

.section-header h3 {
  margin: 0;
  color: #495057;
  font-size: 18px;
}

.refresh-info {
  display: flex;
  gap: 15px;
  font-size: 14px;
}

.refresh-count {
  color: #007bff;
  font-weight: 500;
}

.refresh-timer {
  color: #28a745;
  font-weight: 500;
}

.qrcode-display {
  text-align: center;
}

.qrcode-wrapper {
  position: relative;
  display: inline-block;
  margin-bottom: 20px;
}

.qrcode-image {
  max-width: 300px;
  max-height: 300px;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.mipi-display-hint {
  width: 300px;
  height: 300px;
  margin: 0 auto;
  border: 2px dashed #409EFF;
  border-radius: 8px;
  background: #f0f9ff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #303133;
}

.mipi-display-hint .mipi-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.mipi-display-hint .mipi-sub {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.refresh-indicator {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(255, 255, 255, 0.9);
  padding: 12px 20px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #f3f3f3;
  border-top: 2px solid #007bff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.current-data-info {
  background: white;
  padding: 16px;
  border-radius: 8px;
  text-align: left;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.info-item {
  display: flex;
  margin-bottom: 10px;
  padding: 8px 0;
  border-bottom: 1px solid #f1f3f4;
}

.info-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.info-item label {
  font-weight: 600;
  color: #495057;
  min-width: 80px;
  margin-right: 10px;
}

.info-item span {
  color: #6c757d;
  flex: 1;
}

.content-text {
  word-break: break-all;
}

/* 状态样式 */
.status-processing { color: #007bff; }
.status-success { color: #28a745; }
.status-failed { color: #dc3545; }
.status-waiting { color: #6c757d; }

.status-connected { color: #28a745; }
.status-disconnected { color: #dc3545; }
.status-active { color: #28a745; }
.status-inactive { color: #6c757d; }

/* 控制面板 */
.control-panel {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.panel-section {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e9ecef;
}

.panel-section:last-child {
  margin-bottom: 0;
  border-bottom: none;
}

.panel-section h4 {
  margin: 0 0 15px 0;
  color: #495057;
  font-size: 16px;
}

.control-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.control-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  color: white;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.control-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.control-btn.success { background: #28a745; }
.control-btn.danger { background: #dc3545; }
.control-btn.warning { background: #51d8af; }
.control-btn.info { background: #17a2b8; }
.control-btn:disabled {
  background: #6c757d;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.polling-controls {
  display: flex;
  align-items: center;
  gap: 15px;
}

.polling-info {
  color: #6c757d;
  font-size: 14px;
}

.status-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f1f3f4;
}

.status-item:last-child {
  border-bottom: none;
}

.status-item label {
  font-weight: 600;
  color: #495057;
}

/* 空白状态 */
.empty-state {
  text-align: center;
  padding: 40px 20px;
}

.empty-content {
  max-width: 300px;
  margin: 0 auto;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
  font-weight: 600;
  color: #6c757d;
  margin-bottom: 8px;
}

.empty-tip {
  color: #adb5bd;
  margin-bottom: 20px;
}

.request-status {
  padding: 12px;
  border-radius: 6px;
  margin-bottom: 20px;
  font-weight: 500;
}

.status-success {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.status-error {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.status-loading {
  background: #cce7ff;
  color: #004085;
  border: 1px solid #b3d7ff;
}

.status-default {
  background: #e2e3e5;
  color: #383d41;
  border: 1px solid #d6d8db;
}

/* 调试信息 */
.debug-info {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 15px;
  margin-top: 20px;
  border-left: 4px solid #6c757d;
}

.debug-info h4 {
  margin: 0 0 10px 0;
  color: #495057;
  font-size: 14px;
}

.debug-content p {
  margin: 5px 0;
  font-size: 12px;
  color: #6c757d;
  font-family: monospace;
}
</style>
