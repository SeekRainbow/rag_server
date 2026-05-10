package com.jankzheng.rag_server.loader;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//@Component: 标记注释，表示该类是一个Bean对象，在项目启动时会被自动扫描后注入到IOC容器中
//@Slf4j: 创建日志对象,对象名默认为log
@Slf4j
@Component
public class KnowledgeBaseLoader {

    @Value("${app.knowledge.dir}")
    private String knowledgeDir;

    //Autowired：依赖注入
    @Autowired
    private VectorStore vectorStore;

    //@PostConstruct: 在该类构造后自动被执行的方法
    /**
     * 构建RAG索引，执行步骤
     * 1.加载文档
     * 2.文档分割
     * 3.向量化
     * 4.将向量化的数据存入向量数据库
     */
    @PostConstruct
    public void init() {
        try {
            //文档加载调用
            List<Document> documents = loadDocuments();
            //文档分割调用
            List<Document> splitDocuments = splitDocuments(documents);
            //向量化和入库
            vectorStore.add(splitDocuments);

            log.info("已加载{}个文档，已加载{}个文档块", documents.size(), splitDocuments.size());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //定义文档加载的方法
    private List<Document> loadDocuments() throws IOException {
        //读取文档
        List<Document> documents = new ArrayList<>();

        //创建资源解析器
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        //获取资源
        Resource[] resources = resolver.getResources(knowledgeDir + "/*");

        //遍历资源列表
        for (Resource resource : resources) {
            //定义文档读取器
            if (resource.isReadable()) {
                TikaDocumentReader reader = new TikaDocumentReader(resource);

                //读取文档,将结果添加到列表中
                documents.addAll(reader.get());
                log.info("已加载文档: {}", resource.getFilename());
            }
        }
        return documents;
    }

    //定义文档分割的方法
    private List<Document> splitDocuments(List<Document> documents) {
        //定义文档分割器
        //withChunkSize: 每个文档块的token数，也就是说每500个token分割一次
        //withMinChunkSizeChars: 每个文档块的最小字符数，也就是说每个文档块至少200个字符,避免文档块碎片化
        TokenTextSplitter splitter = TokenTextSplitter
                .builder()
                .withChunkSize(500)
                .withMinChunkSizeChars(200)
                .build();

        return splitter.apply(documents);
    }
}
