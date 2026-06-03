package com.ruoyi.accept.server;



import com.ruoyi.accept.handler.TcpClientHandler;
import com.ruoyi.accept.processor.DataProcessor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.BindException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;




@Component
public class TcpServer {

    private static final Logger log = LoggerFactory.getLogger(TcpServer.class);

    @Value("${tcp.server.port:9797}") // 从配置读取端口，默认9797
    private int port;

    @Value("${tcp.server.charset:UTF-8}") // 从配置读取字符集，默认UTF-8
    private String charset;

    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private volatile boolean running = false;

    @Autowired
    private DataProcessor dataProcessor;

    // 移除带参数的构造函数，使用默认构造函数
    public TcpServer() {
        // 默认构造函数，Spring会通过setter或字段注入设置属性
    }

    @PostConstruct
    public void start() {
        executorService = Executors.newCachedThreadPool();
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            log.info("TCP服务器启动成功，监听端口: {}", port);

            // 启动监听线程
            new Thread(this::listen).start();

        } catch (IOException e) {
            log.error("TCP服务器启动失败", e);
        }
    }

    private void listen() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                log.info("接收到新的TCP连接: {}", socket.getRemoteSocketAddress());

                // 为每个连接创建处理线程
                executorService.execute(new TcpClientHandler(socket, charset, dataProcessor));

            } catch (IOException e) {
                if (running) {
                    log.error("TCP连接接受异常", e);
                }
            }
        }
    }

    @PreDestroy
    public void stop() {
        running = false;
        if (executorService != null) {
            executorService.shutdown();
        }
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                log.error("关闭TCP服务器异常", e);
            }
        }
        log.info("TCP服务器已停止");
    }

    // 发送确认信息给数据发送方
    public void sendConfirmation(OutputStream output) throws IOException {
        String confirmation = "1\n"; // 发送数字1作为确认信息
        output.write(confirmation.getBytes(StandardCharsets.UTF_8));
        output.flush();
        log.info("已发送确认信息: 1");
    }

    // 获取服务器状态
    public boolean isRunning() {
        return running;
    }

    // Getter 和 Setter 方法
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

}
