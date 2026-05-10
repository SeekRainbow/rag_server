package com.jankzheng.rag_server.config;


import com.jankzheng.rag_server.tool.RagTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private RagTool ragTool;

    //defaultSystem:设置系统提示词，可以给大模型设置角色、分配任务、提要求等,越详细，agent执行越准确
    //chatclient通过自动装配application.yml中配置的的ollama大模型
    @Bean
    public ChatClient chatClient() {
        return chatClientBuilder
                .defaultSystem("""
                        你是一个拥有知识库的 AI 助手。
                        当用户提问时，使用工具查找相关信息。
                        遵循以下流程
                        1. 思考：分析用户需求
                        2. 行动：使用searchKnowledgeBase工具检索相关文档
                        3. 观察：查看检索到的信息
                        4. 回答：基于检索到的信息提供全面的回复
                        始终基于知识库中的信息回答问题。
                        如果未找到相关信息，请明确说明。
                        """)
                .defaultTools(ragTool)
                .build();
    }

}
