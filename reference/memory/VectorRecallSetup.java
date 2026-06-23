/*
 * Mnemosyne v0 — DESIGN-STAGE reference. NOT-YET-COMPILED. No compile/run claim (#139, gate-1 TC4).
 * API per research v2 (docs.spring.io 2.0-SNAPSHOT); confirm at build. No company data (#145).
 *
 * Layer B — semantic recall: MongoDBAtlasVectorStore behind RetrievalAugmentationAdvisor.
 * See design/01 (architecture) + design/04 (topK/threshold bound the token cost).
 */
package ai.mnemosyne.memory;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static ai.mnemosyne.memory.config.MemoryArchitectureConfig.ORDER_VECTOR_RAG;

@Configuration
public class VectorRecallSetup {

    /**
     * The vector substrate. Designed against Spring AI's VectorStore abstraction (TC2) with
     * MongoDBAtlasVectorStore as the reference implementation. Swappable if the company stack
     * differs (pgvector, etc.) without touching the advisor below.
     */
    @Bean
    public VectorStore vectorStore(/* MongoTemplate mongoTemplate, */ EmbeddingModel embeddingModel) {
        // DESIGN-STAGE shape (confirm at build):
        //
        // return MongoDBAtlasVectorStore.builder(mongoTemplate, embeddingModel)
        //         .collectionName("mnemosyne_memory")
        //         .vectorIndexName("mnemosyne_vector_index")
        //         .pathName("embedding")
        //         .numCandidates(200)
        //         .metadataFieldsToFilter(List.of("type", "department", "source"))
        //         .initializeSchema(true)   // auto-creates the Atlas vector index
        //         .build();

        throw new UnsupportedOperationException("DESIGN-STAGE sketch: build MongoDBAtlasVectorStore at build time.");
    }

    /**
     * Modular RAG advisor (design/01). Bounded retrieval (similarityThreshold + topK via the
     * retriever) keeps injected context small per design/04. allowEmptyContext(true) so a no-hit
     * query degrades gracefully instead of refusing.
     */
    @Bean("vectorRagAdvisor")
    public Advisor vectorRagAdvisor(VectorStore vectorStore) {
        // DESIGN-STAGE shape (confirm at build):
        //
        // return RetrievalAugmentationAdvisor.builder()
        //         .documentRetriever(VectorStoreDocumentRetriever.builder()
        //                 .vectorStore(vectorStore)
        //                 .similarityThreshold(0.5)
        //                 .topK(6)   // confirm at build: research v2 showed topK on SearchRequest.builder;
        //                            // topK on the retriever builder is plausible-but-unverified.
        //                 .build())
        //         .queryTransformers(RewriteQueryTransformer.builder().build())   // optional query rewrite
        //         .queryAugmenter(ContextualQueryAugmenter.builder().allowEmptyContext(true).build())
        //         .order(ORDER_VECTOR_RAG)
        //         .build();

        throw new UnsupportedOperationException(
            "DESIGN-STAGE sketch: build RetrievalAugmentationAdvisor at build time, order=" + ORDER_VECTOR_RAG);
    }

    /**
     * ETL ingest path (additive, idempotent — NOT model-trusted, per design/02). Chunk -> embed -> add.
     * In production this is a deterministic pipeline, not a model action.
     */
    public void ingest(VectorStore vectorStore, List<Document> documents) {
        // DESIGN-STAGE: vectorStore.accept(new TokenTextSplitter().apply(documents));
        // or vectorStore.add(splitter.apply(reader.get()));
    }
}
