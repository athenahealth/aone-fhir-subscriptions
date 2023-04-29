package com.athenahealth.eventing.partner.service;

import com.athenahealth.eventing.partner.dto.AccessTokenRequestDTO;

public interface IAMService {
    String getAccessToken(AccessTokenRequestDTO accessTokenRequestDTO);

}
