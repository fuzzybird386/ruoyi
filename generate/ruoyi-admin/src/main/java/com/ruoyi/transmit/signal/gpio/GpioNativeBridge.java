package com.ruoyi.transmit.signal.gpio;

import com.sun.jna.Library;
import com.sun.jna.Native;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Java 到 C GPIO 库的 JNA 映射。
 * <p>
 * 对应 native/gpio_signal.h 中导出的函数。
 */
public interface GpioNativeBridge extends Library
{
    int GPIO_SIGNAL_IDLE = 0;
    int GPIO_SIGNAL_SUCCESS = 1;
    int GPIO_SIGNAL_ERROR = -1;

    /**
     * 初始化 GPIO。
     *
     * @param pin 引脚编号
     * @return 0 成功，非 0 失败
     */
    int gpio_signal_init(int pin);

    /**
     * 读取当前信号。
     *
     * @return 0=空闲，1=成功，-1=错误
     */
    int gpio_signal_read();

    /**
     * 释放 GPIO 资源。
     */
    void gpio_signal_shutdown();

    final class Loader
    {
        private static final Logger logger = LoggerFactory.getLogger(Loader.class);

        private Loader()
        {
        }

        public static GpioNativeBridge load(GpioSignalProperties properties)
        {
            String libraryName = properties.getLibraryName();
            String libraryPath = properties.getLibraryPath();

            try
            {
                if (libraryPath != null && !libraryPath.trim().isEmpty())
                {
                    logger.info("加载 GPIO 本地库: {}", libraryPath);
                    return Native.load(libraryPath.trim(), GpioNativeBridge.class);
                }

                logger.info("加载 GPIO 本地库名称: {}", libraryName);
                return Native.load(libraryName, GpioNativeBridge.class);
            }
            catch (UnsatisfiedLinkError ex)
            {
                logger.warn("GPIO 本地库未找到，使用 Java 桩实现: {}", ex.getMessage());
                return new StubGpioNativeBridge();
            }
        }
    }

    /**
     * 本地库不可用时的桩实现，所有读操作返回 IDLE。
     */
    final class StubGpioNativeBridge implements GpioNativeBridge
    {
        @Override
        public int gpio_signal_init(int pin)
        {
            return 0;
        }

        @Override
        public int gpio_signal_read()
        {
            return GPIO_SIGNAL_IDLE;
        }

        @Override
        public void gpio_signal_shutdown()
        {
            // no-op
        }
    }
}
