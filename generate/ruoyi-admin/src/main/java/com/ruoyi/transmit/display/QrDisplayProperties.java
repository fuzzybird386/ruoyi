package com.ruoyi.transmit.display;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 二维码显示配置（MIPI framebuffer / 浏览器）。
 */
@Component
@ConfigurationProperties(prefix = "scan.display")
public class QrDisplayProperties
{
    /** framebuffer：RK3588 MIPI 屏；browser：Vue 调 API 显示 PNG */
    private String mode = "framebuffer";

    private boolean enabled = true;

    private String python = "python3";

    /** show_qr.py 绝对路径，留空则使用 classpath 旁 scripts/show_qr.py */
    private String scriptPath = "";

    private String framebuffer = "/dev/fb0";

    public boolean isFramebufferMode()
    {
        return enabled && "framebuffer".equalsIgnoreCase(mode);
    }

    public String getMode()
    {
        return mode;
    }

    public void setMode(String mode)
    {
        this.mode = mode;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getPython()
    {
        return python;
    }

    public void setPython(String python)
    {
        this.python = python;
    }

    public String getScriptPath()
    {
        return scriptPath;
    }

    public void setScriptPath(String scriptPath)
    {
        this.scriptPath = scriptPath;
    }

    public String getFramebuffer()
    {
        return framebuffer;
    }

    public void setFramebuffer(String framebuffer)
    {
        this.framebuffer = framebuffer;
    }
}
