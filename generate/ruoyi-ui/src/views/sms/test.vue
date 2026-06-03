<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>二维码API测试页面</span>
          <el-tag type="primary">若依框架测试</el-tag>
        </div>
      </template>

      <!-- 测试控制面板 -->
      <div class="test-control-panel">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card shadow="hover">
              <template #header>
                <div class="panel-header">
                  <i class="el-icon-edit"></i>
                  <span>测试数据配置</span>
                </div>
              </template>

              <el-form :model="testForm" label-width="100px">
                <el-form-item label="测试场景">
                  <el-select v-model="selectedScenario" placeholder="选择测试场景" @change="handleScenarioChange">
                    <el-option label="订单数据" value="order"></el-option>
                    <el-option label="用户信息" value="user"></el-option>
                    <el-option label="产品信息" value="product"></el-option>
                    <el-option label="自定义数据" value="custom"></el-option>
                  </el-select>
                </el-form-item>

                <el-form-item label="请求数据">
                  <el-input
                    v-model="testForm.requestData"
                    type="textarea"
                    :rows="8"
                    placeholder="请输入JSON格式的测试数据"
                  ></el-input>
                </el-form-item>

                <el-form-item>
                  <el-button type="primary" @click="handleTest" :loading="testing">
                    <i class="el-icon-search"></i>
                    测试生成二维码
                  </el-button>
                  <el-button @click="handleReset">
                    <i class="el-icon-refresh"></i>
                    重置数据
                  </el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-col>

          <el-col :span="12">
            <el-card shadow="hover">
              <template #header>
                <div class="panel-header">
                  <i class="el-icon-picture"></i>
                  <span>测试结果</span>
                </div>
              </template>

              <div v-if="testResult.status" class="result-display">
                <el-alert
                  :title="testResult.message"
                  :type="testResult.status === 'success' ? 'success' : 'error'"
                  :closable="false"
                  show-icon
                />

                <div v-if="testResult.status === 'success'" class="qrcode-preview">
                  <el-divider content-position="center">生成的二维码</el-divider>
                  <div class="image-container">
                    <img :src="testResult.imageUrl" alt="测试二维码" class="qrcode-image" />
                  </div>

                  <div class="result-info">
                    <el-descriptions :column="1" border size="small">
                      <el-descriptions-item label="响应状态">
                        <el-tag type="success">200 OK</el-tag>
                      </el-descriptions-item>
                      <el-descriptions-item label="内容类型">
                        {{ testResult.contentType }}
                      </el-descriptions-item>
                      <el-descriptions-item label="数据大小">
                        {{ testResult.dataSize }} bytes
                      </el-descriptions-item>
                      <el-descriptions-item label="生成时间">
                        {{ testResult.timestamp }}
                      </el-descriptions-item>
                    </el-descriptions>
                  </div>
                </div>

                <div v-else class="error-details">
                  <el-divider content-position="center">错误详情</el-divider>
                  <pre class="error-message">{{ testResult.error }}</pre>
                </div>
              </div>

              <div v-else class="empty-result">
                <el-empty description="暂无测试结果">
                  <span>请配置测试数据并点击"测试生成二维码"</span>
                </el-empty>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 快速测试用例 -->
      <el-divider content-position="left">快速测试用例</el-divider>
      <div class="quick-test-cases">
        <el-row :gutter="15">
          <el-col :span="6" v-for="(testCase, index) in quickTestCases" :key="index">
            <el-card shadow="hover" class="test-case-card">
              <template #header>
                <div class="case-header">
                  <i :class="testCase.icon"></i>
                  <span>{{ testCase.name }}</span>
                </div>
              </template>

              <div class="case-content">
                <p class="case-desc">{{ testCase.description }}</p>
                <el-button
                  type="primary"
                  size="small"
                  @click="runQuickTest(testCase.data)"
                  :loading="testing"
                >
                  运行测试
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- API调用详情 -->
      <el-divider content-position="left">API调用详情</el-divider>
      <div class="api-details">
        <el-alert
          title="测试API: POST http://10.146.182.141:8787/smsQrCode/autoGenerate"
          type="info"
          :closable="false"
        />

        <div class="code-section">
          <h4>请求示例:</h4>
          <pre><code>POST /smsQrCode/autoGenerate
Content-Type: application/json

{
  "orderId": "202401150001",
  "product": "智能手机",
  "price": 1999,
  "customer": "张三"
}</code></pre>

          <h4>响应示例:</h4>
          <pre><code>Content-Type: image/png
Status: 200 OK

[PNG图片二进制数据]</code></pre>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { autoGenerateQrCode } from "@/api/sms/qrcode";

export default {
  name: "QrCodeTest",
  data() {
    return {
      testing: false,
      selectedScenario: "order",
      testForm: {
        requestData: ''
      },
      testResult: {
        status: '',
        message: '',
        imageUrl: '',
        contentType: '',
        dataSize: 0,
        timestamp: '',
        error: ''
      },
      quickTestCases: [
        {
          name: '订单数据',
          icon: 'el-icon-shopping-bag-1',
          description: '测试订单信息的二维码生成',
          data: {
            orderId: "TEST202401150001",
            product: "智能手机",
            price: 1999,
            customer: "测试用户",
            status: "待支付",
            createTime: new Date().toISOString()
          }
        },
        {
          name: '用户信息',
          icon: 'el-icon-user',
          description: '测试用户信息的二维码生成',
          data: {
            userId: "U10001",
            userName: "张三",
            email: "zhangsan@example.com",
            phone: "13800138000",
            department: "技术部",
            role: "管理员"
          }
        },
        {
          name: '产品信息',
          icon: 'el-icon-goods',
          description: '测试产品信息的二维码生成',
          data: {
            productId: "P2024001",
            productName: "笔记本电脑",
            brand: "ThinkPad",
            model: "X1 Carbon",
            price: 12999,
            specs: {
              cpu: "i7-1260P",
              memory: "16GB",
              storage: "512GB SSD",
              display: "14英寸 2.2K"
            }
          }
        },
        {
          name: '系统信息',
          icon: 'el-icon-monitor',
          description: '测试系统状态二维码',
          data: {
            system: "单向传输系统",
            version: "4.7.1",
            timestamp: new Date().toISOString(),
            status: "运行正常",
            environment: "开发环境"
          }
        }
      ]
    };
  },

  mounted() {
    this.handleScenarioChange('order');
  },

  methods: {
    // 处理测试场景变化
    handleScenarioChange(scenario) {
      const testCase = this.quickTestCases.find(item =>
        item.name === {order: '订单数据', user: '用户信息', product: '产品信息', custom: '自定义数据'}[scenario]
      );

      if (testCase && scenario !== 'custom') {
        this.testForm.requestData = JSON.stringify(testCase.data, null, 2);
      } else if (scenario === 'custom') {
        this.testForm.requestData = JSON.stringify({
          message: "自定义测试数据",
          timestamp: new Date().toISOString()
        }, null, 2);
      }
    },

    // 执行测试
    async handleTest() {
      if (!this.testForm.requestData) {
        this.$message.warning("请输入测试数据");
        return;
      }

      let requestData;
      try {
        requestData = JSON.parse(this.testForm.requestData);
      } catch (error) {
        this.$message.error("JSON格式错误，请检查数据格式");
        return;
      }

      this.testing = true;

      try {
        this.$message.info("正在调用二维码生成API...");

        // 调用若依框架封装的API方法
        const response = await autoGenerateQrCode(requestData);

        // 创建图片URL
        const imageUrl = URL.createObjectURL(response);

        // 更新测试结果
        this.testResult = {
          status: 'success',
          message: `二维码生成成功 (${new Date().toLocaleString()})`,
          imageUrl: imageUrl,
          contentType: response.type || 'image/png',
          dataSize: response.size || 0,
          timestamp: new Date().toLocaleString(),
          error: ''
        };

        this.$message.success("二维码生成测试成功！");

      } catch (error) {
        console.error("API调用失败:", error);

        let errorMessage = "未知错误";
        if (error.response) {
          errorMessage = `服务器错误: ${error.response.status} - ${error.response.statusText}`;
        } else if (error.request) {
          errorMessage = "网络连接失败，请检查API服务是否启动";
        } else {
          errorMessage = error.message;
        }

        this.testResult = {
          status: 'error',
          message: '二维码生成失败',
          imageUrl: '',
          contentType: '',
          dataSize: 0,
          timestamp: new Date().toLocaleString(),
          error: errorMessage
        };

        this.$message.error(`测试失败: ${errorMessage}`);
      } finally {
        this.testing = false;
      }
    },

    // 运行快速测试
    runQuickTest(testData) {
      this.testForm.requestData = JSON.stringify(testData, null, 2);
      this.selectedScenario = 'custom';
      this.handleTest();
    },

    // 重置数据
    handleReset() {
      this.testForm.requestData = '';
      this.testResult = {
        status: '',
        message: '',
        imageUrl: '',
        contentType: '',
        dataSize: 0,
        timestamp: '',
        error: ''
      };
      this.selectedScenario = 'order';
      this.handleScenarioChange('order');
    }
  },

  beforeUnmount() {
    // 清理Blob URL
    if (this.testResult.imageUrl) {
      URL.revokeObjectURL(this.testResult.imageUrl);
    }
  }
};
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.test-control-panel {
  margin-bottom: 30px;
}

.result-display {
  min-height: 200px;
}

.qrcode-preview {
  margin-top: 20px;
}

.image-container {
  display: flex;
  justify-content: center;
  padding: 20px;
}

.qrcode-image {
  width: 200px;
  height: 200px;
  border: 1px solid #e1e1e1;
  border-radius: 4px;
}

.result-info {
  margin-top: 20px;
}

.error-details {
  margin-top: 20px;
}

.error-message {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 4px;
  color: #f56c6c;
  font-size: 14px;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.empty-result {
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.quick-test-cases {
  margin: 20px 0;
}

.test-case-card {
  height: 100%;
}

.case-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
}

.case-content {
  text-align: center;
}

.case-desc {
  font-size: 12px;
  color: #666;
  margin-bottom: 15px;
  height: 40px;
  overflow: hidden;
}

.code-section {
  margin-top: 20px;
}

.code-section h4 {
  margin: 15px 0 10px 0;
  color: #333;
}

.code-section pre {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 4px;
  padding: 15px;
  overflow-x: auto;
}

.code-section code {
  font-family: 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.4;
}
</style>
