package com.ruoyi.accept.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tcp.client")
@Data
public class TcpClientProperties {

    private String host = "125.196.21.249"; // 默认TCP服务器主机
    private int port = 9000;            //# 默认TCP服务器端口
    private int connectionTimeout = 5000; //# 连接超时时间(毫秒)
    private int readTimeout = 10000;    //# 读取超时时间(毫秒)
    private int retryCount = 3;         //# 重试次数
    private int retryInterval = 1000;//  # 重试间隔(毫秒)


    @Override
    public String toString() {
        return "TcpClientProperties{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", connectionTimeout=" + connectionTimeout +
                ", readTimeout=" + readTimeout +
                ", retryCount=" + retryCount +
                ", retryInterval=" + retryInterval +
                '}';
    }
}
