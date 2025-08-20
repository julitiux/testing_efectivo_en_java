
package com.circulosiete.curso.testing.clase07.app.http;

import java.net.URI;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class ExternalGreeterClient {
    private final String baseUrl;
    private final RestTemplate restTemplate;

    public ExternalGreeterClient(String baseUrl, RestTemplate restTemplate) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.restTemplate = restTemplate;
    }

    public String greet(String name) {
        URI uri = URI.create(baseUrl + "/greet?name=" + name);
        ResponseEntity<Map> resp = restTemplate.getForEntity(uri, Map.class);
        return (String) resp.getBody().get("message");
    }
}
