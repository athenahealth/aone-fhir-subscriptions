package com.athenahealth.eventing.partner.service.impl;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.athenahealth.eventing.partner.exception.ErrorCode;
import com.athenahealth.eventing.partner.service.PartnerExecutorService;
import com.athenahealth.eventing.partner.service.PartnerService;
import com.athenahealth.eventing.partner.util.ValidationUtils;
import org.hl7.fhir.r4b.model.Bundle;
import org.hl7.fhir.r4b.model.SubscriptionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PartnerExecutorServiceImpl implements PartnerExecutorService {

    @Autowired
    private PartnerService partnerService;

    @Override
    public void inflatePayload(String request) {
        FhirContext fhirContext = FhirContext.forR4B();
        IParser parser = fhirContext.newJsonParser();
        Bundle bundle = parser.parseResource(Bundle.class, request);
        validateBundle(bundle);
        partnerService.inflatePayload(bundle);
    }

    private void validateBundle(Bundle bundle) {
        Bundle.BundleEntryComponent bundleEntryComponent = bundle.getEntry().get(0);
        ValidationUtils.validateNull(bundleEntryComponent, "Invalid bundle payload", ErrorCode.V1);
        SubscriptionStatus subscriptionStatus = (SubscriptionStatus) bundleEntryComponent.getResource();
        ValidationUtils.validateNull(subscriptionStatus, "Invalid bundle resource", ErrorCode.V1);
        List<SubscriptionStatus.SubscriptionStatusNotificationEventComponent> notificationEventComponents = subscriptionStatus.getNotificationEvent();
        ValidationUtils.validateEmpty(notificationEventComponents, "Invalid bundle notification event", ErrorCode.V1);
    }
}
