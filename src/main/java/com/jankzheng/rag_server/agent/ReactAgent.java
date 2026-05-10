package com.jankzheng.rag_server.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReactAgent {

    @Autowired
    private ChatClient chatClient;

    /**
     * prompt(): 为chatClient创建提示词模板
     * user(): 设置用户问题
     * call(): 对话调用，将用户问题发送给LLM，LLM对问题进行分析，决定是否调用工具，最后生成答案
     * content(): 获取返回对象中的文本
     *
     * @Param userMessage
     * @return
     * */
    public String chat(String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
    }
}
