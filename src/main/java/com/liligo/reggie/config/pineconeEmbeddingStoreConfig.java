package com.liligo.reggie.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pinecone.PineconeEmbeddingStore;
import dev.langchain4j.store.embedding.pinecone.PineconeServerlessIndexConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class pineconeEmbeddingStoreConfig {
    @Autowired
    private EmbeddingModel qwenEmbeddingModel;

    @Bean
    public EmbeddingStore<TextSegment> pineconeEmbeddingStore() {
        return PineconeEmbeddingStore.builder()
                .apiKey("pcsk_4XL1go_8rY1xWU1Z9UwRWBkcA1msUxqAjpav6vgRcvUEyKTCSspweLj4pwSQ7P3bw2CXsM")
                .index("reggie-knowledge-index")
                .nameSpace("reggie-namespace")
                .createIndex(PineconeServerlessIndexConfig.builder()
                        .cloud("AWS")
                        .region("us-east-1")
                        .dimension(qwenEmbeddingModel.dimension())
                        .build())
                .build();
    }
}
