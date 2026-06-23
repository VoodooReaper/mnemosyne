# Reference Dependencies (DESIGN-STAGE — confirm versions at build time)

> Versions are from research v2 (milestone / SNAPSHOT). Community modules are fluid. Confirm at build.

## Spring AI BOM

```xml
<!-- DESIGN-STAGE: pin to the milestone confirmed at build time -->
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.springframework.ai</groupId>
      <artifactId>spring-ai-bom</artifactId>
      <version>2.0.0-M2</version> <!-- confirm; SNAPSHOT for community modules -->
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

## Starters by layer

```xml
<!-- Layer A: file-canon memory tools + advisor (community module) -->
<dependency>
  <groupId>org.springaicommunity</groupId>      <!-- confirm groupId at build -->
  <artifactId>spring-ai-agent-utils</artifactId> <!-- AutoMemoryTools + AutoAutoMemoryToolsAdvisor -->
</dependency>

<!-- Layer B: MongoDB Atlas Vector Search -->
<dependency>
  <groupId>org.springframework.ai</groupId>
  <artifactId>spring-ai-starter-vector-store-mongodb-atlas</artifactId>
</dependency>

<!-- Token lever: dynamic tool discovery -->
<dependency>
  <groupId>org.springframework.ai</groupId>
  <artifactId>spring-ai-starter-tool-search-advisor</artifactId>
</dependency>

<!-- Chat + embedding backend: ONE of these, per the policy decision in design/03 -->
<!-- in-tenant (AWS): -->
<dependency>
  <groupId>org.springframework.ai</groupId>
  <artifactId>spring-ai-starter-model-bedrock-converse</artifactId>
</dependency>
<!-- OR governed channel (Anthropic): -->
<!-- <artifactId>spring-ai-starter-model-anthropic</artifactId> -->
<!-- OR in-tenant (GCP, Gemini-native; Claude-on-Vertex routing Unverified): -->
<!-- <artifactId>spring-ai-starter-model-vertex-ai-gemini</artifactId> -->
```

## Application properties (illustrative)

```properties
# Tool Search (token lever, design/04)
spring.ai.chat.client.tool-search-advisor.enabled=true

# Mongo Atlas Vector (Layer B, design/01)
# spring.ai.vectorstore.mongodb.* — collection, index, path, numCandidates (confirm keys at build)

# Backend (design/03) — exactly one rail's properties, confirm-at-work
# bedrock: region + IAM via standard AWS credential chain
```
