package com.jankzheng.rag_server.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RagTool {

    @Autowired
    private VectorStore vectorStore;

    /**
     * 根据用户问题检索文档块
     * 1.用户问题向量化
     * 2.召回（在向量数据库中检索）
     * 3.返回检索的文本块给大模型
     */
    @Tool(description = "在知识库中检索与用户问题相似的信息，当需要在知识库中检索用户问题时使用该工具")
    public String searchKnowledgeBase(@ToolParam(description = "用于对用户问题进行检索的字段") String query) {
        log.info("RAG工具已调用，查询内容：{}", query);

        //定义检索的请求体
        //链式调用
        SearchRequest request = SearchRequest
                .builder()
                .query(query)
                .topK(3)                        //topk()表示取相似度前三的文档块
                .similarityThreshold(0.5)       //低于0.5的相似度结果过滤掉（不返回）
                .build();

        //在向量数据库中检索
        List<Document> documents = vectorStore.similaritySearch(request);

        //把检索文档列表转换为字符串
        if (documents.isEmpty()) {
            return "在向量数据库中没有检索到任何内容";
        }

        return documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));
    }
}
