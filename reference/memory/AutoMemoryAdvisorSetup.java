/*
 * Mnemosyne v0 — DESIGN-STAGE reference. NOT-YET-COMPILED. No compile/run claim (#139, gate-1 TC4).
 * Community-module names are fluid (spring-ai-agent-utils); confirm at build. No company data (#145).
 *
 * Layer A — curated file canon via AutoAutoMemoryToolsAdvisor.
 * Maps 1:1 to Bishop's memory/*.md + MEMORY.md. See design/01 (architecture) + design/02 (hardening).
 */
package ai.mnemosyne.memory;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.time.Instant;
import java.util.function.BiPredicate;

import static ai.mnemosyne.memory.config.MemoryArchitectureConfig.ORDER_FILE_CANON;

@Configuration
public class AutoMemoryAdvisorSetup {

    /**
     * The file-canon advisor. Exposes the six memory tools (MemoryView/Create/StrReplace/Insert/
     * Delete/Rename), all sandboxed to memoriesRootDirectory (path-traversal + absolute-path guard).
     * The bundled system prompt teaches the four types (user/feedback/project/reference), the
     * two-step save, and staleness rules.
     *
     * HARDENING NOTE (design/02): in the hardened deployment, model-proposed writes are redirected
     * to a quarantine root (_inbox), NOT straight to canon. Two design options:
     *   (a) point memoriesRootDirectory at the quarantine dir and let MemoryCanonGate promote; or
     *   (b) wrap the memory ToolCallbacks so Create/StrReplace/Insert target _inbox.
     * Either way, promotion to canon is the deterministic gate's job, never the model's.
     */
    @Bean("fileCanonAdvisor")
    public Advisor fileCanonAdvisor(
            @Qualifier("autoMemorySystemPrompt") Resource memorySystemPrompt) {

        // Consolidation trigger: the framework default is (req,instant)->false (never).
        // We keep it conservative; real consolidation runs through the deterministic pipeline
        // (design/02 §4), not by trusting the model to self-consolidate on this trigger alone.
        // DESIGN-STAGE TYPE NOTE: research v2 types this as BiPredicate<ChatClientRequest, Instant>.
        // At build, import ChatClientRequest and type it correctly; Object is a placeholder here so
        // the sketch compiles in isolation without the community-module type on the classpath.
        BiPredicate<Object, Instant> consolidationTrigger = (req, instant) -> false;

        // DESIGN-STAGE: exact builder is AutoAutoMemoryToolsAdvisor.builder(...) per research v2.
        // Pseudocode-faithful shape (confirm method names at build):
        //
        // return AutoAutoMemoryToolsAdvisor.builder()
        //         .memoriesRootDirectory(Path.of("./memories"))   // sandboxed root (or _inbox, see note)
        //         .memorySystemPrompt(memorySystemPrompt)
        //         .memoryConsolidationTrigger(consolidationTrigger)
        //         .order(ORDER_FILE_CANON)
        //         .build();

        throw new UnsupportedOperationException(
            "DESIGN-STAGE sketch: wire AutoAutoMemoryToolsAdvisor.builder() at build time. "
          + "memoriesRootDirectory=<sandboxed>, order=" + ORDER_FILE_CANON);
    }
}
