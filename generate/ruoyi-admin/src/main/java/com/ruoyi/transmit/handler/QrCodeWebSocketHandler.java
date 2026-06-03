package com.ruoyi.transmit.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class QrCodeWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(QrCodeWebSocketHandler.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void notifyNewQrCode(Map<String, Object> data) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "QR_CODE_UPDATE");
            message.put("data", data);
            message.put("timestamp", System.currentTimeMillis());
            message.put("message", "检测到新的二维码生成请求");

            // 发送到所有订阅的客户端
            messagingTemplate.convertAndSend("/topic/qrCodeUpdates", message);
            logger.info("【WebSocket】已发送二维码更新通知，数据大小: {}", data != null ? data.size() : 0);

        } catch (Exception e) {
            logger.error("【WebSocket】发送通知失败", e);
        }
    }

    // 新增：发送二维码图片Base64（可选方案）
    public void sendQrCodeImage(String base64Image, Map<String, Object> originalData) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "QR_CODE_IMAGE");
            message.put("imageData", base64Image);
            message.put("originalData", originalData);
            message.put("timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSend("/topic/qrCodeImages", message);
            logger.info("【WebSocket】已发送二维码图片数据");

        } catch (Exception e) {
            logger.error("【WebSocket】发送二维码图片失败", e);
        }
    }

    /**
     * 通知WebSocket客户端有新二维码（String类型数据）
     */
//    public void notifyNewQrCode(Map<String, String> data) {
//        Map<String, Object> convertedData = new HashMap<>(data);
//        notifyNewQrCode(convertedData);
//    }

    /**
     * 发送错误消息
     */
    public void sendErrorMessage(String error) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "ERROR");
            message.put("error", error);
            message.put("timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSend("/topic/qrCodeUpdates", message);
            logger.error("❌ WebSocket错误消息已发送: {}", error);
        } catch (Exception e) {
            logger.error("发送WebSocket错误消息失败: {}", e.getMessage());
        }
    }

    /**
     * 发送成功消息
     */
    public void sendSuccessMessage(String messageText) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "SUCCESS");
            message.put("message", messageText);
            message.put("timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSend("/topic/qrCodeUpdates", message);
            logger.info("✅ WebSocket成功消息已发送: {}", messageText);
        } catch (Exception e) {
            logger.error("发送WebSocket成功消息失败: {}", e.getMessage());
        }
    }
}
