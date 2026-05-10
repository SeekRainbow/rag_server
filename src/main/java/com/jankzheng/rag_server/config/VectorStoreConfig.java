package com.jankzheng.rag_server.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration表示该类是一个配置类，在springboot项目启动时自动配置
@Configuration
public class VectorStoreConfig {

    //向spring的IOC容器注入一个向量数据库对象
    //SimpleVectorStore是一个基于内存的向量数据库
    //embeddingModel会通过自动装配从application.yml文件中获取
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
