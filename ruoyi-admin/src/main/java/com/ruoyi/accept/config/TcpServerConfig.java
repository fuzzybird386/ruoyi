package com.ruoyi.accept.config;


import com.ruoyi.accept.server.TcpServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.annotation.PreDestroy;
import com.ruoyi.accept.server.TcpServer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;



@Slf4j
@Configuration
@ConfigurationProperties(prefix = "tcp.server")
public class TcpServerConfig {

//    @Value("${tcp.server.port:9000}")
    private int port = 9000;
    private String charset = "UTF-8";

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

//    @Bean(name = "tcpServerBean", initMethod = "start", destroyMethod = "stop")
//    public TcpServer tcpServer() {
//        return new TcpServer(port, charset);
//    }
//
//    @Bean(initMethod = "start", destroyMethod = "stop")
//    public TcpServer tcpServer() {
//        return new TcpServer(tcpPort);
//    }

//    @Value("${tcp.server.port:8990}")
//    private int tcpPort;

//    @Value("${tcp.server.backup-port:8991}")
//    private int backupPort;
//
//    @Value("${tcp.server.auto-retry:true}")
//    private boolean autoRetry;
//
//    @Value("${tcp.server.boss-thread-count:1}")
//    private int bossThreadCount;
//
//    @Value("${tcp.server.worker-thread-count:4}")
//    private int workerThreadCount;
//
//    private TcpServer tcpServer;


//    /**
//     * 初始化TCP服务器
//     */
//    @Bean
//    public TcpServer tcpServer() {
//        try {
//            // 创建TCP服务器实例
//            tcpServer = new TcpServer(tcpPort, bossThreadCount, workerThreadCount);
//
//            // 设置DataForwardService到TCP服务器
//            tcpServer.setDataForwardService(dataForwardService);
//
//            tcpServer.start();
//            log.info("TCP服务器启动成功，监听端口: {}", tcpPort);
//            return tcpServer;
//
//        } catch (Exception e) {
//            if (e instanceof java.net.BindException && autoRetry) {
//                log.warn("主端口 {} 被占用，尝试备用端口 {}", tcpPort, backupPort);
//
//                try {
//                    // 尝试备用端口
//                    tcpServer = new TcpServer(backupPort, bossThreadCount, workerThreadCount);
//                    tcpServer.setDataForwardService(dataForwardService);
//                    tcpServer.start();
//                    log.info("TCP服务器启动成功，监听备用端口: {}", backupPort);
//                    return tcpServer;
//
//                } catch (Exception ex) {
//                    log.error("备用端口 {} 也启动失败", backupPort, ex);
//                    throw new RuntimeException("TCP服务器启动失败，所有备用端口都不可用", ex);
//                }
//            } else {
//                log.error("TCP服务器启动失败", e);
//                throw new RuntimeException("TCP服务器启动失败", e);
//            }
//        }
//    }

    /**
     * 销毁TCP服务器
     */
//    @PreDestroy
//    public void destroy() {
//        if (tcpServer != null) {
//            try {
//                tcpServer.stop();
//                log.info("TCP服务器已关闭");
//            } catch (Exception e) {
//                log.error("关闭TCP服务器时发生错误", e);
//            }
//        }
//    }
}
