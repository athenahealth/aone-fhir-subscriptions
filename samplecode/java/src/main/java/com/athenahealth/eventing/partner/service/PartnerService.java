package com.athenahealth.eventing.partner.service;

import org.hl7.fhir.r4b.model.Bundle;

public interface PartnerService {

    void inflatePayload(Bundle bundle);
}
