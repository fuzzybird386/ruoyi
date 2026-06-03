package com.ruoyi.transmit.signal.gpio;

import com.ruoyi.transmit.signal.SuccessSignalHandler;
import com.ruoyi.transmit.signal.SuccessSignalResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 轮询 GPIO 信号并触发队列成功处理，替代原 TCP 1919 信号接收。
 */
@Component
public class GpioSignalMonitor
{
    private static final Logger logger = LoggerFactory.getLogger(GpioSignalMonitor.class);

    @Autowired
    private GpioSignalProperties properties;

    @Autowired
    private GpioSignalSource gpioSignalSource;

    @Autowired
    private SuccessSignalHandler successSignalHandler;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "GpioSignalMonitor");
        t.setDaemon(true);
        return t;
    });

    private ScheduledFuture<?> pollTask;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private boolean lastActive = false;

    @PostConstruct
    public void start()
    {
        if (!properties.isEnabled())
        {
            logger.info("GPIO 信号监控未启用 (scan.signal.gpio.enabled=false)");
            return;
        }
        startMonitor();
    }

    public synchronized void startMonitor()
    {
        if (running.get())
        {
            logger.warn("GPIO 信号监控已在运行");
            return;
        }

        if (!gpioSignalSource.init())
        {
            logger.error("GPIO 信号监控启动失败：初始化未成功");
            return;
        }

        lastActive = false;
        running.set(true);
        pollTask = scheduler.scheduleAtFixedRate(
                this::pollOnce,
                0,
                properties.getPollIntervalMs(),
                TimeUnit.MILLISECONDS);

        logger.info("GPIO 信号监控已启动, interval={}ms, mode={}",
                properties.getPollIntervalMs(),
                properties.getDetectMode());
    }

    public synchronized void stopMonitor()
    {
        running.set(false);
        if (pollTask != null)
        {
            pollTask.cancel(true);
            pollTask = null;
        }
        gpioSignalSource.shutdown();
        logger.info("GPIO 信号监控已停止");
    }

    @PreDestroy
    public void destroy()
    {
        stopMonitor();
        scheduler.shutdownNow();
    }

    public synchronized void restartMonitor()
    {
        stopMonitor();
        startMonitor();
    }

    public Map<String, Object> getStatus()
    {
        Map<String, Object> status = new HashMap<>();
        status.put("monitorRunning", running.get());
        status.put("sourceAvailable", gpioSignalSource.isAvailable());
        status.put("sourceDescription", gpioSignalSource.getDescription());
        status.put("stubMode", gpioSignalSource.isStubMode());
        status.put("pin", properties.getPin());
        status.put("pollIntervalMs", properties.getPollIntervalMs());
        status.put("detectMode", properties.getDetectMode());
        status.put("activeLevel", properties.getActiveLevel());
        return status;
    }

    private void pollOnce()
    {
        if (!running.get())
        {
            return;
        }

        try
        {
            SuccessSignalResult raw = gpioSignalSource.readRaw();
            if (raw == SuccessSignalResult.ERROR)
            {
                logger.warn("GPIO 读取返回 ERROR");
                return;
            }

            boolean active = isActive(raw);
            if (properties.isRisingEdgeMode())
            {
                if (active && !lastActive)
                {
                    successSignalHandler.handleGeneralSuccess();
                }
            }
            else if (active)
            {
                successSignalHandler.handleGeneralSuccess();
            }

            lastActive = active;
        }
        catch (Exception ex)
        {
            logger.error("GPIO 轮询异常: {}", ex.getMessage(), ex);
        }
    }

    private boolean isActive(SuccessSignalResult raw)
    {
        if (raw == SuccessSignalResult.SUCCESS)
        {
            return true;
        }

        // 预留：若 C 层直接返回电平值，可在此扩展
        return false;
    }
}
