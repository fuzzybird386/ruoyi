package com.ruoyi.accept.controller;

import com.ruoyi.accept.entity.DataCountStats;


import com.ruoyi.accept.server.TcpServer;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/tcpdata")
public class TcpDataController extends BaseController {

    @Autowired
    private TcpServer tcpServer;

    /**
     * 获取TCP服务器状态
     */
    @GetMapping("/status")
    public String getStatus() {
        return "TCP服务器运行中";
    }

    /**
     * 重启TCP服务器
     */
    @PostMapping("/restart")
    public String restart() {
        tcpServer.stop();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        tcpServer.start();
        return "TCP服务器重启成功";
    }


}
