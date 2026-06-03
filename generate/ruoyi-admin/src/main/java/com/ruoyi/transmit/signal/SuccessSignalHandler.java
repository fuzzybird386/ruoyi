package com.ruoyi.transmit.signal;

import com.ruoyi.transmit.mapper.QrCodeQueueManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 扫描成功信号统一处理：对接队列管理器并通知前端。
 */
@Component
public class SuccessSignalHandler
{
    private static final Logger logger = LoggerFactory.getLogger(SuccessSignalHandler.class);

    @Autowired
    private QrCodeQueueManager qrCodeQueueManager;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 处理带数据 ID 的成功信号。
     */
    public boolean handleSuccess(String dataId)
    {
        logger.info("收到扫描成功信号, dataId={}", dataId);
        boolean handled = qrCodeQueueManager.handleSuccessSignal(dataId);
        if (handled)
        {
            notifyFrontendSuccess(dataId);
        }
        else
        {
            logger.warn("成功信号未匹配当前处理数据, dataId={}", dataId);
        }
        return handled;
    }

    /**
     * 处理通用成功信号（对应当前正在展示/刷新的二维码）。
     */
    public boolean handleGeneralSuccess()
    {
        logger.info("收到通用扫描成功信号");
        boolean handled = qrCodeQueueManager.handleGeneralSuccessSignal();
        if (handled)
        {
            notifyFrontendSuccess(null);
        }
        else
        {
            logger.warn("通用成功信号被忽略：当前无处理中的数据");
        }
        return handled;
    }

    /**
     * 手动触发成功（测试 / 运维接口）。
     */
    public boolean manualTrigger()
    {
        return handleGeneralSuccess();
    }

    private void notifyFrontendSuccess(String dataId)
    {
        try
        {
            Map<String, Object> confirmation = new HashMap<>();
            confirmation.put("type", "DATA_SUCCESS");
            confirmation.put("receive_signal", true);
            confirmation.put("timestamp", System.currentTimeMillis());
            if (dataId != null)
            {
                confirmation.put("dataId", dataId);
            }
            messagingTemplate.convertAndSend("/topic/qrCodeUpdates", confirmation);
            logger.info("已向前端推送扫描成功确认");
        }
        catch (Exception e)
        {
            logger.error("向前端推送扫描成功确认失败: {}", e.getMessage(), e);
        }
    }
}
