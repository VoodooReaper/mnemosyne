/*
 * Mnemosyne v0 — DESIGN-STAGE reference. NOT-YET-COMPILED. No compile/run claim (#139, gate-1 TC4).
 * Community-module class/method names are fluid (spring-ai-agent-utils 2.0.0-M2+/SNAPSHOT); confirm at build.
 * No company data or source (decision #145) — placeholders only.
 *
 * Assembles the three memory layers as ordered advisors on ONE ChatClient.
 * See design/01-architecture.md for the stack diagram and ordering rationale.
 */
package ai.mnemosyne.memory.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MemoryArchitectureConfig {

    // Order constants, spaced by 100 so future advisors slot in without renumbering.
    public static final int ORDER_SESSION       = 0;   // Layer S: recent turns (framing)
    public static final int ORDER_FILE_CANON    = 100; // Layer A: pinned truth via memory tools
    public static final int ORDER_VECTOR_RAG    = 200; // Layer B: semantically-relevant context
    public static final int ORDER_TOOL_SEARCH   = 300; // token lever: matched tool defs only

    /**
     * The single ChatClient every request flows through. Backend (ChatModel) is injected and
     * is interchangeable per design/03-data-boundary.md (Bedrock / Vertex / Anthropic) — this
     * assembly does not know or care which backend is behind the interface.
     */
    @Bean
    public ChatClient memoryChatClient(
            ChatModel chatModel,
            @org.springframework.beans.factory.annotation.Qualifier("sessionAdvisor")     Advisor sessionAdvisor,
            @org.springframework.beans.factory.annotation.Qualifier("fileCanonAdvisor")   Advisor fileCanonAdvisor,
            @org.springframework.beans.factory.annotation.Qualifier("vectorRagAdvisor")   Advisor vectorRagAdvisor,
            @org.springframework.beans.factory.annotation.Qualifier("toolSearchAdvisor")  Advisor toolSearchAdvisor) {

        return ChatClient.builder(chatModel)
                .defaultAdvisors(
                        sessionAdvisor,     // ORDER_SESSION
                        fileCanonAdvisor,   // ORDER_FILE_CANON
                        vectorRagAdvisor,   // ORDER_VECTOR_RAG
                        toolSearchAdvisor)  // ORDER_TOOL_SEARCH
                .build();
        // Each advisor carries its own .order(...) (set in its Setup class); the builder respects it.
    }
}
