package com.athenahealth.eventing.partner.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class HttpService {

    @Autowired
    private RestTemplate restTemplate;

    public <T> ResponseEntity<T> getForEntity(Map<String, String> headerMap, String url, Class<T> response)  {
        HttpHeaders headers = new HttpHeaders();
        headerMap.forEach(headers::set);
        HttpEntity<String> request = new HttpEntity<>(null, headers);
        return restTemplate.exchange(url, HttpMethod.GET, request, response);
    }

    public <T> ResponseEntity<T> postForEntity(Map<String, String> headerMap, MultiValueMap<String, String> map,
                                              String url, Class<T> response)  {
        HttpHeaders headers = new HttpHeaders();
        headerMap.forEach(headers::set);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        return restTemplate.exchange(url, HttpMethod.POST, request, response);
    }

    public <T> ResponseEntity<T> postForEntity(Map<String, String> headerMap, String url, MultiValueMap<String, String> map, Class<T> response) {
        HttpHeaders headers = new HttpHeaders();
        headerMap.forEach(headers::set);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        return restTemplate.postForEntity(url, request, response);
    }


}
