package com.ruoyi.transmit.signal.gpio;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * GPIO 扫描成功信号配置。
 */
@Component
@ConfigurationProperties(prefix = "scan.signal.gpio")
public class GpioSignalProperties
{
    /** 是否启用 GPIO 轮询 */
    private boolean enabled = true;

    /** 动态库名称（不含 lib 前缀与 .so 后缀），默认 gpio_signal */
    private String libraryName = "gpio_signal";

    /** 动态库绝对路径，留空则按 libraryName 搜索 */
    private String libraryPath = "";

    /** GPIO 引脚编号（具体含义由 C 层实现决定） */
    private int pin = 0;

    /** 轮询间隔（毫秒） */
    private long pollIntervalMs = 100;

    /**
     * 有效电平：1=高电平表示成功，0=低电平表示成功。
     */
    private int activeLevel = 1;

    /**
     * 检测模式：rising=上升沿触发一次，level=电平保持即视为成功。
     */
    private String detectMode = "rising";

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getLibraryName()
    {
        return libraryName;
    }

    public void setLibraryName(String libraryName)
    {
        this.libraryName = libraryName;
    }

    public String getLibraryPath()
    {
        return libraryPath;
    }

    public void setLibraryPath(String libraryPath)
    {
        this.libraryPath = libraryPath;
    }

    public int getPin()
    {
        return pin;
    }

    public void setPin(int pin)
    {
        this.pin = pin;
    }

    public long getPollIntervalMs()
    {
        return pollIntervalMs;
    }

    public void setPollIntervalMs(long pollIntervalMs)
    {
        this.pollIntervalMs = pollIntervalMs;
    }

    public int getActiveLevel()
    {
        return activeLevel;
    }

    public void setActiveLevel(int activeLevel)
    {
        this.activeLevel = activeLevel;
    }

    public String getDetectMode()
    {
        return detectMode;
    }

    public void setDetectMode(String detectMode)
    {
        this.detectMode = detectMode;
    }

    public boolean isRisingEdgeMode()
    {
        return !"level".equalsIgnoreCase(detectMode);
    }
}
