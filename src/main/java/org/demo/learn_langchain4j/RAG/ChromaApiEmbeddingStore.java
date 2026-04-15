package org.demo.learn_langchain4j.RAG;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChromaApiEmbeddingStore implements EmbeddingStore<TextSegment> {

    private static final Logger log = LoggerFactory.getLogger(ChromaApiEmbeddingStore.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String collectionName;
    private final int maxRetries;
    private final long retryDelayMs;

    private volatile String collectionId;

    public ChromaApiEmbeddingStore(String baseUrl, String collectionName, int maxRetries, long retryDelayMs) {
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        this.objectMapper = new ObjectMapper();
        this.baseUrl = normalizeBaseUrl(baseUrl);
        this.collectionName = collectionName;
        this.maxRetries = Math.max(1, maxRetries);
        this.retryDelayMs = Math.max(50L, retryDelayMs);
        this.collectionId = ensureCollectionReady();
    }

    @Override
    public String add(Embedding embedding) {
        String id = UUID.randomUUID().toString();
        addAll(Collections.singletonList(id), Collections.singletonList(embedding), null);
        return id;
    }

    @Override
    public void add(String id, Embedding embedding) {
        addAll(Collections.singletonList(id), Collections.singletonList(embedding), null);
    }

    @Override
    public String add(Embedding embedding, TextSegment embedded) {
        String id = UUID.randomUUID().toString();
        addAll(Collections.singletonList(id), Collections.singletonList(embedding), Collections.singletonList(embedded));
        return id;
    }

    @Override
    public List<String> addAll(List<Embedding> embeddings) {
        List<String> ids = generateIds(embeddings.size());
        addAll(ids, embeddings, null);
        return ids;
    }

    @Override
    public void addAll(List<String> ids, List<Embedding> embeddings, List<TextSegment> embedded) {
        if (ids == null || embeddings == null || ids.size() != embeddings.size()) {
            throw new IllegalArgumentException("ids and embeddings must be non-null and have same size");
        }

        List<List<Float>> vectors = new ArrayList<>(embeddings.size());
        for (Embedding embedding : embeddings) {
            vectors.add(embedding.vectorAsList());
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ids", ids);
        body.put("embeddings", vectors);

        if (embedded != null) {
            List<String> documents = new ArrayList<>(embedded.size());
            List<Map<String, Object>> metadatas = new ArrayList<>(embedded.size());
            for (TextSegment segment : embedded) {
                if (segment == null) {
                    documents.add("");
                    metadatas.add(Collections.emptyMap());
                } else {
                    documents.add(segment.text());
                    metadatas.add(segment.metadata() == null ? Collections.emptyMap() : segment.metadata().toMap());
                }
            }
            body.put("documents", documents);
            body.put("metadatas", metadatas);
        }

        String response = execute("POST", "/api/v1/collections/" + ensureCollectionReady() + "/add", body, 200);
        if (!"true".equalsIgnoreCase(response.trim())) {
            throw new RuntimeException("Chroma add embeddings failed: " + response);
        }
    }

    @Override
    public EmbeddingSearchResult<TextSegment> search(EmbeddingSearchRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("query_embeddings", Collections.singletonList(request.queryEmbedding().vectorAsList()));
        body.put("n_results", request.maxResults());
        body.put("include", List.of("documents", "metadatas", "embeddings", "distances"));

        String raw = execute("POST", "/api/v1/collections/" + ensureCollectionReady() + "/query", body, 200);
        JsonNode root;
        try {
            root = objectMapper.readTree(raw);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse Chroma query response", e);
        }

        List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();
        JsonNode idsNode = firstArray(root.path("ids"));
        JsonNode distancesNode = firstArray(root.path("distances"));
        JsonNode docsNode = firstArray(root.path("documents"));
        JsonNode metadataNode = firstArray(root.path("metadatas"));
        JsonNode embeddingNode = firstArray(root.path("embeddings"));

        int size = idsNode == null ? 0 : idsNode.size();
        for (int i = 0; i < size; i++) {
            String id = idsNode.path(i).asText();
            double distance = distancesNode != null && distancesNode.size() > i ? distancesNode.path(i).asDouble(1d) : 1d;
            double score = distanceToScore(distance);
            if (score < request.minScore()) {
                continue;
            }

            String document = docsNode != null && docsNode.size() > i ? docsNode.path(i).asText("") : "";
            Map<String, Object> metadata = metadataNode != null && metadataNode.size() > i
                    ? objectMapper.convertValue(metadataNode.path(i), new TypeReference<Map<String, Object>>() {})
                    : Collections.emptyMap();
            TextSegment segment = TextSegment.from(document, Metadata.from(metadata));

            Embedding embedding = null;
            if (embeddingNode != null && embeddingNode.size() > i && embeddingNode.path(i).isArray()) {
                List<Float> vector = new ArrayList<>(embeddingNode.path(i).size());
                for (JsonNode value : embeddingNode.path(i)) {
                    vector.add((float) value.asDouble());
                }
                embedding = Embedding.from(vector);
            }

            matches.add(new EmbeddingMatch<>(score, id, embedding, segment));
        }

        return new EmbeddingSearchResult<>(matches);
    }

    @Override
    public void removeAll(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ids", ids);
        execute("POST", "/api/v1/collections/" + ensureCollectionReady() + "/delete", body, 200);
    }

    @Override
    public void removeAll(Filter filter) {
        throw new UnsupportedOperationException("Filter-based deletion is not implemented for ChromaApiEmbeddingStore");
    }

    @Override
    public void removeAll() {
        int status = executeForStatus("DELETE", "/api/v1/collections/" + encode(collectionName), null);
        if (status < 200 || status >= 300) {
            throw new RuntimeException("Failed to delete collection '" + collectionName + "', status=" + status);
        }
        this.collectionId = ensureCollectionReady();
    }

    private String ensureCollectionReady() {
        if (collectionId != null && !collectionId.isBlank()) {
            return collectionId;
        }

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            String found = fetchCollectionId();
            if (found != null) {
                collectionId = found;
                if (attempt > 1) {
                    log.info("Reused Chroma collection '{}' on retry {}", collectionName, attempt);
                }
                return collectionId;
            }

            String created = createCollection();
            if (created != null) {
                collectionId = created;
                log.info("Created Chroma collection '{}'", collectionName);
                return collectionId;
            }

            sleep(attempt);
        }

        throw new RuntimeException("Unable to initialize Chroma collection '" + collectionName + "' after " + maxRetries + " attempts");
    }

    private String fetchCollectionId() {
        HttpResponse<String> response = executeHttp("GET", "/api/v1/collections/" + encode(collectionName), null);
        int status = response.statusCode();
        if (status == 404) {
            return null;
        }
        if (status < 200 || status >= 300) {
            throw new RuntimeException("Chroma fetch collection failed, status=" + status + ", body=" + safeBody(response));
        }
        return parseCollectionId(response.body());
    }

    private String createCollection() {
        Map<String, Object> body = Map.of("name", collectionName);
        HttpResponse<String> response = executeHttp("POST", "/api/v1/collections", body);
        int status = response.statusCode();
        if (status == 409) {
            return null;
        }
        if (status < 200 || status >= 300) {
            throw new RuntimeException("Chroma create collection failed, status=" + status + ", body=" + safeBody(response));
        }
        return parseCollectionId(response.body());
    }

    private String parseCollectionId(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            String id = root.path("id").asText(null);
            if (id == null || id.isBlank()) {
                throw new RuntimeException("Missing collection id in Chroma response: " + body);
            }
            return id;
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse Chroma collection response", e);
        }
    }

    private String execute(String method, String path, Object body, int expectedStatus) {
        HttpResponse<String> response = executeHttp(method, path, body);
        int status = response.statusCode();
        if (status != expectedStatus) {
            throw new RuntimeException("Chroma request failed, status=" + status + ", body=" + safeBody(response));
        }
        return safeBody(response);
    }

    private int executeForStatus(String method, String path, Object body) {
        return executeHttp(method, path, body).statusCode();
    }

    private HttpResponse<String> executeHttp(String method, String path, Object body) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json");

            if (body == null) {
                if ("GET".equals(method) || "DELETE".equals(method)) {
                    builder.method(method, HttpRequest.BodyPublishers.noBody());
                } else {
                    builder.method(method, HttpRequest.BodyPublishers.ofString("{}"));
                }
            } else {
                builder.method(method, HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
            }

            return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to call Chroma API: " + method + " " + path, e);
        }
    }

    private JsonNode firstArray(JsonNode node) {
        if (node == null || !node.isArray() || node.isEmpty()) {
            return null;
        }
        return node.get(0);
    }

    private double distanceToScore(double distance) {
        if (distance < 0d) {
            return 1d;
        }
        return 1d / (1d + distance);
    }

    private void sleep(int attempt) {
        try {
            Thread.sleep(retryDelayMs * attempt);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while retrying Chroma collection init", e);
        }
    }

    private String safeBody(HttpResponse<String> response) {
        return response.body() == null ? "" : response.body();
    }

    private String normalizeBaseUrl(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}

