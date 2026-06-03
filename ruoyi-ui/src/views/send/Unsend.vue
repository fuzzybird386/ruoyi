<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 统计信息 -->
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #409EFF;">
              <i class="el-icon-upload2"></i>
            </div>
            <div class="stat-content">
              <div class="stat-title">队列一（待发送）</div>
              <div class="stat-value">{{ queue1Count }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #F56C6C;">
              <i class="el-icon-warning"></i>
            </div>
            <div class="stat-content">
              <div class="stat-title">队列二（发送失败）</div>
              <div class="stat-value">{{ queue2Count }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #67C23A;">
              <i class="el-icon-finished"></i>
            </div>
            <div class="stat-content">
              <div class="stat-title">TCP连接状态</div>
              <div class="stat-value">{{ tcpStatus ? '运行中' : '已停止' }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #E6A23C;">
              <i class="el-icon-timer"></i>
            </div>
            <div class="stat-content">
              <div class="stat-title">总数据量</div>
              <div class="stat-value">{{ totalDataCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 队列一数据 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <div slot="header" class="clearfix">
            <span>待发送数据 ({{ queue1Data.length }})</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="refreshQueue1">
              <i class="el-icon-refresh"></i> 刷新
            </el-button>
          </div>

          <div v-if="queue1Loading" style="text-align: center; padding: 20px;">
            <i class="el-icon-loading"></i> 加载中...
          </div>

          <el-table
            v-else
            :data="queue1Data"
            height="400"
            empty-text="暂无数据">
            <el-table-column prop="dataId" label="数据ID" min-width="200" show-overflow-tooltip>
              <template slot-scope="scope">
                <el-tooltip :content="scope.row.dataId" placement="top">
                  <span>{{ scope.row.dataId }}</span>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180">
              <template slot-scope="scope">
                {{ formatTime(scope.row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="retryCount" label="重试次数" width="100">
              <template slot-scope="scope">
                <el-tag :type="scope.row.retryCount > 0 ? 'warning' : 'info'">
                  {{ scope.row.retryCount }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template slot-scope="scope">
                <el-button size="mini" @click="viewData(scope.row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 队列二数据 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <div slot="header" class="clearfix">
            <span>发送失败数据 ({{ queue2Data.length }})</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="refreshQueue2">
              <i class="el-icon-refresh"></i> 刷新
            </el-button>
            <el-button style="float: right; padding: 3px 0; margin-right: 10px;" type="text" @click="debugData">
              <i class="el-icon-search"></i> 调试
            </el-button>
          </div>

          <div v-if="queue2Loading" style="text-align: center; padding: 20px;">
            <i class="el-icon-loading"></i> 加载中...
          </div>

          <el-table
            v-else
            :data="queue2Data"
            height="400"
            empty-text="暂无数据">
            <el-table-column prop="dataId" label="数据ID" min-width="200" show-overflow-tooltip>
              <template slot-scope="scope">
                <el-tooltip :content="scope.row.dataId" placement="top">
                  <span>{{ scope.row.dataId }}</span>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180">
              <template slot-scope="scope">
                {{ formatTime(scope.row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="retryCount" label="重试次数" width="100">
              <template slot-scope="scope">
                <el-tag :type="scope.row.retryCount > 0 ? 'warning' : 'info'">
                  {{ scope.row.retryCount }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template slot-scope="scope">
                <el-button size="mini" type="primary" @click="retryData(scope.row)">重试</el-button>
                <el-button size="mini" @click="viewData(scope.row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div style="margin-top: 20px;">
            <el-button type="primary" @click="showImportDialog = true">
              <i class="el-icon-upload"></i> 手动导入JSON数据
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据查看对话框 -->
    <el-dialog title="数据详情" :visible.sync="viewDialogVisible" width="70%">
      <el-card>
        <pre class="json-viewer">{{ formatJSON(viewingData) }}</pre>
      </el-card>
      <div slot="footer" class="dialog-footer">
        <el-button @click="viewDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="copyData">复制JSON</el-button>
      </div>
    </el-dialog>

    <!-- 手动导入对话框 -->
    <el-dialog title="手动导入JSON数据" :visible.sync="showImportDialog" width="60%">
      <el-form :model="importForm" label-width="100px">
        <el-form-item label="JSON数据">
          <el-input
            type="textarea"
            :rows="15"
            placeholder="请输入完整的JSON数据"
            v-model="importForm.jsonData"
            :autosize="{ minRows: 15, maxRows: 20 }"
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showImportDialog = false">取消</el-button>
        <el-button type="primary" @click="handleImport" :loading="importLoading">导入</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
// 导入 dayjs
import dayjs from 'dayjs'
import { getQueue1, getQueue2, importToQueue2, retryQueue2Data, getServerStatus, getDataList } from '@/api/send/send.js'

export default {
  name: 'DataManagement',
  data() {
    return {
      queue1Data: [],
      queue2Data: [],
      queue1Loading: false,
      queue2Loading: false,
      viewDialogVisible: false,
      showImportDialog: false,
      importLoading: false,
      viewingData: '',
      tcpStatus: false,
      totalDataCount: 0,
      importForm: {
        jsonData: ''
      },
      refreshTimer: null
    }
  },
  computed: {
    queue1Count() {
      return this.queue1Data.length
    },
    queue2Count() {
      return this.queue2Data.length
    }
  },
  mounted() {
    this.loadAllData()
    // 每30秒自动刷新数据
    this.refreshTimer = setInterval(() => {
      this.loadAllData()
    }, 30000)
  },
  beforeDestroy() {
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer)
    }
  },
  methods: {
    // 加载所有数据
    async loadAllData() {
      console.log('🔄 开始加载所有数据...')
      await Promise.all([
        this.loadQueue1(),
        this.loadQueue2(),
        this.loadServerStatus(),
        this.loadTotalDataCount()
      ])
      console.log('✅ 所有数据加载完成')
    },

    // 加载队列一数据
    async loadQueue1() {
      try {
        console.log('🔍 开始加载队列一数据...')
        this.queue1Loading = true
        const response = await getQueue1()
        console.log('📥 队列一API响应:', response)

        if (response.data && response.data.data) {
          this.queue1Data = response.data.data
          console.log('✅ 队列一数据加载成功，数量:', this.queue1Data.length)
        } else {
          this.queue1Data = []
          console.warn('⚠️ 队列一数据为空或格式不正确')
        }
      } catch (error) {
        console.error('❌ 加载队列一数据失败:', error)
        this.$message.error('加载队列一数据失败: ' + (error.message || '未知错误'))
        this.queue1Data = []
      } finally {
        this.queue1Loading = false
      }
    },

    // 加载队列二数据
    async loadQueue2() {
      try {
        console.log('🔍 开始加载队列二数据...')
        this.queue2Loading = true
        const response = await getQueue2()
        console.log('📥 队列二API响应:', response)

        if (response.data && response.data.data) {
          this.queue2Data = response.data.data
          console.log('✅ 队列二数据加载成功，数量:', this.queue2Data.length)
          console.log('📋 队列二数据内容:', this.queue2Data)
        } else {
          this.queue2Data = []
          console.warn('⚠️ 队列二数据为空或格式不正确')
        }
      } catch (error) {
        console.error('❌ 加载队列二数据失败:', error)
        this.$message.error('加载队列二数据失败: ' + (error.message || '未知错误'))
        this.queue2Data = []
      } finally {
        this.queue2Loading = false
      }
    },

    // 调试数据
    async debugData() {
      try {
        console.log('🐛 开始调试数据...')
        // 使用 request 而不是 this.$http
        const response = await getQueue2() // 先用现有的API测试
        console.log('🔍 调试信息 - 队列二数据:', response.data)

        if (response.data && response.data.data) {
          const queue2Data = response.data.data
          console.log('📊 队列二实际大小:', queue2Data.length)
          console.log('📋 队列二详情:', queue2Data)

          this.$message.success(`调试信息已输出到控制台。队列二大小: ${queue2Data.length}`)
        }
      } catch (error) {
        console.error('❌ 调试失败:', error)
        this.$message.error('调试失败: ' + (error.message || '未知错误'))
      }
    },

    // 处理导入
    async handleImport() {
      if (!this.importForm.jsonData.trim()) {
        this.$message.warning('请输入JSON数据')
        return
      }

      try {
        this.importLoading = true

        // 验证JSON格式
        try {
          JSON.parse(this.importForm.jsonData)
          console.log('✅ JSON格式验证通过')
        } catch (e) {
          this.$message.error('JSON格式错误，请检查数据格式')
          console.error('❌ JSON格式错误:', e)
          return
        }

        console.log('🚀 开始调用导入API...')
        const response = await importToQueue2({ jsonData: this.importForm.jsonData })
        console.log('📥 导入API响应:', response)

        if (response.code === 200) {
          this.$message.success('数据导入成功')
          this.showImportDialog = false
          this.importForm.jsonData = ''

          // 等待一下然后刷新数据
          setTimeout(async () => {
            console.log('🔄 导入成功后刷新队列二数据...')
            await this.loadQueue2()
          }, 500)

        } else {
          this.$message.error('导入失败: ' + (response.msg || '未知错误'))
        }
      } catch (error) {
        console.error('❌ 导入数据失败:', error)
        this.$message.error('导入数据失败: ' + (error.message || '未知错误'))
      } finally {
        this.importLoading = false
      }
    },

    // 刷新队列一
    async refreshQueue1() {
      console.log('🔄 手动刷新队列一...')
      await this.loadQueue1()
      this.$message.success('队列一已刷新')
    },

    // 刷新队列二
    async refreshQueue2() {
      console.log('🔄 手动刷新队列二...')
      await this.loadQueue2()
      this.$message.success('队列二已刷新')
    },

    // 加载服务器状态
    async loadServerStatus() {
      try {
        const response = await getServerStatus()
        this.tcpStatus = response.data
      } catch (error) {
        console.error('加载服务器状态失败:', error)
      }
    },

    // 加载总数据量
    async loadTotalDataCount() {
      try {
        const response = await getDataList()
        this.totalDataCount = response.data ? response.data.length : 0
      } catch (error) {
        console.error('加载总数据量失败:', error)
      }
    },

    // 查看数据
    viewData(data) {
      this.viewingData = data.jsonData
      this.viewDialogVisible = true
    },

    // 重试数据
    async retryData(data) {
      try {
        await retryQueue2Data(data.dataId)
        this.$message.success('数据已添加到队列一等待重试')
        this.loadAllData()
      } catch (error) {
        console.error('重试数据失败:', error)
        this.$message.error('重试数据失败')
      }
    },

    // 格式化JSON显示
    formatJSON(json) {
      if (!json) return ''
      try {
        const parsed = JSON.parse(json)
        return JSON.stringify(parsed, null, 2)
      } catch {
        return json
      }
    },

    // 格式化时间 - 使用直接导入的dayjs
    formatTime(time) {
      if (!time) return ''
      return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
    },

    // 复制数据
    copyData() {
      const textarea = document.createElement('textarea')
      textarea.value = this.viewingData
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
      this.$message.success('数据已复制到剪贴板')
    }
  }
}
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.stat-icon i {
  font-size: 24px;
  color: white;
}

.stat-content {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.json-viewer {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.5;
  max-height: 500px;
  overflow: auto;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>
