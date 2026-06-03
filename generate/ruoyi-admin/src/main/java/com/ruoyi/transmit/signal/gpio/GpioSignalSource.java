package com.ruoyi.transmit.signal.gpio;

import com.ruoyi.transmit.signal.SuccessSignalResult;
import com.ruoyi.transmit.signal.SuccessSignalSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 通过 C 函数读取 GPIO 电平，作为扫描成功信号来源。
 */
@Component
public class GpioSignalSource implements SuccessSignalSource
{
    private static final Logger logger = LoggerFactory.getLogger(GpioSignalSource.class);

    @Autowired
    private GpioSignalProperties properties;

    private GpioNativeBridge nativeBridge;
    private boolean initialized;
    private boolean stubMode;

    @Override
    public synchronized boolean init()
    {
        if (initialized)
        {
            return true;
        }

        nativeBridge = GpioNativeBridge.Loader.load(properties);
        stubMode = nativeBridge instanceof GpioNativeBridge.StubGpioNativeBridge;

        int rc = nativeBridge.gpio_signal_init(properties.getPin());
        if (rc != 0)
        {
            logger.error("GPIO 初始化失败, pin={}, rc={}", properties.getPin(), rc);
            initialized = false;
            return false;
        }

        initialized = true;
        logger.info("GPIO 信号源初始化完成, pin={}, stubMode={}", properties.getPin(), stubMode);
        return true;
    }

    @Override
    public synchronized SuccessSignalResult readRaw()
    {
        if (!initialized)
        {
            return SuccessSignalResult.ERROR;
        }

        int code = nativeBridge.gpio_signal_read();
        return SuccessSignalResult.fromNativeCode(code);
    }

    @Override
    public synchronized void shutdown()
    {
        if (initialized && nativeBridge != null)
        {
            nativeBridge.gpio_signal_shutdown();
            logger.info("GPIO 信号源已关闭");
        }
        initialized = false;
    }

    @Override
    public boolean isAvailable()
    {
        return initialized;
    }

    @Override
    public String getDescription()
    {
        return stubMode
                ? "GPIO(stub, pin=" + properties.getPin() + ")"
                : "GPIO(native, pin=" + properties.getPin() + ")";
    }

    public boolean isStubMode()
    {
        return stubMode;
    }
}
