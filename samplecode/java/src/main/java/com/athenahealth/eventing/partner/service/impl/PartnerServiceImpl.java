package com.athenahealth.eventing.partner.service.impl;

import com.athenahealth.eventing.partner.configuration.ApplicationConstants;
import com.athenahealth.eventing.partner.dto.AccessTokenRequestDTO;
import com.athenahealth.eventing.partner.service.HttpService;
import com.athenahealth.eventing.partner.service.IAMService;
import com.athenahealth.eventing.partner.service.PartnerService;
import com.athenahealth.eventing.partner.util.AthenaUtil;
import com.athenahealth.eventing.partner.util.Util;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.text.StringSubstitutor;
import org.hl7.fhir.r4b.model.Bundle;
import org.hl7.fhir.r4b.model.Identifier;
import org.hl7.fhir.r4b.model.Reference;
import org.hl7.fhir.r4b.model.SubscriptionStatus;
import org.hl7.fhir.r4b.model.SubscriptionStatus.SubscriptionStatusNotificationEventComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Service
@Slf4j
public class PartnerServiceImpl implements PartnerService {

    @Value("${com.athenahealth.api.baseUrl}")
    private String baseUrl;

    @Value("${com.athenahealth.api.fhirBasePath}")
    private String fhirBasePath;

    @Value("${com.athenahealth.api.scopes}")
    private String scopes;

    @Value("${com.athenahealth.credentials.clientId}")
    private String clientId;

    @Value("${com.athenahealth.credentials.clientSecret}")
    private String clientSecret;

    @Value("${com.athenahealth.credentials.grantType}")
    private String grantType;

    @Autowired
    private HttpService httpService;

    @Autowired
    private IAMService iamService;

    // Cache token for reuse
    private String token;

    @Override
    public void inflatePayload(Bundle bundle) {
        Bundle.BundleEntryComponent bundleEntryComponent = bundle.getEntry().get(0);
        SubscriptionStatus subscriptionStatus = (SubscriptionStatus) bundleEntryComponent.getResource();
        List<SubscriptionStatusNotificationEventComponent> notificationEventComponents = subscriptionStatus.getNotificationEvent();

        for (SubscriptionStatusNotificationEventComponent notificationEventComponent : notificationEventComponents) {
            Reference focusResource = notificationEventComponent.getFocus();
            String endpoint = "";

            if (focusResource.getReference() != null) {
                // Use FHIR reference if available
                endpoint = fhirBasePath + "/" + focusResource.getReference();
            }
            else if (focusResource.getIdentifier() != null) {
                // Else use proprietary API endpoint
                Identifier identifier = focusResource.getIdentifier();
                String system = identifier.getSystem();
                List<String> list = Util.extractPatterns(ApplicationConstants.URN_REGEX, system, 2);
                String entityType = list.get(0);

                Map<String, String> pathParams = new HashMap<>();
                pathParams.put("practiceid", list.get(1));
                pathParams.put("id", identifier.getValue());
                if (notificationEventComponent.getAdditionalContext() != null) {
                    for (Reference relatedResource : notificationEventComponent.getAdditionalContext()) {
                        if ("Patient".equals(relatedResource.getType())) {
                            pathParams.put("patientid", relatedResource.getIdentifier().getValue());
                        }
                    }
                }

                String template = AthenaUtil.getMdpEndpoint(entityType);
                endpoint = StringSubstitutor.replace(template, pathParams, "{", "}");
            }

            log.info("Calling back to endpoint {}", endpoint);
            logInflatedPayload(baseUrl, endpoint, getToken());
        }
    }
    /*
      For production use at scale, we recommend persisting notifications to a durable queue and inflating/processing asynchronously.
      See Subscriptions framework documentation for more details.
     */
    private void logInflatedPayload(String host, String endpoint, String token) {
        String url = host + endpoint;
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", "Bearer "+ token);
        ResponseEntity<String> response =  httpService.getForEntity(header, url, String.class);
        log.info("Inflated payload {}", response.getBody());
    }

    private String getToken() {
        // TODO: refresh token only if expired
        return getToken(true);
    }

    private String getToken(boolean forceRefresh) {
        if (token == null || forceRefresh) {
            AccessTokenRequestDTO accessTokenRequestDTO = AccessTokenRequestDTO.builder()
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .scopes(scopes)
                    .grantType(grantType)
                    .build();
            //getting bearer token to call athenanet API
            token = iamService.getAccessToken(accessTokenRequestDTO);
        }
        return token;
    }
}
