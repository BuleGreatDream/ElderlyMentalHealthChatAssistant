package org.demo.learn_langchain4j.RAG;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.chroma.ChromaApiVersion;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.demo.learn_langchain4j.Tools.FileTextTool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Configuration
public class RagConfig {

    private static final String DOCUMENT_FOLDER = "src/main/resources/Memory";

    @Value("${langchain4j.rag.chroma.base-url:http://localhost:8000}")
    private String chromaBaseUrl;

    @Value("${langchain4j.rag.chroma.collection-name:ai-helper-rag}")
    private String chromaCollectionName;

    @Value("${langchain4j.rag.chroma.auto-ingest:true}")
    private boolean chromaAutoIngest;

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(EmbeddingModel embeddingModel) {
        EmbeddingStore<TextSegment> embeddingStore = ChromaEmbeddingStore.builder()
                .baseUrl(chromaBaseUrl)
                .collectionName(chromaCollectionName)
                .apiVersion(ChromaApiVersion.V2)
                .build();

        // 使用 try-catch 来处理可能的集合已存在异常
        try {
            if (chromaAutoIngest && FileTextTool.hasAnyFile(DOCUMENT_FOLDER)) {
                System.out.println("=== 检测到文档，向 Chroma 向量库追加向量化 ===");
                loadAndIngestDocuments(embeddingStore, embeddingModel);
            } else {
                System.out.println("=== Chroma 向量库启动完成，未执行自动向量化 ===");
            }
        } catch (Exception e) {
            // 如果是因为集合已存在导致的异常，可以忽略，让系统继续运行
            if (e.getMessage() != null && e.getMessage().contains("already exists")) {
                System.out.println("=== 集合 " + chromaCollectionName + " 已存在，直接复用 ===");
            } else {
                throw new IllegalStateException("Failed to preload vector store", e);
            }
        }

        return embeddingStore;
    }

//    @Bean
//    public EmbeddingStore<TextSegment> embeddingStore(
//            EmbeddingModel embeddingModel
//    ) {
//        EmbeddingStore<TextSegment> embeddingStore = ChromaEmbeddingStore.builder()
//                .baseUrl(chromaBaseUrl)
//                .collectionName(chromaCollectionName)
//                .build();
//
//        try {
//            if (chromaAutoIngest && FileTextTool.hasAnyFile(DOCUMENT_FOLDER)) {
//                System.out.println("=== 检测到文档，向 Chroma 向量库追加向量化 ===");
//                loadAndIngestDocuments(embeddingStore, embeddingModel);
//            } else {
//                System.out.println("=== Chroma 向量库启动完成，未执行自动向量化 ===");
//            }
//        } catch (Exception e) {
//            throw new IllegalStateException("Failed to preload vector store", e);
//        }
//
//        return embeddingStore;
//    }


    // ====================== 核心 Bean ======================
    @Bean
    @DependsOn("embeddingStore") // 确保先初始化向量库
    public ContentRetriever contentRetriever(
            EmbeddingModel embeddingModel,
            EmbeddingStore<TextSegment> embeddingStore
    ) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(5)
                .minScore(0.75)
                .build();
    }

    // ====================== 读取文档（方法传入依赖，解除循环） ======================
    private void loadAndIngestDocuments(
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel
    ) {
        Path folderPath = Paths.get(DOCUMENT_FOLDER);
        if (!folderPath.toFile().exists()) {
            return;
        }

        List<Document> documents = FileSystemDocumentLoader.loadDocuments(folderPath);
        if (documents.isEmpty()) {
            return;
        }

        DocumentByParagraphSplitter splitter = new DocumentByParagraphSplitter(1000, 100);

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .textSegmentTransformer(seg -> TextSegment.from(
                        seg.metadata().getString("file_name") + "\n" + seg.text(),
                        seg.metadata()))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        ingestor.ingest(documents);
    }
}