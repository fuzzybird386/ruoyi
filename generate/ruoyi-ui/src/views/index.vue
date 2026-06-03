
<!--<template>-->
<!--  <div class="statistics-panel">-->
<!--    &lt;!&ndash; 系统标题 &ndash;&gt;-->
<!--    <div class="system-title" style="color: #1c84c6">-->
<!--      <h1 >单向信息传输系统</h1>-->
<!--    </div>-->

<!--    &lt;!&ndash; 消息接收及转换情况 &ndash;&gt;-->
<!--    <div class="charts-section">-->
<!--      <div class="section-header" style="color: #00afff">-->
<!--        <div class="section-title">消息接收及转换情况</div>-->
<!--        <div class="section-divider"></div>-->
<!--      </div>-->

<!--    </div>-->

<!--  </div>-->
<!--</template>-->

<!--<script>-->

<!--export default {-->
<!--  name: 'StatisticsPanel',-->
<!--  data() {-->
<!--    return {-->

<!--    }-->
<!--  },-->
<!--  mounted() {-->
<!--    this.loadStatistics()-->
<!--  },-->

<!--  methods: {-->

<!--  }-->
<!--}-->
<!--</script>-->

<!--<style scoped>-->
<!--.statistics-panel {-->
<!--  padding: 20px;-->
<!--  background-color: #f5f7fa;-->
<!--  min-height: 100vh;-->
<!--}-->

<!--.system-title {-->
<!--  text-align: center;-->
<!--  margin-bottom: 30px;-->
<!--  padding-top: 10px;-->
<!--}-->

<!--.system-title h1 {-->
<!--  color: #303133;-->
<!--  font-weight: 600;-->
<!--  margin: 0;-->
<!--  font-size: 28px;-->
<!--  letter-spacing: 1px;-->
<!--}-->

<!--.section-header {-->
<!--  text-align: center;-->
<!--  margin-bottom: 30px;-->
<!--  color: #1c84c6;-->
<!--}-->

<!--.section-title {-->
<!--  font-size: 20px;-->
<!--  font-weight: 500;-->
<!--  color: #303133;-->
<!--  margin-bottom: 12px;-->
<!--  letter-spacing: 0.5px;-->
<!--}-->

<!--.section-divider {-->
<!--  height: 2px;-->
<!--  background: linear-gradient(90deg,-->
<!--  transparent 0%,-->
<!--  #409EFF 50%,-->
<!--  transparent 100%);-->
<!--  width: 200px;-->
<!--  margin: 0 auto;-->
<!--}-->

<!--.charts-section {-->
<!--  margin-bottom: 20px;-->
<!--}-->


<!--/* 响应式设计 */-->
<!--@media (max-width: 768px) {-->
<!--  .charts-section .el-col {-->
<!--    margin-bottom: 15px;-->
<!--  }-->

<!--  .chart-card {-->
<!--    height: 350px;-->
<!--  }-->

<!--  .chart-container {-->
<!--    height: 250px;-->
<!--  }-->

<!--  .system-title h1 {-->
<!--    font-size: 24px;-->
<!--  }-->

<!--  .section-title {-->
<!--    font-size: 18px;-->
<!--  }-->

<!--  .chart-main-title {-->
<!--    font-size: 16px;-->
<!--  }-->
<!--}-->
<!--</style>-->


<template>
  <div class="statistics-panel">
    <!-- 系统标题 -->
    <div class="system-title" style="color: #1c84c6">
      <h1>单向信息传输系统</h1>
    </div>

    <!-- 统计筛选 -->
    <div class="filter-section">
      <el-card shadow="never">
        <el-form :inline="true" :model="filterForm">
          <el-form-item label="选择年份">
            <el-select v-model="filterForm.year" placeholder="请选择年份" @change="loadStatistics">
              <el-option
                v-for="year in availableYears"
                :key="year"
                :label="year + '年'"
                :value="year"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadStatistics">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 消息接收及转换情况 -->
    <div class="charts-section">
      <div class="section-header" style="color: #00afff">
        <div class="section-title">消息接收及转换情况</div>
        <div class="section-divider"></div>
      </div>

      <el-row :gutter="20">
        <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
          <el-card class="chart-card" shadow="hover">
            <template #header>
              <div class="chart-main-title">
                {{ filterForm.year }}年数据接收与二维码生成统计
              </div>
            </template>
            <div class="chart-container">
              <div ref="monthlyChart" style="width: 100%; height: 400px;"></div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getStatistics } from "@/api/sms/qrcode";

export default {
  name: 'StatisticsPanel',
  data() {
    const currentYear = new Date().getFullYear()
    return {
      filterForm: {
        year: currentYear
      },
      availableYears: [currentYear, currentYear - 1, currentYear - 2], // 默认最近三年
      monthlyStats: [],
      monthlyChart: null
    }
  },
  mounted() {
    this.initChart()
    this.loadAvailableYears()
    this.loadStatistics()
  },
  beforeUnmount() {
    if (this.monthlyChart) {
      this.monthlyChart.dispose()
    }
  },
  methods: {
    // 初始化图表
    initChart() {
      this.monthlyChart = echarts.init(this.$refs.monthlyChart)

      // 设置默认配置
      const option = {
        title: {
          text: '月度数据统计',
          left: 'center',
          textStyle: {
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: function(params) {
            let result = params[0].name + '<br/>'
            params.forEach(item => {
              result += `${item.marker} ${item.seriesName}: ${item.value}<br/>`
            })
            return result
          }
        },
        legend: {
          data: ['数据接收量', '二维码生成量'],
          top: '10%',
          textStyle: {
            fontSize: 12
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: '20%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
          axisLabel: {
            interval: 0,
            rotate: 0,
            fontSize: 12
          }
        },
        yAxis: {
          type: 'value',
          name: '数量',
          nameTextStyle: {
            fontSize: 12
          },
          axisLabel: {
            fontSize: 12
          }
        },
        series: [
          {
            name: '数据接收量',
            type: 'bar',
            barWidth: '35%',
            data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
            itemStyle: {
              color: '#5470c6'
            },
            emphasis: {
              focus: 'series'
            },
            label: {
              show: true,
              position: 'top',
              fontSize: 11
            }
          },
          {
            name: '二维码生成量',
            type: 'bar',
            barWidth: '35%',
            data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
            itemStyle: {
              color: '#91cc75'
            },
            emphasis: {
              focus: 'series'
            },
            label: {
              show: true,
              position: 'top',
              fontSize: 11
            }
          }
        ]
      }

      this.monthlyChart.setOption(option)

      // 响应窗口大小变化
      window.addEventListener('resize', this.handleResize)
    },

    // 处理窗口大小变化
    handleResize() {
      if (this.monthlyChart) {
        this.monthlyChart.resize()
      }
    },

    // 加载可用年份
    loadAvailableYears() {
      // 这里先使用默认年份，实际数据从API获取后会更新
      const currentYear = new Date().getFullYear()
      this.availableYears = [currentYear, currentYear - 1, currentYear - 2]
    },

    // 加载统计数据
    async loadStatistics() {
      try {
        this.loading = true
        const params = {
          year: this.filterForm.year
        }

        const response = await getStatistics(params)

        if (response.code === 200) {
          this.monthlyStats = response.data.monthlyStats || []
          this.availableYears = response.data.availableYears || this.availableYears
          this.updateMonthlyChart()
          this.$modal.msgSuccess("统计数据加载成功")
        } else {
          this.$modal.msgError(response.msg || "加载统计数据失败")
        }
      } catch (error) {
        console.error('加载统计失败:', error)
        this.$modal.msgError("加载统计数据失败")
        // 使用模拟数据作为fallback
        this.useMockData()
      } finally {
        this.loading = false
      }
    },

    // 使用模拟数据（备用）
    useMockData() {
      const mockData = []
      for (let i = 1; i <= 12; i++) {
        mockData.push({
          month: i,
          receiveCount: Math.floor(Math.random() * 100) + 20,
          qrCodeCount: Math.floor(Math.random() * 80) + 15
        })
      }
      this.monthlyStats = mockData
      this.updateMonthlyChart()
    },

    // 更新月度统计图表
    updateMonthlyChart() {
      if (!this.monthlyChart) return

      // 准备数据
      const receiveData = new Array(12).fill(0)
      const qrCodeData = new Array(12).fill(0)

      // 填充实际数据
      this.monthlyStats.forEach(stat => {
        const monthIndex = stat.month - 1
        if (monthIndex >= 0 && monthIndex < 12) {
          receiveData[monthIndex] = stat.receiveCount || 0
          qrCodeData[monthIndex] = stat.qrCodeCount || 0
        }
      })

      const option = {
        series: [
          {
            data: receiveData
          },
          {
            data: qrCodeData
          }
        ]
      }

      this.monthlyChart.setOption(option)
    },

    // 重置筛选
    resetFilter() {
      this.filterForm.year = new Date().getFullYear()
      this.loadStatistics()
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

.filter-section {
  margin-bottom: 20px;
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
  transition: all 0.3s ease;
}

.chart-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.chart-main-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  text-align: center;
}

.chart-container {
  height: 400px;
  padding: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .statistics-panel {
    padding: 10px;
  }

  .charts-section .el-col {
    margin-bottom: 15px;
  }

  .chart-card {
    height: auto;
  }

  .chart-container {
    height: 300px;
  }

  .system-title h1 {
    font-size: 24px;
  }

  .section-title {
    font-size: 18px;
  }

  .chart-main-title {
    font-size: 14px;
  }

  .filter-section .el-form-item {
    margin-bottom: 10px;
  }
}

@media (max-width: 480px) {
  .system-title h1 {
    font-size: 20px;
  }

  .section-title {
    font-size: 16px;
  }

  .chart-container {
    height: 250px;
  }
}
</style>
