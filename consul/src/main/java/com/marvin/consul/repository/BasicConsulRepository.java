package com.marvin.consul.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marvin.consul.model.ConsulKeyValueDTO;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class BasicConsulRepository {

    private static final TypeReference<List<ConsulKeyValueDTO>> TYPE_REFERENCE = new TypeReference<>() {
    };

    private final String url;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BasicConsulRepository(String url, ObjectMapper objectMapper) {
        this.url = url;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
    }

    public List<ConsulKeyValueDTO> getPropertiesRecursively(String keyPrefix) throws Exception {

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(new URI(url + "/v1/kv/" + keyPrefix + "?recurse=true"))
                .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        String responseBody = httpResponse.body();

        return StringUtils.isBlank(responseBody)
                ? Collections.emptyList()
                : objectMapper.readValue(responseBody, TYPE_REFERENCE);
    }
}
