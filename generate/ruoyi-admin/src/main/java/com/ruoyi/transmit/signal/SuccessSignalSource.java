package com.ruoyi.transmit.signal;

/**
 * 扫描成功信号来源（GPIO / 其它硬件）。
 */
public interface SuccessSignalSource
{
    /**
     * 初始化信号源（打开 GPIO、映射寄存器等）。
     *
     * @return true 表示初始化成功或处于桩模式
     */
    boolean init();

    /**
     * 读取当前原始信号电平/状态。
     */
    SuccessSignalResult readRaw();

    /**
     * 释放硬件资源。
     */
    void shutdown();

    /**
     * 是否可用（库已加载且初始化成功）。
     */
    boolean isAvailable();

    /**
     * 信号源描述，用于状态接口展示。
     */
    String getDescription();
}
