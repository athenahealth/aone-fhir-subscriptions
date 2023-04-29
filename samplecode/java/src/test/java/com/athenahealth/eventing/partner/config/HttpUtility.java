package com.athenahealth.eventing.partner.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class HttpUtility {

    public final String baseUrl = "http://localhost:";
    @Autowired
    private TestRestTemplate restTemplate;

    public <T> ResponseEntity<T> postForEntity(String url, String requestBody, Class<T> response) throws URISyntaxException {
        URI uri = new URI(url);
        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", "application/json;charset=UTF-8");
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        return restTemplate.postForEntity(uri, request, response);
    }

    public <T> ResponseEntity<T> postForEntity(HttpHeaders headers, int port, String endpoint, String requestBody, Class<T> response) throws Exception {
        String url = baseUrl + port + endpoint;
        URI uri = new URI(url);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        return restTemplate.postForEntity(uri, request, response);
    }

    public <T> ResponseEntity<T> putForEntity(int port, String endpoint, String requestBody, Class<T> response) throws Exception {
        String endPoint = baseUrl + port + endpoint;
        URI uri = new URI(endPoint);
        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", "application/json;charset=UTF-8");
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        return restTemplate.exchange(uri, HttpMethod.PUT, request, response);
    }

    public <T> ResponseEntity<T> getForEntity(int port, String endpoint, Class<T> response) throws Exception {
        String endPoint = baseUrl + port + endpoint;
        URI uri = new URI(endPoint);
        return restTemplate.getForEntity(uri, response);
    }

    public <T> ResponseEntity<T> getForEntity(int port, String endpoint, ParameterizedTypeReference<T> type) throws Exception {
        String endPoint = baseUrl + port + endpoint;
        URI uri = new URI(endPoint);
        return restTemplate.exchange(uri, HttpMethod.GET, null, type);
    }

    public <T> ResponseEntity<T> getForEntity(HttpHeaders headers, String url, Class<T> response) throws Exception {
        URI uri = new URI(url);
        HttpEntity<String> request = new HttpEntity<>(null, headers);
        return restTemplate.exchange(uri, HttpMethod.GET, request, response);
    }

    public <T> ResponseEntity<T> postForEntity(int port, String endpoint, String requestBody, ParameterizedTypeReference<T> type) throws Exception {
        String endPoint = baseUrl + port + endpoint;
        URI uri = new URI(endPoint);
        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", "application/json;charset=UTF-8");
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        return restTemplate.exchange(uri, HttpMethod.POST, request, type);
    }

}
