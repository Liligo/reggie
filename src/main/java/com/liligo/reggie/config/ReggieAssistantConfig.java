package com.liligo.reggie.config;

import java.util.List;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


@Component
public class ReggieAssistantConfig {
    private final MongoChatMemoryStoreConfig mongoChatMemoryStoreConfig;
    private final EmbeddingModel qwenEmbeddingModel;
    private final EmbeddingStore<TextSegment> pineconeEmbeddingStore;

    public ReggieAssistantConfig(MongoChatMemoryStoreConfig mongoChatMemoryStoreConfig,
                                 EmbeddingModel qwenEmbeddingModel,
                                 EmbeddingStore<TextSegment> pineconeEmbeddingStore) {
        this.mongoChatMemoryStoreConfig = mongoChatMemoryStoreConfig;
        this.qwenEmbeddingModel = qwenEmbeddingModel;
        this.pineconeEmbeddingStore = pineconeEmbeddingStore;
    }

    @Bean
    public ChatMemoryProvider reggieChatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(50)
                .chatMemoryStore(mongoChatMemoryStoreConfig)
                .build();
    }

    /**
     * 创建内容检索器Bean
     * <p>用于RAG（Retrieval-Augmented Generation）流程，基于文档嵌入实现知识检索</p>
     *
     * @return 基于内存嵌入存储的内容检索器
     */
    @Bean
    public ContentRetriever reggieContentRetriever() {
        // 从资源目录加载文档并使用默认的文档解析器进行解析
        List<Document> documents = FileSystemDocumentLoader.loadDocuments("src/main/resources/documents");

        // 创建内存嵌入存储（临时存储文档的向量数据）
//        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        // 将文档内容摄入（处理并存储）到嵌入存储中
        EmbeddingStoreIngestor.builder()
                .embeddingStore(pineconeEmbeddingStore)
                .embeddingModel(qwenEmbeddingModel)
                .build()
                .ingest(documents);

        // 构建基于嵌入存储的内容检索器
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(pineconeEmbeddingStore)
                .embeddingModel(qwenEmbeddingModel)
                .maxResults(3)
                .minScore(0.8)
                .build();
    }
}
