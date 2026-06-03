package com.ruoyi.accept.handler;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.accept.processor.DataProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class TcpClientHandler implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(TcpClientHandler.class);

    private final Socket socket;
    private final String charset;
    private final DataProcessor dataProcessor;

    public TcpClientHandler(Socket socket, String charset, DataProcessor dataProcessor) {
        this.socket = socket;
        this.charset = charset;
        this.dataProcessor = dataProcessor;
    }

//@Override
//public void run() {
//    String connectionKey = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
//    log.info("✅ 新的TCP连接已建立: {}", connectionKey);
//
//    try (BufferedReader reader = new BufferedReader(
//            new InputStreamReader(socket.getInputStream(), charset))) {
//
//        StringBuilder dataBuilder = new StringBuilder();
//        String line;
//
//        while ((line = reader.readLine()) != null) {
//            log.debug("📥 收到TCP数据行 [{}]: {}", connectionKey, line);
//            dataBuilder.append(line);
//
//            // 判断是否为完整JSON
//            if (line.trim().endsWith("}")) {
//                String jsonData = dataBuilder.toString().trim();
//                if (!jsonData.isEmpty()) {
//                    log.info("✅ 收到完整TCP JSON数据 [{}]: {}", connectionKey, jsonData);
//
//                    try {
//                        // 处理接收到的数据，并传递socket用于后续发送确认
//                        dataProcessor.processReceivedData(jsonData, socket);
//                        log.info("✅ 已处理数据并交给DataProcessor");
//
//                        // 注意：这里不再立即发送确认，而是等待API调用成功后再发送
//                        log.info("⏳ [{}] 等待API调用成功后再发送确认信号...", connectionKey);
//
//                        // 存储socket关联信息，用于后续API调用时发送确认
//                        try {
//                            JSONObject obj = JSONObject.parseObject(jsonData);
//                            String sendUserID = obj.getString("sendUserID");
//                            String systemID = obj.getString("systemID");
//                            String dataId = generateDataId(sendUserID, systemID); // 生成唯一数据ID
//
//                            // 存储socket关联
//                            dataProcessor.storeDataSocketAssociation(dataId, socket);
//                            log.info("🔗 [{}] 已存储Socket关联: dataId={}", connectionKey, dataId);
//                        } catch (Exception e) {
//                            log.error("❌ [{}] 解析JSON生成dataId失败: {}", connectionKey, e.getMessage());
//                        }
//
//                    } catch (Exception dpEx) {
//                        log.error("❌ [{}] DataProcessor 处理异常: {}", connectionKey, dpEx.getMessage(), dpEx);
//                    }
//                }
//                dataBuilder.setLength(0); // 清空builder，准备接收下一条数据
//            }
//        }
//
//    } catch (SocketTimeoutException e) {
//        log.error("⏰ [{}] TCP连接读取超时: {}", connectionKey, e.getMessage());
//    } catch (IOException e) {
//        log.error("❌ [{}] TCP连接IO异常: {}", connectionKey, e.getMessage(), e);
//    } catch (Exception ex) {
//        log.error("❌ [{}] TCP连接未知异常: {}", connectionKey, ex.getMessage(), ex);
//    } finally {
//        try {
//            if (socket != null && !socket.isClosed()) {
//                socket.close();
//                log.info("🔌 [{}] 连接已关闭", connectionKey);
//
//                // 清理相关的socket存储
//                dataProcessor.cleanTcpSocket(connectionKey);
//                log.info("🧹 [{}] 已清理Socket关联", connectionKey);
//            }
//        } catch (IOException e) {
//            log.error("⚠️ [{}] 关闭Socket出错: {}", connectionKey, e.getMessage(), e);
//        }
//    }
//}

@Override
public void run() {
    String connectionKey = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
    log.info(" 新的TCP连接已建立: {}", connectionKey);

    try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(socket.getInputStream(), charset))) {

        StringBuilder dataBuilder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            log.debug(" 收到TCP数据行 [{}]: {}", connectionKey, line);
            dataBuilder.append(line);

            // 判断是否为完整JSON
            if (line.trim().endsWith("}")) {
                String jsonData = dataBuilder.toString().trim();
                if (!jsonData.isEmpty()) {
                    log.info(" 收到完整TCP JSON数据 [{}]: {}", connectionKey, jsonData);

                    try {
                        // 立即发送TCP确认信号
                        sendImmediateAck();
                        log.info(" 已立即发送TCP确认信号 '1'");

                        // 处理接收到的数据
                        processReceivedData(jsonData, connectionKey);

                    } catch (Exception dpEx) {
                        log.error("[{}] DataProcessor 处理异常: {}", connectionKey, dpEx.getMessage(), dpEx);
                    }
                }
                dataBuilder.setLength(0); // 清空builder，准备接收下一条数据
            }
        }

    } catch (SocketTimeoutException e) {
        log.error("⏰ [{}] TCP连接读取超时: {}", connectionKey, e.getMessage());
    } catch (IOException e) {
        log.error("❌ [{}] TCP连接IO异常: {}", connectionKey, e.getMessage(), e);
    } catch (Exception ex) {
        log.error("❌ [{}] TCP连接未知异常: {}", connectionKey, ex.getMessage(), ex);
    } finally {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                log.info("🔌 [{}] 连接已关闭", connectionKey);
            }
        } catch (IOException e) {
            log.error("⚠️ [{}] 关闭Socket出错: {}", connectionKey, e.getMessage(), e);
        }
    }
}

    /**
     * 处理接收到的数据
     */
//    private void processReceivedData(String jsonData, String connectionKey) {
//        try {
//            JSONObject jsonObject = JSONObject.parseObject(jsonData);
//
//            // 添加固定字段
//            jsonObject.put("systemId", "aaaaaaa252265878");
//
//            String modifiedJson = jsonObject.toJSONString();
//            log.info("🔄 [{}] 添加systemId后的数据: {}", connectionKey, modifiedJson);
//
//            // 放入队列一进行处理
//            dataProcessor.addToQueue1(modifiedJson);
//            log.info("✅ [{}] 数据已添加到队列一", connectionKey);
//
//        } catch (Exception e) {
//            log.error("❌ [{}] 处理接收数据异常: {}", connectionKey, e.getMessage(), e);
//        }
//    }
    /**
     * 处理接收到的数据 - 根据新字段要求处理
     */
    private void processReceivedData(String jsonData, String connectionKey) {
        try {
            JSONObject jsonObject = JSONObject.parseObject(jsonData);


            JSONObject modifiedData = new JSONObject();

            // 添加固定systemId字段
            modifiedData.put("systemId", "ba7c78f1706142c99d7c44ccdfecf58d");

            // 提取并设置content字段
            if (jsonObject.containsKey("content")) {
                modifiedData.put("content", jsonObject.getString("content"));
            } else {
                // 如果没有content字段，使用原始数据作为content
                modifiedData.put("content", jsonData);
            }

            // 提取并设置receivePhone字段
            if (jsonObject.containsKey("receivePhone")) {
                modifiedData.put("receivePhone", jsonObject.getString("receivePhone"));
            } else {
                log.warn("⚠️ [{}] 接收的数据缺少receivePhone字段", connectionKey);
//                modifiedData.put("receivePhone", ""); // 设置为空字符串
            }

            String modifiedJson = modifiedData.toJSONString();
            log.info("🔄 [{}] 处理后的数据: {}", connectionKey, modifiedJson);

            // 放入队列一进行处理
            dataProcessor.addToQueue1(modifiedJson);
            log.info("✅ [{}] 数据已添加到队列一", connectionKey);

        } catch (Exception e) {
            log.error("❌ [{}] 处理接收数据异常: {}", connectionKey, e.getMessage(), e);

            // 即使处理异常，也尝试将原始数据放入队列
            try {
                JSONObject fallbackData = new JSONObject();
                fallbackData.put("systemId", "ba7c78f1706142c99d7c44ccdfecf58d");
                fallbackData.put("content", jsonData);
                fallbackData.put("receivePhone", "");

                dataProcessor.addToQueue1(fallbackData.toJSONString());
                log.info("✅ [{}] 使用备用方案添加数据到队列一", connectionKey);
            } catch (Exception ex) {
                log.error("❌ [{}] 备用方案也失败: {}", connectionKey, ex.getMessage());
            }
        }
    }

    /**
     * 生成唯一数据ID
     * 根据业务需求自定义生成逻辑
     */
    private String generateDataId(String sendUserID, String systemID) {
        if (sendUserID == null) sendUserID = "unknown";
        if (systemID == null) systemID = "unknown";

        // 使用时间戳+用户ID+系统ID生成唯一ID
        return System.currentTimeMillis() + "_" + sendUserID + "_" + systemID;
    }

    /**
     * 立即发送接收确认
     */
//    private void sendImmediateAck() {
//        try {
////            // 这里可以立即发送一个接收成功的确认
////            String immediateAck = "RECEIVED\n";
////            socket.getOutputStream().write(immediateAck.getBytes("UTF-8"));
////            socket.getOutputStream().flush();
////            log.debug("已发送立即确认信息给: {}",
////                    socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
//            String immediateAck = "1";
//            socket.getOutputStream().write(immediateAck.getBytes(StandardCharsets.UTF_8));
//            socket.getOutputStream().flush();
//            log.info("✅ 已发送确认信号 '1' 给: {}", socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
//        } catch (IOException e) {
//            log.error("发送立即确认信息失败", e);
//        }
//    }
    private void sendImmediateAck() {
        try {
            String immediateAck = "1"; // 确认信号
            socket.setSoTimeout(5000); // 设置5秒发送超时
            OutputStream out = socket.getOutputStream();

            out.write(immediateAck.getBytes(StandardCharsets.UTF_8));
            out.flush();

            log.info("📤 已发送确认信号 '{}' 到客户端 [{}:{}]",
                    immediateAck,
                    socket.getInetAddress().getHostAddress(),
                    socket.getPort());
        } catch (SocketTimeoutException e) {
            log.error("⏰ 确认信号发送超时: {}", e.getMessage(), e);
        } catch (IOException e) {
            log.error("❌ 确认信号发送失败: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("⚠️ 未知错误，发送确认信号失败: {}", e.getMessage(), e);
        }
    }
}
