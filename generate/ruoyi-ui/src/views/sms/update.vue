<!--<template>-->
<!--  <div class="qr-code-container">-->
<!--    &lt;!&ndash; 主显示区域 &ndash;&gt;-->
<!--    <div class="display-area">-->
<!--      &lt;!&ndash; 当前二维码显示 &ndash;&gt;-->
<!--      <div class="current-qrcode-section">-->
<!--        <div class="section-header">-->
<!--          <h3>当前扫描二维码</h3>-->
<!--          <div class="refresh-info">-->
<!--            <span class="refresh-count">刷新次数: {{ currentRefreshCount }}/10</span>-->
<!--            <span class="refresh-timer">刷新: {{ refreshCountdown }}秒</span>-->
<!--          </div>-->
<!--        </div>-->

<!--        <div v-if="currentImageUrl" class="qrcode-display">-->
<!--          <div class="qrcode-wrapper">-->
<!--            <img-->
<!--              :src="currentImageUrl"-->
<!--              class="qrcode-image"-->
<!--              alt="二维码"-->
<!--              @load="onImageLoad"-->
<!--              @error="onImageError"-->
<!--            />-->
<!--            <div class="refresh-indicator" v-if="isRefreshing">-->
<!--              <div class="spinner"></div>-->
<!--              <span>刷新中...</span>-->
<!--            </div>-->
<!--          </div>-->

<!--          <div class="current-data-info">-->
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
<!--            <div class="info-item">-->
<!--              <label>生成时间:</label>-->
<!--              <span>{{ lastUpdateTime }}</span>-->
<!--            </div>-->
<!--            <div class="info-item status" :class="currentStatusClass">-->
<!--              <label>状态:</label>-->
<!--              <span>{{ currentStatusText }}</span>-->
<!--            </div>-->
<!--          </div>-->
<!--        </div>-->

<!--        &lt;!&ndash; 空白状态 &ndash;&gt;-->
<!--        <div v-else class="empty-state">-->
<!--          <div class="empty-content">-->
<!--            <div class="empty-icon">📭</div>-->
<!--            <div class="empty-text">暂无待扫描二维码</div>-->
<!--            <div class="empty-tip">等待队列一数据生成二维码</div>-->
<!--            <div class="request-status" :class="statusClass">-->
<!--              {{ lastRequestStatus }}-->
<!--            </div>-->
<!--          </div>-->
<!--        </div>-->
<!--      </div>-->

<!--      &lt;!&ndash; 队列状态显示 &ndash;&gt;-->
<!--      <div class="queues-section">-->
<!--        <div class="queues-container">-->
<!--          &lt;!&ndash; 队列一：待处理数据 &ndash;&gt;-->
<!--          <div class="queue-card queue1">-->
<!--            <div class="queue-header">-->
<!--              <h4>队列一 - 待处理数据</h4>-->
<!--              <span class="queue-count">{{ queue1Data.length }}</span>-->
<!--            </div>-->
<!--            <div class="queue-list">-->
<!--              <div v-for="(item, index) in queue1Data" :key="item._dataId"-->
<!--                   class="queue-item" :class="{ 'processing': index === 0 }">-->
<!--                <div class="item-index">#{{ index + 1 }}</div>-->
<!--                <div class="item-content">-->
<!--                  <div class="item-id">{{ item._dataId }}</div>-->
<!--                  <div class="item-text">{{ item.content }}</div>-->
<!--                  <div class="item-phone">{{ item.receivePhone }}</div>-->
<!--                </div>-->
<!--                <div class="item-status" v-if="index === 0">即将处理</div>-->
<!--              </div>-->
<!--              <div v-if="queue1Data.length === 0" class="empty-queue">-->
<!--                暂无待处理数据-->
<!--              </div>-->
<!--            </div>-->
<!--          </div>-->

<!--          &lt;!&ndash; 队列二：未成功数据 &ndash;&gt;-->
<!--          <div class="queue-card queue2">-->
<!--            <div class="queue-header">-->
<!--              <h4>队列二 - 未成功数据</h4>-->
<!--              <span class="queue-count">{{ queue2Data.length }}</span>-->
<!--              <div class="queue-actions">-->
<!--                <button @click="exportFailedData" class="export-btn"-->
<!--                        :disabled="queue2Data.length === 0">-->
<!--                  导出全部-->
<!--                </button>-->
<!--              </div>-->
<!--            </div>-->
<!--            <div class="queue-list">-->
<!--              <div v-for="(item, index) in queue2Data" :key="item._dataId"-->
<!--                   class="queue-item failed">-->
<!--                <div class="item-index">#{{ index + 1 }}</div>-->
<!--                <div class="item-content">-->
<!--                  <div class="item-id">{{ item._dataId }}</div>-->
<!--                  <div class="item-text">{{ item.content }}</div>-->
<!--                  <div class="item-phone">{{ item.receivePhone }}</div>-->
<!--                  <div class="fail-reason">{{ item._failReason }}</div>-->
<!--                </div>-->
<!--                <div class="item-status failed">失败</div>-->
<!--              </div>-->
<!--              <div v-if="queue2Data.length === 0" class="empty-queue">-->
<!--                暂无未成功数据-->
<!--              </div>-->
<!--            </div>-->
<!--          </div>-->
<!--        </div>-->
<!--      </div>-->
<!--    </div>-->

<!--    &lt;!&ndash; 控制面板 &ndash;&gt;-->
<!--    <div class="control-panel">-->
<!--      <div class="panel-section">-->
<!--        <h4>系统控制</h4>-->
<!--        <div class="control-buttons">-->
<!--          <button @click="startQueueProcessing" class="control-btn success"-->
<!--                  :disabled="queueProcessingActive">-->
<!--            启动队列处理-->
<!--          </button>-->
<!--          <button @click="stopQueueProcessing" class="control-btn danger"-->
<!--                  :disabled="!queueProcessingActive">-->
<!--            停止队列处理-->
<!--          </button>-->
<!--          <button @click="clearAllQueues" class="control-btn warning">-->
<!--            清空所有队列-->
<!--          </button>-->
<!--          <button @click="refreshQueueStatus" class="control-btn info">-->
<!--            刷新状态-->
<!--          </button>-->
<!--        </div>-->
<!--      </div>-->

<!--      <div class="panel-section">-->
<!--        <h4>轮询控制</h4>-->
<!--        <div class="polling-controls">-->
<!--          <button @click="togglePolling" class="control-btn"-->
<!--                  :class="{ 'active': isPollingActive }">-->
<!--            {{ isPollingActive ? '停止轮询' : '开始轮询' }}-->
<!--          </button>-->
<!--          <span class="polling-info">-->
<!--            状态: {{ pollingStatus }} ({{ pollingCount }}次)-->
<!--          </span>-->
<!--        </div>-->
<!--      </div>-->

<!--      <div class="panel-section">-->
<!--        <h4>系统状态</h4>-->
<!--        <div class="status-info">-->
<!--          <div class="status-item">-->
<!--            <label>WebSocket:</label>-->
<!--            <span :class="wsStatusClass">{{ wsStatus }}</span>-->
<!--          </div>-->
<!--          <div class="status-item">-->
<!--            <label>队列处理:</label>-->
<!--            <span :class="queueStatusClass">{{ queueProcessingStatus }}</span>-->
<!--          </div>-->
<!--          <div class="status-item">-->
<!--            <label>总调用次数:</label>-->
<!--            <span>{{ requestCount }}</span>-->
<!--          </div>-->
<!--          <div class="status-item">-->
<!--            <label>最后检查:</label>-->
<!--            <span>{{ lastCheckTime || '从未检查' }}</span>-->
<!--          </div>-->
<!--        </div>-->
<!--      </div>-->
<!--    </div>-->

<!--    &lt;!&ndash; 调试信息（可选） &ndash;&gt;-->
<!--    <div v-if="showDebug" class="debug-info">-->
<!--      <h4>调试信息</h4>-->
<!--      <div class="debug-content">-->
<!--        <p>当前处理ID: {{ lastProcessedId || '无' }}</p>-->
<!--        <p>等待信号: {{ waitingForReceiveSignal ? '是' : '否' }}</p>-->
<!--        <p>待处理队列: {{ pendingQrQueue.length }} 条</p>-->
<!--        <p>最后请求状态: {{ lastRequestStatus }}</p>-->
<!--      </div>-->
<!--    </div>-->
<!--  </div>-->
<!--</template>-->

<!--<script>-->
<!--import {-->
<!--  autoGenerateQrCode,-->
<!--  getAllQueueStatus,-->
<!--  exportAndClearFailedData,-->
<!--  exportFailedData,-->
<!--  startQueue,-->
<!--  stopQueue,-->
<!--  clearAllQueues-->
<!--} from "@/api/sms/qrcode";-->
<!--import websocketClient from "@/utils/websocket";-->
<!--import item from "@/layout/components/Sidebar/Item.vue";-->

<!--export default {-->
<!--  name: "QrCodeDisplay",-->
<!--  data() {-->
<!--    return {-->
<!--      // 当前二维码数据-->
<!--      currentImageUrl: "",-->
<!--      currentDataContent: "",-->
<!--      currentData: null,-->
<!--      currentDataId: "",-->
<!--      currentReceivePhone: "",-->
<!--      currentRefreshCount: 0,-->
<!--      currentStatus: "waiting", // waiting, processing, success, failed-->

<!--      // 队列数据-->
<!--      queue1Data: [],-->
<!--      queue2Data: [],-->

<!--      // 系统状态-->
<!--      lastUpdateTime: null,-->
<!--      lastRequestStatus: "系统初始化中...",-->
<!--      requestCount: 0,-->
<!--      wsStatus: "未连接",-->
<!--      isWsConnected: false,-->

<!--      // 轮询控制-->
<!--      isPollingActive: true,-->
<!--      pollingInterval: 3000, // 3秒-->
<!--      pollingTimer: null,-->
<!--      pollingCount: 0,-->
<!--      pollingStatus: "未开始",-->
<!--      lastCheckTime: null,-->
<!--      refreshCountdown: 3,-->
<!--      countdownTimer: null,-->

<!--      // 处理状态-->
<!--      isRefreshing: false,-->
<!--      lastProcessedId: null,-->
<!--      waitingForReceiveSignal: false,-->
<!--      pendingQrQueue: [],-->

<!--      // 队列处理状态-->
<!--      queueProcessingActive: false,-->
<!--      queueProcessingStatus: "未启动",-->

<!--      // 调试-->
<!--      showDebug: false,-->

<!--      autoRefreshTimer: null,-->
<!--      refreshInterval: 3000, // 3秒刷新一次-->
<!--    };-->
<!--  },-->

<!--  computed: {-->
<!--    statusClass() {-->
<!--      if (this.lastRequestStatus.includes('成功')) return 'status-success';-->
<!--      if (this.lastRequestStatus.includes('失败')) return 'status-error';-->
<!--      if (this.lastRequestStatus.includes('正在')) return 'status-loading';-->
<!--      return 'status-default';-->
<!--    },-->

<!--    currentStatusClass() {-->
<!--      switch (this.currentStatus) {-->
<!--        case 'processing': return 'status-processing';-->
<!--        case 'success': return 'status-success';-->
<!--        case 'failed': return 'status-failed';-->
<!--        default: return 'status-waiting';-->
<!--      }-->
<!--    },-->

<!--    currentStatusText() {-->
<!--      switch (this.currentStatus) {-->
<!--        case 'processing': return `处理中 (${this.currentRefreshCount}/10)`;-->
<!--        case 'success': return '成功';-->
<!--        case 'failed': return '失败';-->
<!--        default: return '等待中';-->
<!--      }-->
<!--    },-->

<!--    wsStatusClass() {-->
<!--      return this.isWsConnected ? 'status-connected' : 'status-disconnected';-->
<!--    },-->

<!--    queueStatusClass() {-->
<!--      return this.queueProcessingActive ? 'status-active' : 'status-inactive';-->
<!--    },-->

<!--    autoRefreshStatus() {-->
<!--      return this.autoRefreshTimer ? '运行中' : '已停止';-->
<!--    },-->
<!--    autoRefreshStatusClass() {-->
<!--      return this.autoRefreshTimer ? 'status-active' : 'status-inactive';-->
<!--    }-->
<!--  },-->

<!--  mounted() {-->
<!--    console.log("=== 启动二维码显示系统 ===");-->
<!--    this.initWebSocket();-->
<!--    this.startPolling();-->
<!--    this.refreshQueueStatus();-->

<!--    this.startAutoRefresh(); // 启动自动刷新-->
<!--  },-->

<!--  beforeUnmount() {-->
<!--    this.disconnectWebSocket();-->
<!--    this.stopPolling();-->
<!--    this.revokeImageUrl();-->
<!--    this.stopAutoRefresh(); // 停止自动刷新-->
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

<!--        this.lastRequestStatus = "WebSocket已连接";-->
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

<!--        // 处理清空指令-->
<!--        if (message.clear || message.type === 'CLEAR_ALL') {-->
<!--          this.clearDisplay();-->
<!--          this.waitingForReceiveSignal = false;-->
<!--          this.lastProcessedId = null;-->
<!--          console.log("🗑️ 收到清空指令，重置所有状态");-->
<!--          return;-->
<!--        }-->

<!--        // 处理接收确认信号-->
<!--        if (message.receive_signal === true || message.type === 'DATA_SUCCESS') {-->
<!--          console.log("✅ 收到成功信号，完成当前数据处理");-->
<!--          this.currentStatus = 'success';-->
<!--          this.waitingForReceiveSignal = false;-->
<!--          this.lastProcessedId = null;-->

<!--          // 短暂延迟后处理下一条-->
<!--          setTimeout(() => {-->
<!--            this.processNextQrInQueue();-->
<!--          }, 500);-->
<!--          return;-->
<!--        }-->

<!--        // 处理数据失败-->
<!--        if (message.type === 'DATA_FAILED') {-->
<!--          console.log("❌ 数据标记为失败:", message.dataId);-->
<!--          this.currentStatus = 'failed';-->
<!--          // 继续处理下一条-->
<!--          setTimeout(() => {-->
<!--            this.processNextQrInQueue();-->
<!--          }, 1000);-->
<!--          return;-->
<!--        }-->

<!--        // 处理队列状态更新-->
<!--        if (message.type === 'QUEUE_STATUS_UPDATE') {-->
<!--          this.refreshQueueStatus();-->
<!--          return;-->
<!--        }-->

<!--        // 处理二维码更新-->
<!--        if (message.type === 'QR_CODE_UPDATE' && message.data) {-->
<!--          console.log("📨 收到新二维码数据:", message.data);-->
<!--          this.handleNewQrCodeData(message.data);-->
<!--        }-->
<!--      } catch (error) {-->
<!--        console.error("处理WebSocket消息失败:", error);-->
<!--      }-->
<!--    },-->
<!--    /** 启动自动刷新 */-->
<!--    startAutoRefresh() {-->
<!--      this.stopAutoRefresh();-->
<!--      console.log("🔄 启动自动刷新，间隔:", this.refreshInterval + "ms");-->

<!--      this.autoRefreshTimer = setInterval(() => {-->
<!--        this.autoRefreshQrCode();-->
<!--      }, this.refreshInterval);-->
<!--    },-->

<!--    /** 停止自动刷新 */-->
<!--    stopAutoRefresh() {-->
<!--      if (this.autoRefreshTimer) {-->
<!--        clearInterval(this.autoRefreshTimer);-->
<!--        this.autoRefreshTimer = null;-->
<!--        console.log("⏹️ 停止自动刷新");-->
<!--      }-->
<!--    },-->

<!--    /** 自动刷新二维码 */-->
<!--    async autoRefreshQrCode() {-->
<!--      // 如果没有当前数据，不进行刷新-->
<!--      if (!this.currentData) {-->
<!--        return;-->
<!--      }-->

<!--      // 如果正在刷新中，跳过-->
<!--      if (this.isRefreshing) {-->
<!--        return;-->
<!--      }-->

<!--      try {-->
<!--        this.isRefreshing = true;-->
<!--        this.currentRefreshCount++;-->

<!--        console.log(`🔄 自动刷新二维码 (${this.currentRefreshCount}/10)`, this.currentData);-->

<!--        // 重新生成二维码-->
<!--        await this.generateAndDisplayQrCode(this.currentData);-->

<!--        // 如果达到10次刷新，标记为失败-->
<!--        if (this.currentRefreshCount >= 10) {-->
<!--          console.log("❌ 刷新达到10次，标记为失败");-->
<!--          this.currentStatus = 'failed';-->
<!--          this.stopAutoRefresh();-->
<!--          // 延迟后处理下一条-->
<!--          setTimeout(() => {-->
<!--            this.processNextQrInQueue();-->
<!--          }, 1000);-->
<!--        }-->

<!--      } catch (error) {-->
<!--        console.error("❌ 自动刷新失败:", error);-->
<!--      } finally {-->
<!--        this.isRefreshing = false;-->
<!--      }-->
<!--    },-->

<!--    /** 处理新二维码数据 */-->
<!--    handleNewQrCodeData(data) {-->
<!--      console.log("📨 收到新二维码数据:", data);-->

<!--      // const exists = this.pendingQrQueue.some(item =>-->
<!--      //   item._dataId === data._dataId-->
<!--      // );-->
<!--      // if(!exists){-->
<!--      //   this.pendingQrQueue.push(data);-->
<!--      //   console.log(`数据加入队列，ID：${data._dataId}`)-->
<!--      // }-->

<!--      // 设置当前数据-->
<!--      this.currentData = data;-->
<!--      this.currentDataId = this.generateDataId(data);-->
<!--      this.currentReceivePhone = data.receivePhone;-->
<!--      this.currentDataContent = data.content;-->
<!--      this.currentStatus = 'processing';-->

<!--      // 正确获取内容-->
<!--      if (data.content) {-->
<!--        // 如果数据中有 content 字段，直接使用-->
<!--        this.currentDataContent = data.content;-->
<!--      } else {-->
<!--        // 否则显示整个数据对象-->
<!--        this.currentDataContent = JSON.stringify(data, null, 2);-->
<!--      }-->
<!--      this.currentStatus = 'processing';-->

<!--      // 立即生成二维码-->
<!--      this.generateAndDisplayQrCode(data);-->

<!--      // 启动自动刷新-->
<!--      this.startAutoRefresh();-->
<!--        // 如果没有在等待信号，立即处理-->
<!--        if (!this.waitingForReceiveSignal) {-->
<!--          this.processNextQrInQueue();-->
<!--        }-->
<!--    },-->

<!--    /** 生成数据ID */-->
<!--    generateDataId(data) {-->
<!--      return 'DATA_' + JSON.stringify(data).hashCode() + '_' + Date.now();-->
<!--    },-->

<!--    /** 处理WebSocket消息 */-->
<!--    // async handleQrCodeMessage(message) {-->
<!--    //   try {-->
<!--    //     console.log('📨 接收到WebSocket消息:', message);-->
<!--    //-->
<!--    //     // 处理二维码更新-->
<!--    //     if (message.type === 'QR_CODE_UPDATE' && message.data) {-->
<!--    //       this.handleNewQrCodeData(message.data);-->
<!--    //       return;-->
<!--    //     }-->
<!--    //-->
<!--    //     // 处理数据成功-->
<!--    //     if (message.type === 'DATA_SUCCESS') {-->
<!--    //       console.log("✅ 收到成功信号，完成当前数据处理");-->
<!--    //       this.currentStatus = 'success';-->
<!--    //       this.stopAutoRefresh();-->
<!--    //-->
<!--    //       // 短暂延迟后清空显示-->
<!--    //       setTimeout(() => {-->
<!--    //         this.clearCurrentDisplay();-->
<!--    //       }, 1000);-->
<!--    //       return;-->
<!--    //     }-->
<!--    //-->
<!--    //     // 处理数据失败-->
<!--    //     if (message.type === 'DATA_FAILED') {-->
<!--    //       console.log("❌ 数据标记为失败:", message.dataId);-->
<!--    //       this.currentStatus = 'failed';-->
<!--    //       this.stopAutoRefresh();-->
<!--    //-->
<!--    //       // 延迟后清空显示-->
<!--    //       setTimeout(() => {-->
<!--    //         this.clearCurrentDisplay();-->
<!--    //       }, 2000);-->
<!--    //       return;-->
<!--    //     }-->
<!--    //-->
<!--    //     // 处理队列状态更新-->
<!--    //     if (message.type === 'QUEUE_STATUS_UPDATE') {-->
<!--    //       this.refreshQueueStatus();-->
<!--    //       return;-->
<!--    //     }-->
<!--    //-->
<!--    //   } catch (error) {-->
<!--    //     console.error("处理WebSocket消息失败:", error);-->
<!--    //   }-->
<!--    // },-->



<!--    // /** 处理新二维码数据 */-->
<!--    // handleNewQrCodeData(data) {-->
<!--    //   // 检查是否已经在队列中-->
<!--    //   const exists = this.pendingQrQueue.some(item =>-->
<!--    //     item._dataId === data._dataId-->
<!--    //   );-->
<!--    //-->
<!--    //   if (!exists) {-->
<!--    //     this.pendingQrQueue.push(data);-->
<!--    //     console.log(`📥 新数据加入队列，ID: ${data._dataId}`);-->
<!--    //   }-->
<!--    //-->
<!--    //   // 如果没有在等待信号，立即处理-->
<!--    //   if (!this.waitingForReceiveSignal) {-->
<!--    //     this.processNextQrInQueue();-->
<!--    //   }-->
<!--    // },-->

<!--    /** 处理队列中的下一条二维码 */-->
<!--    async processNextQrInQueue() {-->
<!--      if (this.waitingForReceiveSignal || this.isRefreshing) {-->
<!--        console.log("⏳ 等待信号中或正在刷新，暂不处理新二维码");-->
<!--        return;-->
<!--      }-->

<!--      if (this.pendingQrQueue.length === 0) {-->
<!--        console.log("📭 队列为空，无待处理二维码");-->
<!--        this.clearCurrentDisplay();-->
<!--        return;-->
<!--      }-->

<!--      const nextData = this.pendingQrQueue[0];-->
<!--      const dataId = nextData._dataId;-->

<!--      // 检查是否已经处理过这个ID-->
<!--      if (this.lastProcessedId === dataId) {-->
<!--        console.log("🔄 跳过已处理的二维码:", dataId);-->
<!--        this.pendingQrQueue.shift();-->
<!--        setTimeout(() => this.processNextQrInQueue(), 50);-->
<!--        return;-->
<!--      }-->

<!--      console.log("➡️ 处理队列中下一条二维码:", nextData);-->
<!--      this.pendingQrQueue.shift();-->

<!--      await this.processNewQrCode(nextData);-->
<!--    },-->

<!--    /** 处理新二维码 */-->
<!--    async processNewQrCode(data) {-->
<!--      try {-->
<!--        const dataId = data._dataId;-->

<!--        this.lastProcessedId = dataId;-->
<!--        this.requestCount++;-->
<!--        this.currentStatus = 'processing';-->
<!--        this.currentRefreshCount = data._refreshCount || 1;-->
<!--        this.currentDataId = dataId;-->
<!--        this.currentReceivePhone = data.receivePhone;-->
<!--        this.currentDataContent = data.content;-->

<!--        // 修改这里：正确设置内容-->
<!--        if (data.content) {-->
<!--          this.currentDataContent = data.content;-->
<!--        } else {-->
<!--          this.currentDataContent = JSON.stringify(data, null, 2);-->
<!--        }-->

<!--        console.log(`📡 生成二维码中，刷新次数: ${this.currentRefreshCount}`, data);-->
<!--        this.lastRequestStatus = "正在生成二维码...";-->
<!--        this.isRefreshing = true;-->

<!--        await this.generateAndDisplayQrCode(data);-->

<!--        // 设置等待信号状态-->
<!--        this.waitingForReceiveSignal = true;-->
<!--        console.log(`🕓 等待外部信号确认 (${this.currentRefreshCount}/10)`);-->

<!--      } catch (e) {-->
<!--        console.error("❌ 处理二维码失败:", e);-->
<!--        this.isRefreshing = false;-->
<!--        this.waitingForReceiveSignal = false;-->
<!--        setTimeout(() => this.processNextQrInQueue(), 100);-->
<!--      }-->
<!--    },-->

<!--    /** 生成并显示二维码 */-->
<!--    async generateAndDisplayQrCode(data) {-->
<!--      try {-->
<!--        this.lastRequestStatus = "调用API生成二维码中...";-->

<!--        const response = await autoGenerateQrCode(data);-->
<!--        if (!response || response.size === 0) {-->
<!--          throw new Error("API返回空响应");-->
<!--        }-->

<!--        const blob = new Blob([response], { type: 'image/png' });-->
<!--        const imageUrl = URL.createObjectURL(blob);-->

<!--        this.revokeImageUrl();-->
<!--        this.currentImageUrl = imageUrl;-->
<!--        this.lastUpdateTime = new Date().toLocaleTimeString();-->
<!--        this.lastRequestStatus = `✅ 二维码显示成功!`;-->

<!--        this.$message.success('二维码已更新');-->

<!--      } catch (error) {-->
<!--        console.error("❌ 生成二维码失败:", error);-->
<!--        this.lastRequestStatus = `❌ 生成失败: ${error.message}`;-->
<!--        this.$message.error(`二维码生成失败: ${error.message}`);-->
<!--        throw error;-->
<!--      }-->
<!--    },-->

<!--    /** 刷新队列状态 */-->
<!--    async refreshQueueStatus() {-->
<!--      try {-->
<!--        const response = await getAllQueueStatus();-->
<!--        if (response.data) {-->
<!--          this.queue1Data = response.data.queue1Data || [];-->
<!--          this.queue2Data = response.data.queue2Data || [];-->
<!--          this.queueProcessingActive = response.data.currentProcessing || false;-->
<!--          this.queueProcessingStatus = this.queueProcessingActive ? '运行中' : '已停止';-->

<!--          // 更新当前处理数据的刷新次数-->
<!--          if (response.data.currentDataDetail) {-->
<!--            this.currentRefreshCount = response.data.currentDataDetail._refreshCount || 0;-->
<!--          }-->
<!--        }-->
<!--      } catch (error) {-->
<!--        console.error("刷新队列状态失败:", error);-->
<!--      }-->
<!--    },-->

<!--    /** 导出失败数据 */-->
<!--    // async exportFailedData() {-->
<!--    //   try {-->
<!--    //     await exportAndClearFailedData();-->
<!--    //     this.$message.success('失败数据导出成功');-->
<!--    //     this.refreshQueueStatus();-->
<!--    //   } catch (error) {-->
<!--    //     this.$message.error('导出失败: ' + error.message);-->
<!--    //   }-->
<!--    // },-->
<!--    /** 导出失败数据为JSON文件 - 完整错误处理版本 */-->
<!--    // async exportFailedData() {-->
<!--    //   if (this.queue2Data.length === 0) {-->
<!--    //     this.$message.warning('队列二中暂无失败数据');-->
<!--    //     return;-->
<!--    //   }-->
<!--    //-->
<!--    //   // 添加加载状态-->
<!--    //   const loading = this.$loading({-->
<!--    //     lock: true,-->
<!--    //     text: `正在导出 ${this.queue2Data.length} 条数据...`,-->
<!--    //     spinner: 'el-icon-loading',-->
<!--    //     background: 'rgba(0, 0, 0, 0.7)'-->
<!--    //   });-->
<!--    //-->
<!--    //   try {-->
<!--    //     // 调用API-->
<!--    //     const blobResponse = await exportFailedData();-->
<!--    //-->
<!--    //     // 检查是否是有效的Blob-->
<!--    //     if (!(blobResponse instanceof Blob)) {-->
<!--    //       throw new Error('服务器返回的数据格式不正确');-->
<!--    //     }-->
<!--    //-->
<!--    //     // 检查Blob大小-->
<!--    //     if (blobResponse.size === 0) {-->
<!--    //       throw new Error('导出的文件为空');-->
<!--    //     }-->
<!--    //-->
<!--    //     // 创建下载-->
<!--    //     const blob = new Blob([blobResponse], {-->
<!--    //       type: 'application/json'-->
<!--    //     });-->
<!--    //-->
<!--    //     const downloadUrl = URL.createObjectURL(blob);-->
<!--    //     const link = document.createElement('a');-->
<!--    //     link.href = downloadUrl;-->
<!--    //-->
<!--    //     // 使用时间戳生成文件名-->
<!--    //     const now = new Date();-->
<!--    //     const fileName = `失败数据导出_${now.getFullYear()}${(now.getMonth()+1).toString().padStart(2, '0')}${now.getDate().toString().padStart(2, '0')}_${now.getHours().toString().padStart(2, '0')}${now.getMinutes().toString().padStart(2, '0')}${now.getSeconds().toString().padStart(2, '0')}.json`;-->
<!--    //-->
<!--    //     link.download = fileName;-->
<!--    //     document.body.appendChild(link);-->
<!--    //     link.click();-->
<!--    //     document.body.removeChild(link);-->
<!--    //-->
<!--    //     // 清理URL-->
<!--    //     setTimeout(() => {-->
<!--    //       URL.revokeObjectURL(downloadUrl);-->
<!--    //     }, 100);-->
<!--    //-->
<!--    //     this.$message.success({-->
<!--    //       message: `JSON文件导出成功！\n文件名: ${fileName}\n数据量: ${this.queue2Data.length}条`,-->
<!--    //       duration: 5000-->
<!--    //     });-->
<!--    //-->
<!--    //     console.log('📁 导出完成:', fileName);-->
<!--    //-->
<!--    //   } catch (error) {-->
<!--    //     console.error('导出失败:', error);-->
<!--    //-->
<!--    //     let errorMessage = '导出失败';-->
<!--    //-->
<!--    //     // 处理不同类型的错误-->
<!--    //     if (error instanceof Blob) {-->
<!--    //       // 如果是Blob类型的错误响应-->
<!--    //       try {-->
<!--    //         const errorText = await new Promise((resolve, reject) => {-->
<!--    //           const reader = new FileReader();-->
<!--    //           reader.onload = () => resolve(reader.result);-->
<!--    //           reader.onerror = reject;-->
<!--    //           reader.readAsText(error);-->
<!--    //         });-->
<!--    //         const errorData = JSON.parse(errorText);-->
<!--    //         errorMessage = errorData.message || errorMessage;-->
<!--    //       } catch {-->
<!--    //         errorMessage = '服务器返回了错误信息';-->
<!--    //       }-->
<!--    //     } else if (error.response) {-->
<!--    //       errorMessage = error.response.data?.message || errorMessage;-->
<!--    //     } else if (error.message) {-->
<!--    //       errorMessage = error.message;-->
<!--    //     }-->
<!--    //-->
<!--    //     this.$message.error(errorMessage);-->
<!--    //-->
<!--    //   } finally {-->
<!--    //     loading.close();-->
<!--    //   }-->
<!--    // },-->

<!--    /** 导出失败数据为纯JSON文件 */-->
<!--    async exportFailedData() {-->
<!--      if (this.queue2Data.length === 0) {-->
<!--        this.$message.warning('队列二中暂无失败数据可导出');-->
<!--        return;-->
<!--      }-->

<!--      try {-->
<!--        this.$message.info(`正在导出 ${this.queue2Data.length} 条失败数据...`);-->

<!--        // 调用API获取blob数据-->
<!--        const blobData = await exportFailedData();-->

<!--        // 创建下载链接-->
<!--        const blob = new Blob([blobData], {-->
<!--          type: 'application/json; charset=utf-8'-->
<!--        });-->

<!--        const downloadUrl = window.URL.createObjectURL(blob);-->
<!--        const link = document.createElement('a');-->
<!--        link.href = downloadUrl;-->

<!--        // 生成文件名-->
<!--        const timestamp = new Date().getTime();-->
<!--        const fileName = `failed_data_${timestamp}.json`;-->

<!--        link.download = fileName;-->
<!--        document.body.appendChild(link);-->
<!--        link.click();-->
<!--        document.body.removeChild(link);-->

<!--        // 释放URL对象-->
<!--        window.URL.revokeObjectURL(downloadUrl);-->

<!--        this.$message.success(`JSON文件导出成功，共${this.queue2Data.length}条数据`);-->

<!--      } catch (error) {-->
<!--        console.error('导出失败:', error);-->
<!--        this.$message.error('导出失败: ' + (error.message || '未知错误'));-->
<!--      }-->
<!--    },-->
<!--    /** 读取Blob为文本 */-->
<!--    readBlobAsText(blob) {-->
<!--      return new Promise((resolve, reject) => {-->
<!--        const reader = new FileReader();-->
<!--        reader.onload = () => resolve(reader.result);-->
<!--        reader.onerror = reject;-->
<!--        reader.readAsText(blob);-->
<!--      });-->
<!--    },-->

<!--    /** 启动队列处理 */-->
<!--    async startQueueProcessing() {-->
<!--      try {-->
<!--        await startQueue();-->
<!--        this.queueProcessingActive = true;-->
<!--        this.queueProcessingStatus = '运行中';-->
<!--        this.$message.success('队列处理已启动');-->
<!--      } catch (error) {-->
<!--        this.$message.error('启动失败: ' + error.message);-->
<!--      }-->
<!--    },-->

<!--    /** 停止队列处理 */-->
<!--    async stopQueueProcessing() {-->
<!--      try {-->
<!--        await stopQueue();-->
<!--        this.queueProcessingActive = false;-->
<!--        this.queueProcessingStatus = '已停止';-->
<!--        this.$message.success('队列处理已停止');-->
<!--      } catch (error) {-->
<!--        this.$message.error('停止失败: ' + error.message);-->
<!--      }-->
<!--    },-->

<!--    /** 清空所有队列 */-->
<!--    async clearAllQueues() {-->
<!--      try {-->
<!--        await clearAllQueues();-->
<!--        this.clearDisplay();-->
<!--        this.pendingQrQueue = [];-->
<!--        this.refreshQueueStatus();-->
<!--        this.$message.success('所有队列已清空');-->
<!--      } catch (error) {-->
<!--        this.$message.error('清空失败: ' + error.message);-->
<!--      }-->
<!--    },-->

<!--    /** 清空当前显示 */-->
<!--    clearCurrentDisplay() {-->
<!--      this.currentImageUrl = "";-->
<!--      this.currentDataContent = "";-->
<!--      this.currentData = null;-->
<!--      this.currentDataId = "";-->
<!--      this.currentReceivePhone = "";-->
<!--      this.currentStatus = "waiting";-->
<!--      this.currentRefreshCount = 0;-->
<!--    },-->

<!--    /** 清空显示 */-->
<!--    clearDisplay() {-->
<!--      this.revokeImageUrl();-->
<!--      this.clearCurrentDisplay();-->
<!--      this.lastProcessedId = null;-->
<!--      this.waitingForReceiveSignal = false;-->
<!--      this.isRefreshing = false;-->
<!--    },-->

<!--    /** 释放图片URL */-->
<!--    revokeImageUrl() {-->
<!--      if (this.currentImageUrl && this.currentImageUrl.startsWith('blob:')) {-->
<!--        URL.revokeObjectURL(this.currentImageUrl);-->
<!--      }-->
<!--    },-->

<!--    /** 图片加载事件 */-->
<!--    onImageLoad() {-->
<!--      console.log("✅ 二维码图片加载完成");-->
<!--      this.isRefreshing = false;-->
<!--    },-->

<!--    onImageError(event) {-->
<!--      console.error("❌ 图片加载失败!", event);-->
<!--      this.isRefreshing = false;-->
<!--      this.waitingForReceiveSignal = false;-->
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
<!--      this.stopPolling();-->
<!--      this.isPollingActive = true;-->
<!--      this.pollingStatus = "轮询中...";-->
<!--      this.checkForUpdates();-->
<!--      this.pollingTimer = setInterval(() => this.checkForUpdates(), this.pollingInterval);-->
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
<!--      this.refreshCountdown = this.pollingInterval / 1000;-->
<!--      if (this.countdownTimer) clearInterval(this.countdownTimer);-->
<!--      this.countdownTimer = setInterval(() => {-->
<!--        this.refreshCountdown&#45;&#45;;-->
<!--        if (this.refreshCountdown <= 0) this.refreshCountdown = this.pollingInterval / 1000;-->
<!--      }, 1000);-->
<!--    },-->

<!--    async checkForUpdates() {-->
<!--      if (!this.isWsConnected) {-->
<!--        try {-->
<!--          this.pollingCount++;-->
<!--          this.lastCheckTime = new Date().toLocaleTimeString();-->
<!--          await this.refreshQueueStatus();-->
<!--        } catch (error) {-->
<!--          console.error("轮询检查失败:", error);-->
<!--        }-->
<!--      }-->
<!--    }-->
<!--  }-->
<!--};-->
<!--</script>-->

<!--<style scoped>-->
<!--.qr-code-container {-->
<!--  max-width: 1200px;-->
<!--  margin: 0 auto;-->
<!--  padding: 20px;-->
<!--  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;-->
<!--}-->

<!--.display-area {-->
<!--  display: grid;-->
<!--  grid-template-columns: 1fr 1fr;-->
<!--  gap: 20px;-->
<!--  margin-bottom: 20px;-->
<!--}-->

<!--.current-qrcode-section {-->
<!--  background: #f8f9fa;-->
<!--  border-radius: 12px;-->
<!--  padding: 20px;-->
<!--  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);-->
<!--}-->

<!--.section-header {-->
<!--  display: flex;-->
<!--  justify-content: space-between;-->
<!--  align-items: center;-->
<!--  margin-bottom: 20px;-->
<!--  padding-bottom: 10px;-->
<!--  border-bottom: 2px solid #e9ecef;-->
<!--}-->

<!--.section-header h3 {-->
<!--  margin: 0;-->
<!--  color: #495057;-->
<!--  font-size: 18px;-->
<!--}-->

<!--.refresh-info {-->
<!--  display: flex;-->
<!--  gap: 15px;-->
<!--  font-size: 14px;-->
<!--}-->

<!--.refresh-count {-->
<!--  color: #007bff;-->
<!--  font-weight: 500;-->
<!--}-->

<!--.refresh-timer {-->
<!--  color: #28a745;-->
<!--  font-weight: 500;-->
<!--}-->

<!--.qrcode-display {-->
<!--  text-align: center;-->
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

<!--.current-data-info {-->
<!--  background: white;-->
<!--  padding: 16px;-->
<!--  border-radius: 8px;-->
<!--  text-align: left;-->
<!--  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);-->
<!--}-->

<!--.info-item {-->
<!--  display: flex;-->
<!--  margin-bottom: 10px;-->
<!--  padding: 8px 0;-->
<!--  border-bottom: 1px solid #f1f3f4;-->
<!--}-->

<!--.info-item:last-child {-->
<!--  border-bottom: none;-->
<!--  margin-bottom: 0;-->
<!--}-->

<!--.info-item label {-->
<!--  font-weight: 600;-->
<!--  color: #495057;-->
<!--  min-width: 80px;-->
<!--  margin-right: 10px;-->
<!--}-->

<!--.info-item span {-->
<!--  color: #6c757d;-->
<!--  flex: 1;-->
<!--}-->

<!--.content-text {-->
<!--  word-break: break-all;-->
<!--}-->

<!--/* 状态样式 */-->
<!--.status-processing { color: #007bff; }-->
<!--.status-success { color: #28a745; }-->
<!--.status-failed { color: #dc3545; }-->
<!--.status-waiting { color: #6c757d; }-->

<!--.status-connected { color: #28a745; }-->
<!--.status-disconnected { color: #dc3545; }-->
<!--.status-active { color: #28a745; }-->
<!--.status-inactive { color: #6c757d; }-->

<!--/* 队列样式 */-->
<!--.queues-section {-->
<!--  background: #f8f9fa;-->
<!--  border-radius: 12px;-->
<!--  padding: 20px;-->
<!--  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);-->
<!--}-->

<!--.queues-container {-->
<!--  display: grid;-->
<!--  grid-template-columns: 1fr 1fr;-->
<!--  gap: 20px;-->
<!--  height: 400px;-->
<!--}-->

<!--.queue-card {-->
<!--  background: white;-->
<!--  border-radius: 8px;-->
<!--  padding: 15px;-->
<!--  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);-->
<!--  display: flex;-->
<!--  flex-direction: column;-->
<!--}-->

<!--.queue1 {-->
<!--  border-left: 4px solid #007bff;-->
<!--}-->

<!--.queue2 {-->
<!--  border-left: 4px solid #dc3545;-->
<!--}-->

<!--.queue-header {-->
<!--  display: flex;-->
<!--  justify-content: space-between;-->
<!--  align-items: center;-->
<!--  margin-bottom: 15px;-->
<!--  padding-bottom: 10px;-->
<!--  border-bottom: 1px solid #e9ecef;-->
<!--}-->

<!--.queue-header h4 {-->
<!--  margin: 0;-->
<!--  font-size: 16px;-->
<!--}-->

<!--.queue-count {-->
<!--  background: #6c757d;-->
<!--  color: white;-->
<!--  padding: 2px 8px;-->
<!--  border-radius: 12px;-->
<!--  font-size: 12px;-->
<!--  font-weight: 600;-->
<!--}-->

<!--.queue1 .queue-count {-->
<!--  background: #007bff;-->
<!--}-->

<!--.queue2 .queue-count {-->
<!--  background: #dc3545;-->
<!--}-->

<!--.queue-actions {-->
<!--  display: flex;-->
<!--  gap: 8px;-->
<!--}-->

<!--.export-btn {-->
<!--  padding: 4px 8px;-->
<!--  background: #28a745;-->
<!--  color: white;-->
<!--  border: none;-->
<!--  border-radius: 4px;-->
<!--  font-size: 12px;-->
<!--  cursor: pointer;-->
<!--}-->

<!--.export-btn:disabled {-->
<!--  background: #6c757d;-->
<!--  cursor: not-allowed;-->
<!--}-->

<!--.queue-list {-->
<!--  flex: 1;-->
<!--  overflow-y: auto;-->
<!--}-->

<!--.queue-item {-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  padding: 10px;-->
<!--  margin-bottom: 8px;-->
<!--  border-radius: 6px;-->
<!--  border: 1px solid #e9ecef;-->
<!--  background: #f8f9fa;-->
<!--}-->

<!--.queue-item.processing {-->
<!--  background: #e3f2fd;-->
<!--  border-color: #007bff;-->
<!--}-->

<!--.queue-item.failed {-->
<!--  background: #f8d7da;-->
<!--  border-color: #dc3545;-->
<!--}-->

<!--.item-index {-->
<!--  background: #6c757d;-->
<!--  color: white;-->
<!--  width: 24px;-->
<!--  height: 24px;-->
<!--  border-radius: 50%;-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  justify-content: center;-->
<!--  font-size: 12px;-->
<!--  font-weight: 600;-->
<!--  margin-right: 10px;-->
<!--}-->

<!--.processing .item-index {-->
<!--  background: #007bff;-->
<!--}-->

<!--.failed .item-index {-->
<!--  background: #dc3545;-->
<!--}-->

<!--.item-content {-->
<!--  flex: 1;-->
<!--}-->

<!--.item-id {-->
<!--  font-size: 12px;-->
<!--  color: #6c757d;-->
<!--  margin-bottom: 2px;-->
<!--}-->

<!--.item-text {-->
<!--  font-size: 14px;-->
<!--  color: #495057;-->
<!--  margin-bottom: 2px;-->
<!--  word-break: break-all;-->
<!--}-->

<!--.item-phone {-->
<!--  font-size: 12px;-->
<!--  color: #007bff;-->
<!--}-->

<!--.fail-reason {-->
<!--  font-size: 11px;-->
<!--  color: #dc3545;-->
<!--  margin-top: 2px;-->
<!--}-->

<!--.item-status {-->
<!--  font-size: 12px;-->
<!--  font-weight: 600;-->
<!--  padding: 2px 6px;-->
<!--  border-radius: 4px;-->
<!--  background: #6c757d;-->
<!--  color: white;-->
<!--}-->

<!--.item-status.failed {-->
<!--  background: #dc3545;-->
<!--}-->

<!--.empty-queue {-->
<!--  text-align: center;-->
<!--  color: #6c757d;-->
<!--  font-style: italic;-->
<!--  padding: 20px;-->
<!--}-->

<!--/* 控制面板 */-->
<!--.control-panel {-->
<!--  background: #f8f9fa;-->
<!--  border-radius: 12px;-->
<!--  padding: 20px;-->
<!--  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);-->
<!--}-->

<!--.panel-section {-->
<!--  margin-bottom: 20px;-->
<!--  padding-bottom: 15px;-->
<!--  border-bottom: 1px solid #e9ecef;-->
<!--}-->

<!--.panel-section:last-child {-->
<!--  margin-bottom: 0;-->
<!--  border-bottom: none;-->
<!--}-->

<!--.panel-section h4 {-->
<!--  margin: 0 0 15px 0;-->
<!--  color: #495057;-->
<!--  font-size: 16px;-->
<!--}-->

<!--.control-buttons {-->
<!--  display: flex;-->
<!--  gap: 10px;-->
<!--  flex-wrap: wrap;-->
<!--}-->

<!--.control-btn {-->
<!--  padding: 8px 16px;-->
<!--  border: none;-->
<!--  border-radius: 6px;-->
<!--  color: white;-->
<!--  font-weight: 500;-->
<!--  cursor: pointer;-->
<!--  transition: all 0.2s;-->
<!--}-->

<!--.control-btn:hover {-->
<!--  transform: translateY(-1px);-->
<!--  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);-->
<!--}-->

<!--.control-btn.success { background: #28a745; }-->
<!--.control-btn.danger { background: #dc3545; }-->
<!--.control-btn.warning { background: #fd7e14; }-->
<!--.control-btn.info { background: #17a2b8; }-->
<!--.control-btn:disabled {-->
<!--  background: #6c757d;-->
<!--  cursor: not-allowed;-->
<!--  transform: none;-->
<!--  box-shadow: none;-->
<!--}-->

<!--.polling-controls {-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  gap: 15px;-->
<!--}-->

<!--.polling-info {-->
<!--  color: #6c757d;-->
<!--  font-size: 14px;-->
<!--}-->

<!--.status-info {-->
<!--  display: grid;-->
<!--  grid-template-columns: 1fr 1fr;-->
<!--  gap: 10px;-->
<!--}-->

<!--.status-item {-->
<!--  display: flex;-->
<!--  justify-content: space-between;-->
<!--  padding: 8px 0;-->
<!--  border-bottom: 1px solid #f1f3f4;-->
<!--}-->

<!--.status-item:last-child {-->
<!--  border-bottom: none;-->
<!--}-->

<!--.status-item label {-->
<!--  font-weight: 600;-->
<!--  color: #495057;-->
<!--}-->

<!--/* 空白状态 */-->
<!--.empty-state {-->
<!--  text-align: center;-->
<!--  padding: 40px 20px;-->
<!--}-->

<!--.empty-content {-->
<!--  max-width: 300px;-->
<!--  margin: 0 auto;-->
<!--}-->

<!--.empty-icon {-->
<!--  font-size: 48px;-->
<!--  margin-bottom: 16px;-->
<!--}-->

<!--.empty-text {-->
<!--  font-size: 16px;-->
<!--  font-weight: 600;-->
<!--  color: #6c757d;-->
<!--  margin-bottom: 8px;-->
<!--}-->

<!--.empty-tip {-->
<!--  color: #adb5bd;-->
<!--  margin-bottom: 20px;-->
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

<!--/* 调试信息 */-->
<!--.debug-info {-->
<!--  background: #f8f9fa;-->
<!--  border-radius: 8px;-->
<!--  padding: 15px;-->
<!--  margin-top: 20px;-->
<!--  border-left: 4px solid #6c757d;-->
<!--}-->

<!--.debug-info h4 {-->
<!--  margin: 0 0 10px 0;-->
<!--  color: #495057;-->
<!--  font-size: 14px;-->
<!--}-->

<!--.debug-content p {-->
<!--  margin: 5px 0;-->
<!--  font-size: 12px;-->
<!--  color: #6c757d;-->
<!--  font-family: monospace;-->
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

        <div v-if="currentImageUrl" class="qrcode-display">
          <div class="qrcode-wrapper">
            <img
              :src="currentImageUrl"
              class="qrcode-image"
              alt="二维码"
              @load="onImageLoad"
              @error="onImageError"
            />
            <div class="refresh-indicator" v-if="isRefreshing">
              <div class="spinner"></div>
              <span>刷新中...</span>
            </div>
          </div>

          <div class="current-data-info">
            <div class="info-item">
              <label>数据ID:</label>
              <span>{{ currentDataId || '暂无' }}</span>
            </div>
            <div class="info-item">
              <label>内容:</label>
              <span class="content-text">{{ currentDataContent }}</span>
            </div>
            <div class="info-item">
              <label>接收手机:</label>
              <span>{{ currentReceivePhone || '暂无' }}</span>
            </div>
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
            <div class="queue-list scrollable">
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
            <div class="queue-list scrollable">
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

      <!-- 新增：已完成数据清理控制 -->
      <div class="panel-section">
        <h4>已完成数据清理</h4>
        <div class="cleanup-controls">
          <div class="control-buttons">
            <button @click="showCleanupPreview" class="control-btn info">
              查看清理预览
            </button>
            <button @click="showCompletedStats" class="control-btn info">
              查看统计信息
            </button>
            <button @click="showCleanupSchedule" class="control-btn info">
              查看清理计划
            </button>
            <button @click="manualCleanup" class="control-btn warning"
                    :disabled="cleanupInProgress">
              {{ cleanupInProgress ? '清理中...' : '手动清理已完成数据' }}
            </button>
          </div>

          <!-- 清理状态信息 -->
          <div v-if="cleanupStatus" class="cleanup-status" :class="cleanupStatusClass">
            {{ cleanupStatus }}
          </div>

          <!-- 清理统计信息 -->
          <div v-if="cleanupStats" class="cleanup-stats">
            <div class="stat-item">
              <span class="stat-label">已完成数据总量:</span>
              <span class="stat-value">{{ cleanupStats.totalCompleted || 0 }} 条</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">7天内数据:</span>
              <span class="stat-value">{{ cleanupStats.lastWeekCount || 0 }} 条</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">下次清理时间:</span>
              <span class="stat-value">{{ nextCleanupTime }}</span>
            </div>
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

    <!-- 清理确认对话框 -->
    <el-dialog
      v-if="showCleanupDialog"
      :visible.sync="showCleanupDialog"
      title="清理已完成数据"
      width="500px"
      @close="handleCleanupDialogClose"
    >
      <div class="cleanup-dialog-content">
        <div class="dialog-section">
          <h4>清理预览</h4>
          <div v-if="cleanupPreview" class="preview-info">
            <div class="info-item">
              <span class="label">将清理的数据量:</span>
              <span class="value highlight">{{ cleanupPreview.count || 0 }} 条</span>
            </div>
            <div class="info-item">
              <span class="label">保留天数:</span>
              <span class="value">{{ cleanupPreview.retentionDays || 7 }} 天</span>
            </div>
            <div class="info-item">
              <span class="label">清理截止日期:</span>
              <span class="value">{{ formatDate(cleanupPreview.cutoffDate) }}</span>
            </div>
            <div class="info-item">
              <span class="label">当前已完成数据总量:</span>
              <span class="value">{{ cleanupPreview.totalCompleted || 0 }} 条</span>
            </div>
          </div>

          <!-- 预览数据列表 -->
          <div v-if="cleanupPreview && cleanupPreview.previewData && cleanupPreview.previewData.length > 0"
               class="preview-list">
            <h5>即将清理的数据（前{{ Math.min(cleanupPreview.previewData.length, 5) }}条）:</h5>
            <div class="preview-scroll">
              <div v-for="(item, index) in cleanupPreview.previewData.slice(0, 5)"
                   :key="item.id || index" class="preview-item">
                <div class="preview-header">
                  <span class="preview-index">#{{ index + 1 }}</span>
                  <span class="preview-id">ID: {{ item.data_id }}</span>
                  <span class="preview-time">{{ formatDate(item.created_time) }}</span>
                </div>
                <div class="preview-content">
                  <pre>{{ formatPreviewContent(item) }}</pre>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="dialog-section">
          <h4>清理设置</h4>
          <div class="settings-form">
            <div class="form-item">
              <label>保留天数（清理{{ retentionDays }}天前的数据）:</label>
              <div class="retention-controls">
                <button @click="decreaseRetentionDays" class="small-btn"
                        :disabled="retentionDays <= 0">-</button>
                <input v-model="retentionDays" type="number" min="0" max="365"
                       class="retention-input" @change="updateRetentionDays">
                <button @click="increaseRetentionDays" class="small-btn"
                        :disabled="retentionDays >= 365">+</button>
                <span class="hint">天</span>
              </div>
            </div>
            <div class="form-tips">
              <p>💡 提示：</p>
              <ul>
                <li>设置为0将清理所有已完成数据</li>
                <li>建议保留7-30天以便追溯</li>
                <li>清理后数据无法恢复，请谨慎操作</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="showCleanupDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmCleanup"
                   :loading="cleanupInProgress" :disabled="!cleanupPreview || cleanupPreview.count === 0">
          确认清理 {{ cleanupPreview ? cleanupPreview.count : 0 }} 条数据
        </el-button>
      </span>
    </el-dialog>

    <!-- 统计信息对话框 -->
    <el-dialog
      v-if="showStatsDialog"
      :visible.sync="showStatsDialog"
      title="已完成数据统计"
      width="700px"
    >
      <div v-if="completedStats" class="stats-dialog-content">
        <div class="stats-summary">
          <div class="summary-card">
            <div class="summary-icon">📊</div>
            <div class="summary-content">
              <div class="summary-label">已完成数据总量</div>
              <div class="summary-value">{{ completedStats.totalCompleted }}</div>
            </div>
          </div>
          <div class="summary-card">
            <div class="summary-icon">🗑️</div>
            <div class="summary-content">
              <div class="summary-label">7天前数据量</div>
              <div class="summary-value">{{ completedStats.lastWeekCount }}</div>
            </div>
          </div>
          <div class="summary-card">
            <div class="summary-icon">⏰</div>
            <div class="summary-content">
              <div class="summary-label">下次自动清理</div>
              <div class="summary-value">{{ nextCleanupTime }}</div>
            </div>
          </div>
        </div>

        <div class="stats-details">
          <h4>最近完成的数据（最多显示20条）</h4>
          <div v-if="completedStats.recentData && completedStats.recentData.length > 0"
               class="recent-data-list">
            <div v-for="(item, index) in completedStats.recentData.slice(0, 20)"
                 :key="item.id || index" class="recent-item">
              <div class="recent-header">
                <span class="recent-index">#{{ index + 1 }}</span>
                <span class="recent-id">{{ item.data_id }}</span>
                <span class="recent-time">{{ formatDate(item.created_time) }}</span>
              </div>
              <div class="recent-content">
                <pre>{{ formatDataContent(item) }}</pre>
              </div>
            </div>
          </div>
          <div v-else class="no-data">
            <div class="no-data-icon">📭</div>
            <div class="no-data-text">暂无已完成数据</div>
          </div>
        </div>
      </div>
      <div v-else class="loading-stats">
        <div class="spinner"></div>
        <div>正在加载统计信息...</div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  autoGenerateQrCode,
  getAllQueueStatus,
  exportAndClearFailedData,
  exportFailedData,
  startQueue,
  stopQueue,
  clearAllQueues,

  cleanupCompletedData,
  getCompletedStats,
  getCleanupPreview,
  getCleanupSchedule
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


      // 新增：清理相关数据
      cleanupInProgress: false,
      cleanupStatus: "",
      cleanupStats: null,
      cleanupPreview: null,
      completedStats: null,
      nextCleanupTime: "计算中...",

      // 对话框控制
      showCleanupDialog: false,
      showStatsDialog: false,
      showScheduleDialog: false,

      // 清理设置
      retentionDays: 7,
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
    },
    //定时清理
    cleanupStatusClass() {
      if (this.cleanupStatus.includes('成功') || this.cleanupStatus.includes('完成')) {
        return 'status-success';
      }
      if (this.cleanupStatus.includes('失败') || this.cleanupStatus.includes('错误')) {
        return 'status-error';
      }
      if (this.cleanupStatus.includes('正在') || this.cleanupStatus.includes('中...')) {
        return 'status-loading';
      }
      return 'status-default';
    }
  },

  mounted() {
    console.log("=== 启动二维码显示系统 ===");
    this.initWebSocket();
    this.startPolling();
    this.refreshQueueStatus();

    this.startAutoRefresh(); // 启动自动刷新

    // 加载清理统计信息
    this.loadCleanupStats();
  },

  beforeUnmount() {
    this.disconnectWebSocket();
    this.stopPolling();
    this.revokeImageUrl();
    this.stopAutoRefresh(); // 停止自动刷新
  },

  methods: {
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

      // 设置当前数据
      this.currentData = data;
      this.currentDataId = this.generateDataId(data);
      this.currentReceivePhone = data.receivePhone;
      this.currentDataContent = data.content;
      this.currentStatus = 'processing';

      // 正确获取内容
      if (data.content) {
        // 如果数据中有 content 字段，直接使用
        this.currentDataContent = data.content;
      } else {
        // 否则显示整个数据对象
        this.currentDataContent = JSON.stringify(data, null, 2);
      }
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

        // 修改这里：正确设置内容
        if (data.content) {
          this.currentDataContent = data.content;
        } else {
          this.currentDataContent = JSON.stringify(data, null, 2);
        }

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
        this.lastRequestStatus = "调用API生成二维码中...";

        const response = await autoGenerateQrCode(data);
        if (!response || response.size === 0) {
          throw new Error("API返回空响应");
        }

        const blob = new Blob([response], { type: 'image/png' });
        const imageUrl = URL.createObjectURL(blob);

        this.revokeImageUrl();
        this.currentImageUrl = imageUrl;
        this.lastUpdateTime = new Date().toLocaleTimeString();
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
    },

    // ============ 清理相关方法 ============

    /** 加载清理统计信息 */
    async loadCleanupStats() {
      try {
        const response = await getCleanupSchedule();
        if (response.data) {
          this.nextCleanupTime = response.data.nextExecutionTimeStr || '每周一凌晨3点';
        }

        // 加载统计数据
        const statsResponse = await getCompletedStats();
        if (statsResponse.data) {
          this.cleanupStats = {
            totalCompleted: statsResponse.data.totalCompleted || 0,
            lastWeekCount: statsResponse.data.lastWeekCount || 0,
            nextCleanupTime: this.nextCleanupTime
          };
        }
      } catch (error) {
        console.error("加载清理统计失败:", error);
      }
    },

    /** 显示清理预览对话框 */
    async showCleanupPreview() {
      try {
        this.cleanupStatus = "正在加载清理预览...";

        const response = await getCleanupPreview(this.retentionDays);
        if (response.data) {
          this.cleanupPreview = response.data;
          this.showCleanupDialog = true;
          this.cleanupStatus = `加载完成，可清理 ${response.data.count} 条数据`;
        } else {
          this.$message.warning('未获取到清理预览数据');
        }
      } catch (error) {
        console.error("加载清理预览失败:", error);
        this.cleanupStatus = `加载失败: ${error.message}`;
        this.$message.error('加载清理预览失败');
      }
    },

    /** 显示统计信息对话框 */
    async showCompletedStats() {
      try {
        this.cleanupStatus = "正在加载统计信息...";
        const response = await getCompletedStats();

        if (response.data) {
          this.completedStats = response.data;
          this.showStatsDialog = true;
          this.cleanupStatus = "统计信息加载完成";
        } else {
          this.$message.warning('未获取到统计信息');
        }
      } catch (error) {
        console.error("加载统计信息失败:", error);
        this.cleanupStatus = `加载失败: ${error.message}`;
        this.$message.error('加载统计信息失败');
      }
    },

    /** 显示清理计划对话框 */
    async showCleanupSchedule() {
      try {
        const response = await getCleanupSchedule();
        if (response.data) {
          this.$alert(
            `<div class="schedule-info">
              <p><strong>📅 清理计划</strong></p>
              <p>执行时间: ${response.data.description}</p>
              <p>下次执行: ${response.data.nextExecutionTimeStr}</p>
              <p>保留天数: ${response.data.retentionDays} 天</p>
              <p>清理状态: ${response.data.autoCleanupEnabled ? '✅ 已启用' : '❌ 已禁用'}</p>
              <p class="tip">💡 系统会在指定时间自动清理已完成数据，您也可以手动清理。</p>
            </div>`,
            '数据清理计划',
            {
              dangerouslyUseHTMLString: true,
              confirmButtonText: '确定'
            }
          );
        }
      } catch (error) {
        console.error("加载清理计划失败:", error);
        this.$message.error('加载清理计划失败');
      }
    },

    /** 手动清理已完成后端数据 */
    async manualCleanup() {
      this.showCleanupDialog = true;
    },

    /** 确认清理 */
    async confirmCleanup() {
      if (!this.cleanupPreview || this.cleanupPreview.count === 0) {
        this.$message.warning('没有需要清理的数据');
        return;
      }

      try {
        this.cleanupInProgress = true;
        this.cleanupStatus = `正在清理 ${this.cleanupPreview.count} 条数据...`;

        const response = await cleanupCompletedData({
          retentionDays: this.retentionDays
        });

        if (response.code === 200) {
          const data = response.data || {};
          this.cleanupStatus = `✅ 清理完成！删除了 ${data.deletedCount || 0} 条数据，剩余 ${data.remainingCount || 0} 条`;

          this.$message.success(`成功清理 ${data.deletedCount || 0} 条已完成数据`);

          // 关闭对话框
          this.showCleanupDialog = false;

          // 重新加载统计信息
          this.loadCleanupStats();

          // 清空预览数据
          this.cleanupPreview = null;
        } else {
          throw new Error(response.msg || '清理失败');
        }
      } catch (error) {
        console.error("清理失败:", error);
        this.cleanupStatus = `❌ 清理失败: ${error.message}`;
        this.$message.error(`清理失败: ${error.message}`);
      } finally {
        this.cleanupInProgress = false;
      }
    },

    /** 更新保留天数 */
    updateRetentionDays() {
      if (this.retentionDays < 0) this.retentionDays = 0;
      if (this.retentionDays > 365) this.retentionDays = 365;
      this.showCleanupPreview();
    },

    /** 增加保留天数 */
    increaseRetentionDays() {
      if (this.retentionDays < 365) {
        this.retentionDays++;
        this.showCleanupPreview();
      }
    },

    /** 减少保留天数 */
    decreaseRetentionDays() {
      if (this.retentionDays > 0) {
        this.retentionDays--;
        this.showCleanupPreview();
      }
    },

    /** 处理清理对话框关闭 */
    handleCleanupDialogClose() {
      this.cleanupPreview = null;
      this.cleanupStatus = "";
    },

    /** 格式化日期 */
    formatDate(dateStr) {
      if (!dateStr) return '未知时间';
      try {
        const date = new Date(dateStr);
        return date.toLocaleString();
      } catch (e) {
        return dateStr;
      }
    },

    /** 格式化预览内容 */
    formatPreviewContent(item) {
      try {
        if (item.content_json) {
          const content = JSON.parse(item.content_json);
          return JSON.stringify(content, null, 2);
        }
        return JSON.stringify(item, null, 2);
      } catch (e) {
        return JSON.stringify(item, null, 2);
      }
    },

    /** 格式化数据内容 */
    formatDataContent(item) {
      try {
        if (item.content_json) {
          const content = JSON.parse(item.content_json);
          // 简化显示，只显示关键信息
          const simplified = {
            content: content.content ? content.content.substring(0, 50) + '...' : '无内容',
            receivePhone: content.receivePhone || '未知',
            createTime: content.createTime || item.created_time
          };
          return JSON.stringify(simplified, null, 2);
        }
        return JSON.stringify(item, null, 2);
      } catch (e) {
        return JSON.stringify(item, null, 2);
      }
    },
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
.control-btn.warning { background: #fd7e14; }
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


/* ============ 清理控制样式 ============ */
.cleanup-controls {
  margin-top: 10px;
}

.cleanup-controls .control-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  margin-bottom: 15px;
}

.cleanup-controls .control-btn {
  padding: 8px 12px;
  font-size: 13px;
}

.cleanup-status {
  padding: 10px;
  border-radius: 6px;
  margin: 10px 0;
  font-size: 13px;
  font-weight: 500;
  text-align: center;
  transition: all 0.3s;
}

.cleanup-stats {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 12px;
  margin-top: 10px;
  border: 1px solid #e9ecef;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px dashed #dee2e6;
}

.stat-item:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.stat-label {
  font-weight: 600;
  color: #495057;
  font-size: 13px;
}

.stat-value {
  color: #007bff;
  font-weight: 600;
  font-size: 13px;
}

/* 对话框样式 */
.cleanup-dialog-content {
  max-height: 60vh;
  overflow-y: auto;
}

.dialog-section {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.dialog-section:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.dialog-section h4 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 16px;
}

.preview-info .info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  padding: 6px 0;
}

.preview-info .label {
  font-weight: 500;
  color: #606266;
}

.preview-info .value {
  color: #409eff;
  font-weight: 600;
}

.preview-info .value.highlight {
  color: #f56c6c;
  font-size: 16px;
}

.preview-list {
  margin-top: 15px;
}

.preview-list h5 {
  margin: 0 0 10px 0;
  color: #909399;
  font-size: 14px;
}

.preview-scroll {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 10px;
  background: #fafafa;
}

.preview-item {
  margin-bottom: 10px;
  padding: 10px;
  background: white;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.preview-item:last-child {
  margin-bottom: 0;
}

.preview-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.preview-index {
  background: #409eff;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  margin-right: 10px;
}

.preview-id {
  font-size: 12px;
  color: #909399;
  flex: 1;
}

.preview-time {
  font-size: 12px;
  color: #67c23a;
}

.preview-content pre {
  margin: 0;
  font-size: 12px;
  color: #606266;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 80px;
  overflow-y: auto;
}

/* 设置表单样式 */
.settings-form {
  margin-top: 15px;
}

.form-item {
  margin-bottom: 15px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  color: #606266;
  font-weight: 500;
}

.retention-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.small-btn {
  width: 32px;
  height: 32px;
  border: 1px solid #dcdfe6;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: #606266;
}

.small-btn:hover {
  border-color: #409eff;
  color: #409eff;
}

.small-btn:disabled {
  border-color: #ebeef5;
  color: #c0c4cc;
  cursor: not-allowed;
}

.retention-input {
  width: 80px;
  height: 32px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 0 8px;
  text-align: center;
  font-size: 14px;
}

.retention-input:focus {
  border-color: #409eff;
  outline: none;
}

.hint {
  color: #909399;
  font-size: 13px;
  margin-left: 5px;
}

.form-tips {
  background: #f0f9ff;
  border-left: 4px solid #409eff;
  padding: 10px 15px;
  border-radius: 4px;
  margin-top: 20px;
}

.form-tips p {
  margin: 0 0 8px 0;
  color: #409eff;
  font-weight: 500;
}

.form-tips ul {
  margin: 0;
  padding-left: 20px;
  color: #606266;
}

.form-tips li {
  margin-bottom: 4px;
  font-size: 13px;
}

/* 统计对话框样式 */
.stats-dialog-content {
  padding: 10px 0;
}

.stats-summary {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
  margin-bottom: 20px;
}

.summary-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 8px;
  padding: 15px;
  display: flex;
  align-items: center;
  gap: 15px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.summary-card:nth-child(2) {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.summary-card:nth-child(3) {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.summary-icon {
  font-size: 32px;
}

.summary-content {
  flex: 1;
}

.summary-label {
  font-size: 12px;
  opacity: 0.9;
  margin-bottom: 5px;
}

.summary-value {
  font-size: 24px;
  font-weight: bold;
}

.stats-details {
  margin-top: 20px;
}

.recent-data-list {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 15px;
  background: #fafafa;
}

.recent-item {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 10px;
}

.recent-item:last-child {
  margin-bottom: 0;
}

.recent-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f5f7fa;
}

.recent-index {
  background: #67c23a;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  margin-right: 10px;
}

.recent-id {
  font-size: 13px;
  color: #303133;
  flex: 1;
  font-family: monospace;
}

.recent-time {
  font-size: 12px;
  color: #909399;
}

.recent-content pre {
  margin: 0;
  font-size: 12px;
  color: #606266;
  background: #f6f8fa;
  padding: 8px;
  border-radius: 4px;
  white-space: pre-wrap;
  word-break: break-all;
}

.no-data {
  text-align: center;
  padding: 40px 20px;
}

.no-data-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.no-data-text {
  color: #909399;
  font-size: 14px;
}

.loading-stats {
  text-align: center;
  padding: 40px 20px;
}

.loading-stats .spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #409eff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
