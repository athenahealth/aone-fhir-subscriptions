package com.athenahealth.eventing.partner.service.impl;

import com.athenahealth.eventing.partner.configuration.ApplicationConstants;
import com.athenahealth.eventing.partner.dto.AccessTokenRequestDTO;
import com.athenahealth.eventing.partner.dto.IAMResponse;
import com.athenahealth.eventing.partner.exception.RecordNotFoundException;
import com.athenahealth.eventing.partner.service.HttpService;
import com.athenahealth.eventing.partner.service.IAMService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class IAMServiceImpl implements IAMService {

    @Autowired
    private HttpService httpService;

    @Value("${com.athenahealth.iam.host}")
    private String iamHost;

    @Override
    public String getAccessToken(AccessTokenRequestDTO accessTokenRequestDTO) {
        String secret = accessTokenRequestDTO.getClientId() + ":" + accessTokenRequestDTO.getClientSecret();
        log.info("Fetching token for clientId {}", accessTokenRequestDTO.getClientId());
        String authorizationKey = "Basic " + Base64.getEncoder().encodeToString(secret.getBytes());
        Map<String, String> header = new HashMap<>();
        header.put(ApplicationConstants.AUTHORIZATION_HEADER_KEY, authorizationKey);
        header.put(ApplicationConstants.CONTENT_TYPE_KEY, "application/x-www-form-urlencoded");
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add(ApplicationConstants.GRANT_TYPE_KEY, accessTokenRequestDTO.getGrantType());
        multiValueMap.add(ApplicationConstants.SCOPE_KEY, accessTokenRequestDTO.getScopes());
        ResponseEntity<IAMResponse> response = httpService.postForEntity(header, multiValueMap, iamHost, IAMResponse.class);
        if (Objects.nonNull(response) && Objects.nonNull(response.getBody())) {
            return response.getBody().getAccessToken();
        }
        throw new RecordNotFoundException("Invalid token response");
    }
}
