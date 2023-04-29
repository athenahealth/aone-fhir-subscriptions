package com.athenahealth.eventing.partner.service.impl;

import com.athenahealth.eventing.partner.configuration.ApplicationConstants;
import com.athenahealth.eventing.partner.dto.AccessTokenRequestDTO;
import com.athenahealth.eventing.partner.service.HttpService;
import com.athenahealth.eventing.partner.service.IAMService;
import com.athenahealth.eventing.partner.service.PartnerService;
import com.athenahealth.eventing.partner.util.AthenaUtil;
import com.athenahealth.eventing.partner.util.Util;
import lombok.extern.slf4j.Slf4j;
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

    @Value("${com.athenahealth.mdp.host}")
    private String mdpHost;

    @Value("${com.athenahealth.mdp.scopes}")
    private String mdpScope;

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

    @Override
    public void inflatePayload(Bundle bundle) {
        Bundle.BundleEntryComponent bundleEntryComponent = bundle.getEntry().get(0);
        SubscriptionStatus subscriptionStatus = (SubscriptionStatus) bundleEntryComponent.getResource();
        List<SubscriptionStatusNotificationEventComponent> notificationEventComponents = subscriptionStatus.getNotificationEvent();
        for (SubscriptionStatusNotificationEventComponent notificationEventComponent : notificationEventComponents) {
            Reference reference = notificationEventComponent.getFocus();
            Identifier identifier = reference.getIdentifier();
            if (StringUtils.hasText(identifier.getValue())) {
                log.info("{}",identifier.getValue());
                //Fetching token for athena api
                String token = getToken(mdpScope);
                String system = identifier.getSystem();
                List<String> list = Util.extractPatterns(ApplicationConstants.URN_REGEX, system, 2);
                StringJoiner stringJoiner = new StringJoiner("/");
                stringJoiner.add(list.get(1)).add(AthenaUtil.getMdpEndpoint(list.get(0))).add(identifier.getValue());
                //However, we are supporting athena api only but in future fhir support also will be added
                logInflatedPayload(mdpHost , stringJoiner.toString(), token);
            }
        }
    }

    /*
    "For production use at scale, we recommend persisting notifications to a durable queue and inflating/processing asynchronously.
    See Subscriptions framework documentation for more details."
     */
    private void logInflatedPayload(String host, String endpoint, String token) {
        String url = host + endpoint;
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", "Bearer "+ token);
        ResponseEntity<String> response =  httpService.getForEntity(header, url, String.class);
        log.info("Inflated payload {}", response.getBody());
    }

    private String getToken(String scopes) {
        /*Credentials are stored in AWS secret manager, it can be stored anywhere else also but just
        for demonstration we are using secret manager
        */
        AccessTokenRequestDTO accessTokenRequestDTO = AccessTokenRequestDTO.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .scopes(scopes)
                .grantType(grantType)
                .build();
        //getting bearer token to call athenanet API
        return iamService.getAccessToken(accessTokenRequestDTO);
    }
}
