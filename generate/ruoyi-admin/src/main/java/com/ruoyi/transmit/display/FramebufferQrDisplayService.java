package com.ruoyi.transmit.display;

import com.ruoyi.transmit.utils.QrContentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 调用 show_qr.py，在 MIPI framebuffer 上显示二维码（同 /home/phoney/show.py 逻辑）。
 */
@Service
public class FramebufferQrDisplayService
{
    private static final Logger logger = LoggerFactory.getLogger(FramebufferQrDisplayService.class);

    @Autowired
    private QrDisplayProperties properties;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "FramebufferQrDisplay");
        t.setDaemon(true);
        return t;
    });

    private Path resolvedScriptPath;

    @PostConstruct
    public void init()
    {
        resolvedScriptPath = resolveScriptPath();
        if (properties.isFramebufferMode())
        {
            logger.info("MIPI 二维码显示已启用: script={}, fb={}",
                    resolvedScriptPath, properties.getFramebuffer());
        }
    }

    public void displayAsync(Map<String, Object> data)
    {
        if (!properties.isFramebufferMode())
        {
            return;
        }
        executor.submit(() -> {
            if (data == null)
            {
                clearScreen();
            }
            else
            {
                display(data);
            }
        });
    }

    public boolean display(Map<String, Object> data)
    {
        if (!properties.isFramebufferMode())
        {
            return false;
        }

        try
        {
            String content = QrContentUtil.buildContent(data);
            return runScript(content, false);
        }
        catch (Exception e)
        {
            logger.error("MIPI 显示二维码失败", e);
            return false;
        }
    }

    public boolean clearScreen()
    {
        if (!properties.isFramebufferMode())
        {
            return false;
        }
        return runScript("", true);
    }

    private boolean runScript(String content, boolean clear)
    {
        if (resolvedScriptPath == null || !Files.isRegularFile(resolvedScriptPath))
        {
            logger.error("show_qr.py 不存在: {}", resolvedScriptPath);
            return false;
        }

        try
        {
            ProcessBuilder builder = new ProcessBuilder(
                    properties.getPython(),
                    resolvedScriptPath.toString(),
                    "--once",
                    "--fb", properties.getFramebuffer(),
                    clear ? "--clear" : "--stdin"
            );
            builder.redirectErrorStream(true);

            Process process = builder.start();

            if (!clear)
            {
                process.getOutputStream().write(content.getBytes(StandardCharsets.UTF_8));
            }
            process.getOutputStream().close();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    output.append(line).append('\n');
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0)
            {
                logger.error("show_qr.py 退出码 {}: {}", exitCode, output);
                return false;
            }

            logger.info("MIPI 显示成功: {}", output.toString().trim());
            return true;
        }
        catch (Exception e)
        {
            logger.error("执行 show_qr.py 失败", e);
            return false;
        }
    }

    private Path resolveScriptPath()
    {
        String configured = properties.getScriptPath();
        if (configured != null && !configured.trim().isEmpty())
        {
            return Paths.get(configured.trim());
        }

        Path relative = Paths.get("scripts/show_qr.py");
        if (Files.isRegularFile(relative))
        {
            return relative.toAbsolutePath();
        }

        return Paths.get("ruoyi-admin/scripts/show_qr.py").toAbsolutePath();
    }
}
