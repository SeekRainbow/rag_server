package com.jankzheng.rag_server.controller;

import com.jankzheng.rag_server.agent.ReactAgent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agent")
public class ChatController {

    @Autowired
    private ReactAgent reactAgent;

    @PostMapping("/chat")
    public String chat(@RequestParam String message, @RequestParam String conversationId) {
        return reactAgent.chat(message, conversationId);
    }

    @PostMapping("/clear")
    public String clear(@RequestParam String conversationId) {
        reactAgent.clearMemory(conversationId);
        return "ok";
    }
}
