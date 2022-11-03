package com.xun.schedule.controller;

import com.xun.schedule.webScoket.WebSocketServer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author xun
 * @create 2022/11/2 14:15
 */
@RestController
@RequestMapping("/web_Socket")
public class WebSocketController {

    @RequestMapping("/push/{cid}")
    public Map pushToCus(@PathVariable String cid, String message) {
        Map<String, Object> result = new HashMap<>();
        try {
            WebSocketServer.sendInfo(message, cid);
            result.put("code", cid);
            result.put("msg", message);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
