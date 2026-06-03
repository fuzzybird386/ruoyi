package com.ruoyi.transmit.config;


import com.ruoyi.transmit.display.FramebufferQrDisplayService;
import com.ruoyi.transmit.handler.QrCodeWebSocketHandler;
import com.ruoyi.transmit.mapper.QrCodeQueueManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket回调实现
 */
@Component
public class QrCodeWebSocketCallback implements QrCodeQueueManager.QrCodeUpdateCallback {

    private static final Logger logger = LoggerFactory.getLogger(QrCodeWebSocketCallback.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    @Autowired
    private QrCodeQueueManager qrCodeQueueManager;

    @Autowired
    private FramebufferQrDisplayService framebufferQrDisplayService;

    @Override
    public void onQrCodeUpdate(Map<String, Object> data) {
        try {
            // MIPI 屏直接显示（show_qr.py → /dev/fb0）
            framebufferQrDisplayService.displayAsync(data);

            Map<String, Object> message = new HashMap<>();
            message.put("type", "QR_CODE_UPDATE");
            message.put("data", data);
            message.put("timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSend("/topic/qrCodeUpdates", message);
            logger.info("📢 WebSocket推送二维码更新: {}", message);
        } catch (Exception e) {
            logger.error("WebSocket推送失败", e);
        }
    }

    @Override
    public void onQueueStatusUpdate(int queue1Size, int queue2Size) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "QUEUE_STATUS_UPDATE");
            message.put("queue1Size", queue1Size);
            message.put("queue2Size", queue2Size);
            message.put("timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSend("/topic/qrCodeUpdates", message);
            logger.debug("📊 WebSocket推送队列状态 - 队列一: {}, 队列二: {}", queue1Size, queue2Size);
        } catch (Exception e) {
            logger.error("WebSocket队列状态推送失败", e);
        }
    }

    @Override
    public void onDataSuccess(String dataId) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "DATA_SUCCESS");
            message.put("dataId", dataId);
            message.put("timestamp", System.currentTimeMillis());
            message.put("message", "数据成功处理");

            messagingTemplate.convertAndSend("/topic/qrCodeUpdates", message);
            logger.info("✅ WebSocket推送数据成功: {}", dataId);
        } catch (Exception e) {
            logger.error("WebSocket成功通知推送失败", e);
        }
    }

    @Override
    public void onDataFailed(String dataId, String reason) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "DATA_FAILED");
            message.put("dataId", dataId);
            message.put("reason", reason);
            message.put("timestamp", System.currentTimeMillis());
            message.put("message", "数据处理失败");

            messagingTemplate.convertAndSend("/topic/qrCodeUpdates", message);
            logger.info("❌ WebSocket推送数据失败: {} - {}", dataId, reason);
        } catch (Exception e) {
            logger.error("WebSocket失败通知推送失败", e);
        }
    }

    /**
     * WebSocket 连接建立时发送完整状态
     */
    public void onWebSocketConnected() {
        // 发送当前处理的数据
        Map<String, Object> currentData = qrCodeQueueManager.getCurrentProcessingData();
        if (currentData != null) {
            onQrCodeUpdate(currentData);
        }

        // 发送队列状态
        onQueueStatusUpdate(qrCodeQueueManager.getQueue1Size(), qrCodeQueueManager.getQueue2Size());

        // 确保队列处理已启动
        qrCodeQueueManager.startProcessing();
    }
}
