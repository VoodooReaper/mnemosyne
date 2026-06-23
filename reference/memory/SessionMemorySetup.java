/*
 * Mnemosyne v0 — DESIGN-STAGE reference. NOT-YET-COMPILED. No compile/run claim (#139, gate-1 TC4).
 * API per research v2; confirm at build. No company data (#145).
 *
 * Layer S — short-term session/working memory + compaction.
 * This is the BOUNDARY layer, not the deliverable (see design/05). Specced because the long-term
 * layers sit beside it on the chain and durable facts are promoted OUT of it into canon.
 */
package ai.mnemosyne.memory;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static ai.mnemosyne.memory.config.MemoryArchitectureConfig.ORDER_SESSION;

@Configuration
public class SessionMemorySetup {

    /**
     * Working memory for the current conversation. A verbatim window of recent turns keeps
     * latency and tokens low (design/04); older turns are compacted by the strategy below.
     * Compaction is SAFE because durable facts have been promoted to canon (design/05 §contract).
     */
    @Bean
    public ChatMemory chatMemory() {
        // DESIGN-STAGE shape (confirm at build):
        //
        // return MessageWindowChatMemory.builder()
        //         .maxMessages(20)                 // verbatim window (deterministic cap, design/04)
        //         .build();
        // For event-sourced persistence + recursive-summarization compaction, wire the Session API
        // (JDBC) with a deterministic compaction trigger (token/message threshold), NOT a model-decided one.

        throw new UnsupportedOperationException("DESIGN-STAGE sketch: build ChatMemory/Session at build time.");
    }

    @Bean("sessionAdvisor")
    public Advisor sessionAdvisor(ChatMemory chatMemory) {
        // DESIGN-STAGE shape (confirm at build):
        //
        // return MessageChatMemoryAdvisor.builder(chatMemory)
        //         .order(ORDER_SESSION)
        //         .build();

        throw new UnsupportedOperationException(
            "DESIGN-STAGE sketch: build MessageChatMemoryAdvisor at build time, order=" + ORDER_SESSION);
    }
}
