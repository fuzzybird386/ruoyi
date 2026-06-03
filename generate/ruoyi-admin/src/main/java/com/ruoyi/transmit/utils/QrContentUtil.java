package com.ruoyi.transmit.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * 二维码内容序列化（与 SmsController.autoGenerate 保持一致）。
 */
public final class QrContentUtil
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private QrContentUtil()
    {
    }

    public static String buildContent(Map<String, Object> data) throws Exception
    {
        if (data == null || data.isEmpty())
        {
            return "{\"message\":\"暂无处理数据\"}";
        }

        Map<String, Object> cleanData = new HashMap<>(data);
        cleanData.keySet().removeIf(key -> key.startsWith("_"));
        return OBJECT_MAPPER.writeValueAsString(cleanData);
    }
}
