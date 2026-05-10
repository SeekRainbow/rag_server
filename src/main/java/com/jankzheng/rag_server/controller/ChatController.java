package com.jankzheng.rag_server.controller;


import com.jankzheng.rag_server.agent.ReactAgent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @RequestMapping 设置前端页面可以访问的url路径，设置在类上表示公共路径
 * 设置在方法上表示独立路径，其映射的完整路径为：类路径+方法路径
 *
 * @GetMapping 本质是@RequestMapping(method = RequestMethod.GET)
 * @PostMapping 本质是@RequestMapping(method = RequestMethod.POST)
 *
 * */
@RestController
@RequestMapping("/agent")
public class ChatController {

    @Autowired
    private ReactAgent reactAgent;

    @RequestMapping ("/chat")
    public String chat(@RequestParam String message) {
        return reactAgent.chat(message);
    }
}
