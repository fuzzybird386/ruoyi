<template>
  <div class="statistics-panel">
    <!-- 系统标题 -->
    <div class="system-title" style="color: #1c84c6">
      <h1 >单向信息传输系统</h1>
    </div>

    <!-- 消息接收及转换情况 -->
    <div class="charts-section">
      <div class="section-header" style="color: #00afff">
        <div class="section-title">二维码识别及推送情况</div>
        <div class="section-divider"></div>
      </div>

<!--      <el-row :gutter="20">-->
<!--        &lt;!&ndash; 消息接收报表 &ndash;&gt;-->
<!--        <el-col :span="12">-->
<!--          <el-card class="chart-card" shadow="never">-->
<!--            <div class="chart-title-section">-->
<!--              <div class="chart-main-title">二维码识别报表</div>-->
<!--              <div class="chart-subtitle">按月统计接收的二维码信息数量</div>-->
<!--            </div>-->
<!--            <div class="chart-container">-->
<!--              <div id="receiveChart" class="echarts-chart"></div>-->
<!--            </div>-->
<!--          </el-card>-->
<!--        </el-col>-->

<!--        &lt;!&ndash; 转码及发送报表 &ndash;&gt;-->
<!--        <el-col :span="12">-->
<!--          <el-card class="chart-card" shadow="never">-->
<!--            <div class="chart-title-section">-->
<!--              <div class="chart-main-title">消息推送报表</div>-->
<!--              <div class="chart-subtitle">按月统计消息转发数量</div>-->
<!--            </div>-->
<!--            <div class="chart-container">-->
<!--              <div id="sendChart" class="echarts-chart"></div>-->
<!--            </div>-->
<!--          </el-card>-->
<!--        </el-col>-->
<!--      </el-row>-->
    </div>

    <!-- 加载状态 -->
    <el-dialog
      :visible.sync="loading"
      :show-close="false"
      :close-on-click-modal="false"
      width="200px"
    >
      <div style="text-align: center;">
        <el-button type="text" loading>加载中...</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
// import * as echarts from 'echarts'
// import {
//   getRealTimeStatistics,
//   getMonthlyStatistics
// } from '@/api/sms/statistics'

export default {
  name: 'StatisticsPanel',
  data() {
    return {
      loading: false,
      // 图表实例
      receiveChart: null,
      sendChart: null,
      // 消息接收数据
      receiveData: [],
      // 转码发送数据
      sendData: [],
      // 月份数据
      monthLabels: []
    }
  },
  mounted() {
    this.loadStatistics()
  },
  beforeDestroy() {
    // 销毁图表实例
    if (this.receiveChart) {
      this.receiveChart.dispose()
    }
    if (this.sendChart) {
      this.sendChart.dispose()
    }
  },
  methods: {
    // 加载统计数据
    // async loadStatistics() {
    //   this.loading = true
    //   try {
    //     // 获取月度统计数据
    //     const monthlyResponse = await getMonthlyStatistics()
    //     if (monthlyResponse.code === 200) {
    //       this.processMonthlyData(monthlyResponse.data)
    //     } else {
    //       // 如果月度统计API失败，尝试获取实时统计
    //       await this.loadRealTimeStatistics()
    //     }
    //   } catch (error) {
    //     console.error('加载统计失败:', error)
    //     this.$message.error('加载统计数据失败')
    //     // 使用默认数据作为后备
    //     this.useDefaultData()
    //   } finally {
    //     this.loading = false
    //   }
    // },

    // // 加载实时统计数据（备用方案）
    // async loadRealTimeStatistics() {
    //   try {
    //     const realTimeResponse = await getRealTimeStatistics()
    //     if (realTimeResponse.code === 200) {
    //       this.processRealTimeData(realTimeResponse.data)
    //     }
    //   } catch (error) {
    //     console.error('加载实时统计失败:', error)
    //     this.useDefaultData()
    //   }
    // },

    // 处理月度统计数据
    processMonthlyData(data) {
      if (data && data.length > 0) {
        // 按月份倒序排列，取最近4个月的数据
        const sortedData = data.sort((a, b) => {
          const dateA = new Date(a.yearMonth || a.month)
          const dateB = new Date(b.yearMonth || b.month)
          return dateB - dateA
        })

        const recentMonths = sortedData.slice(0, 4)

        // 处理月份标签
        this.monthLabels = recentMonths.map(item =>
          this.formatMonth(item.yearMonth || item.month)
        )

        // 处理接收数据
        this.receiveData = recentMonths.map(item =>
          item.receivedCount || 0
        )

        // 处理发送数据
        this.sendData = recentMonths.map(item =>
          item.qrGeneratedCount || 0
        )

        // 初始化图表
        this.$nextTick(() => {
          this.initCharts()
        })
      } else {
        this.useDefaultData()
      }
    },

    // 处理实时统计数据
    processRealTimeData(data) {
      // 如果实时统计数据包含月度信息，使用实时数据
      if (data.monthlyData) {
        this.processMonthlyData(data.monthlyData)
      } else {
        // 否则使用默认数据
        this.useDefaultData()
      }
    },

    // 使用默认数据
    useDefaultData() {
      // 生成最近4个月的数据
      const months = this.generateRecentMonths(4)

      this.monthLabels = months.map(month => this.formatMonth(month))

      this.receiveData = months.map(() => Math.floor(Math.random() * 100) + 50)
      this.sendData = months.map(() => Math.floor(Math.random() * 80) + 30)

      // 初始化图表
      this.$nextTick(() => {
        this.initCharts()
      })
    },

    // 生成最近几个月的年月数据
    generateRecentMonths(count) {
      const months = []
      const now = new Date()

      for (let i = count - 1; i >= 0; i--) {
        const date = new Date(now.getFullYear(), now.getMonth() - i, 1)
        const year = date.getFullYear()
        const month = date.getMonth() + 1
        months.push(`${year}-${month.toString().padStart(2, '0')}`)
      }

      return months
    },

    // 初始化图表
    initCharts() {
      this.initReceiveChart()
      this.initSendChart()
    },

    // 初始化接收图表
    initReceiveChart() {
      const chartDom = document.getElementById('receiveChart')
      if (!chartDom) return

      this.receiveChart = echarts.init(chartDom)

      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: '{b}<br/>{a}: {c}'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '15%',
          top: '10%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: this.monthLabels,
          axisLine: {
            lineStyle: {
              color: '#E0E0E0'
            }
          },
          axisLabel: {
            color: '#606266',
            fontSize: 12
          }
        },
        yAxis: {
          type: 'value',
          name: '数量',
          nameTextStyle: {
            color: '#909399',
            fontSize: 12
          },
          axisLine: {
            show: true,
            lineStyle: {
              color: '#E0E0E0'
            }
          },
          axisLabel: {
            color: '#606266',
            fontSize: 12
          },
          splitLine: {
            lineStyle: {
              color: '#F0F0F0',
              type: 'dashed'
            }
          }
        },
        series: [
          {
            name: '接收数量',
            type: 'bar',
            data: this.receiveData,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#667eea' },
                { offset: 1, color: '#764ba2' }
              ]),
              borderRadius: [4, 4, 0, 0]
            },
            label: {
              show: true,
              position: 'top',
              color: '#303133',
              fontSize: 12,
              fontWeight: 'bold'
            },
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowColor: 'rgba(102, 126, 234, 0.5)'
              }
            }
          }
        ]
      }

      this.receiveChart.setOption(option)

      // 响应式调整
      window.addEventListener('resize', () => {
        this.receiveChart.resize()
      })
    },

    // 初始化发送图表
    initSendChart() {
      const chartDom = document.getElementById('sendChart')
      if (!chartDom) return

      this.sendChart = echarts.init(chartDom)

      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: '{b}<br/>{a}: {c}'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '15%',
          top: '10%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: this.monthLabels,
          axisLine: {
            lineStyle: {
              color: '#E0E0E0'
            }
          },
          axisLabel: {
            color: '#606266',
            fontSize: 12
          }
        },
        yAxis: {
          type: 'value',
          name: '数量',
          nameTextStyle: {
            color: '#909399',
            fontSize: 12
          },
          axisLine: {
            show: true,
            lineStyle: {
              color: '#E0E0E0'
            }
          },
          axisLabel: {
            color: '#606266',
            fontSize: 12
          },
          splitLine: {
            lineStyle: {
              color: '#F0F0F0',
              type: 'dashed'
            }
          }
        },
        series: [
          {
            name: '生成数量',
            type: 'bar',
            data: this.sendData,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#f093fb' },
                { offset: 1, color: '#f5576c' }
              ]),
              borderRadius: [4, 4, 0, 0]
            },
            label: {
              show: true,
              position: 'top',
              color: '#303133',
              fontSize: 12,
              fontWeight: 'bold'
            },
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowColor: 'rgba(245, 87, 108, 0.5)'
              }
            }
          }
        ]
      }

      this.sendChart.setOption(option)

      // 响应式调整
      window.addEventListener('resize', () => {
        this.sendChart.resize()
      })
    },

    // 格式化月份显示
    formatMonth(monthStr) {
      if (!monthStr) return '未知'

      // 处理 '2024-01' 格式
      if (monthStr.includes('-')) {
        const [year, month] = monthStr.split('-')
        return `${year}年${parseInt(month)}月`
      }

      // 处理其他格式
      return monthStr
    }
  }
}
</script>

<style scoped>
.statistics-panel {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.system-title {
  text-align: center;
  margin-bottom: 30px;
  padding-top: 10px;
}

.system-title h1 {
  color: #303133;
  font-weight: 600;
  margin: 0;
  font-size: 28px;
  letter-spacing: 1px;
}

.section-header {
  text-align: center;
  margin-bottom: 30px;
  color: #1c84c6;
}

.section-title {
  font-size: 20px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 12px;
  letter-spacing: 0.5px;
}

.section-divider {
  height: 2px;
  background: linear-gradient(90deg,
  transparent 0%,
  #409EFF 50%,
  transparent 100%);
  width: 200px;
  margin: 0 auto;
}

.charts-section {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 8px;
  border: 1px solid #ebeef5;
  height: 400px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.chart-title-section {
  padding: 20px 20px 10px 20px;
  text-align: center;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 10px;
}

.chart-main-title {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
  letter-spacing: 0.5px;
}

.chart-subtitle {
  font-size: 14px;
  color: #909399;
  font-weight: normal;
}

.chart-container {
  height: 300px;
  padding: 10px;
}

.echarts-chart {
  width: 100%;
  height: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .charts-section .el-col {
    margin-bottom: 15px;
  }

  .chart-card {
    height: 350px;
  }

  .chart-container {
    height: 250px;
  }

  .system-title h1 {
    font-size: 24px;
  }

  .section-title {
    font-size: 18px;
  }

  .chart-main-title {
    font-size: 16px;
  }
}
</style>
