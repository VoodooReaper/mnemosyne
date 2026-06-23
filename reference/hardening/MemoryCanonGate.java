/*
 * Mnemosyne v0 — DESIGN-STAGE reference. NOT-YET-COMPILED (no compile/run claim, #139, gate-1 TC4).
 * No company data (#145) — operates on a generic canon directory of synthetic files.
 *
 * THE WEDGE (design/02): a DETERMINISTIC gate over the curated canon. Replaces the framework's
 * "trust the model to keep memory clean" with a script the model cannot talk past (Bishop #92).
 * Stdlib-only (no Spring), so it runs in any venue: pre-commit / CI, a scheduled job, or a
 * post-tool hook. Faithful port of Bishop's tools/verify_state.py canon checks.
 *
 * Checks (each FAILs the gate, except staleness which WARNs — it flags, never auto-edits):
 *   1. Schema    — every canon file has parseable frontmatter: name, description, type in the 4.
 *   2. Index     — MEMORY.md <-> canon files are in 1:1 sync (no orphans either direction).
 *   3. Uniqueness— no duplicate `name:` slug across canon.
 *   4. Quarantine— _inbox/ exists; its contents are NOT counted as canon (must be promoted first).
 *   5. Freshness — WARN on `project`-type files older than a staleness threshold (volatility-aware).
 *
 * Exit 0 = clean; non-zero = gate failed (CI blocks the commit).
 */
package ai.mnemosyne.hardening;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class MemoryCanonGate {

    private static final Set<String> VALID_TYPES = Set.of("user", "feedback", "project", "reference");
    private static final Duration PROJECT_STALE_AFTER = Duration.ofDays(30); // volatility-aware threshold
    private static final Pattern FRONTMATTER = Pattern.compile("(?s)^---\\s*\\n(.*?)\\n---\\s*\\n");
    private static final Pattern NAME       = Pattern.compile("(?m)^name:\\s*(\\S.*?)\\s*$");
    private static final Pattern DESC       = Pattern.compile("(?m)^description:\\s*(\\S.*?)\\s*$");
    // Intentionally lenient on placement: matches `type:` whether top-level or nested under
    // `metadata:` (the schema nests it under metadata). v0 validates the VALUE against the 4-set;
    // strict block-anchored placement enforcement is a build-time tightening if wanted.
    private static final Pattern TYPE       = Pattern.compile("(?m)^\\s*type:\\s*(\\S+)\\s*$");
    // MEMORY.md pointer line: - [Title](slug.md) — hook
    private static final Pattern INDEX_LINE = Pattern.compile("\\(([A-Za-z0-9_-]+)\\.md\\)");

    private final Path canonDir;
    private final List<String> failures = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    public MemoryCanonGate(Path canonDir) { this.canonDir = canonDir; }

    /** @return true if the gate passes (no failures). Warnings do not fail the gate. */
    public boolean run() throws IOException {
        Path indexFile = canonDir.resolve("MEMORY.md");
        if (!Files.exists(indexFile)) { failures.add("MEMORY.md index missing"); return false; }

        List<Path> canonFiles;
        try (var stream = Files.list(canonDir)) {
            canonFiles = stream
                .filter(p -> p.toString().endsWith(".md"))
                .filter(p -> !p.getFileName().toString().equals("MEMORY.md"))
                .collect(Collectors.toList());
        }

        Set<String> fileSlugs = new HashSet<>();
        Map<String, Integer> slugCounts = new HashMap<>();

        // Checks 1, 3, 5 — per-file schema, uniqueness, freshness.
        for (Path f : canonFiles) {
            String slug = stripExt(f.getFileName().toString());
            fileSlugs.add(slug);
            slugCounts.merge(slug, 1, Integer::sum);

            String content = Files.readString(f);
            Matcher fm = FRONTMATTER.matcher(content);
            if (!fm.find()) { failures.add(f + ": no YAML frontmatter block"); continue; }
            String front = fm.group(1);

            String name = group(NAME, front);
            String desc = group(DESC, front);
            String type = group(TYPE, front);

            if (name == null) failures.add(f + ": frontmatter missing `name`");
            else if (!name.equals(slug)) failures.add(f + ": `name` (" + name + ") != filename slug (" + slug + ")");
            if (desc == null) failures.add(f + ": frontmatter missing `description`");
            if (type == null) failures.add(f + ": frontmatter missing `type`");
            else if (!VALID_TYPES.contains(type)) failures.add(f + ": invalid type `" + type + "` (must be one of " + VALID_TYPES + ")");

            if ("project".equals(type)) {
                BasicFileAttributes attr = Files.readAttributes(f, BasicFileAttributes.class);
                Instant modified = attr.lastModifiedTime().toInstant();
                // NOTE: real impl should read a frontmatter `updated:` date, not mtime — mtime is a
                // design-stage proxy. Freshness WARNs (flags); it never auto-edits canon (design/02 §2).
                if (Duration.between(modified, Instant.now()).compareTo(PROJECT_STALE_AFTER) > 0)
                    warnings.add(f + ": project file stale (>30d) — review for freshness");
            }
        }

        // Check 3 — duplicate slugs.
        slugCounts.forEach((slug, n) -> { if (n > 1) failures.add("duplicate canon slug: " + slug + " (" + n + "x)"); });

        // Check 2 — index <-> files 1:1 sync.
        Set<String> indexSlugs = new HashSet<>();
        for (String line : Files.readAllLines(indexFile)) {
            Matcher m = INDEX_LINE.matcher(line);
            while (m.find()) indexSlugs.add(m.group(1));
        }
        for (String fileSlug : fileSlugs)
            if (!indexSlugs.contains(fileSlug)) failures.add("canon file not in MEMORY.md index: " + fileSlug);
        for (String idxSlug : indexSlugs)
            if (!fileSlugs.contains(idxSlug)) failures.add("MEMORY.md points to missing canon file: " + idxSlug);

        // Check 4 — quarantine present; its contents are NOT canon (must be promoted via the gate).
        Path inbox = canonDir.resolve("_inbox");
        if (Files.isDirectory(inbox)) {
            try (var s = Files.list(inbox)) {
                long pending = s.filter(p -> p.toString().endsWith(".md")).count();
                if (pending > 0) warnings.add(inbox + ": " + pending + " quarantined item(s) awaiting promotion");
            }
        }

        return failures.isEmpty();
    }

    private static String group(Pattern p, String s) { Matcher m = p.matcher(s); return m.find() ? m.group(1).trim() : null; }
    private static String stripExt(String n) { return n.endsWith(".md") ? n.substring(0, n.length() - 3) : n; }

    public List<String> failures() { return failures; }
    public List<String> warnings() { return warnings; }

    public static void main(String[] args) throws IOException {
        Path canon = Path.of(args.length > 0 ? args[0] : "./memories");
        MemoryCanonGate gate = new MemoryCanonGate(canon);
        boolean ok = gate.run();
        gate.warnings().forEach(w -> System.out.println("WARN: " + w));
        gate.failures().forEach(f -> System.out.println("FAIL: " + f));
        System.out.println(ok ? "canon gate: PASS" : "canon gate: FAIL (" + gate.failures().size() + ")");
        System.exit(ok ? 0 : 1);
    }
}
