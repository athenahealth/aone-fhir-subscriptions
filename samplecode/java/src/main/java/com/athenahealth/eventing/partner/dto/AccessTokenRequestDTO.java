package com.athenahealth.eventing.partner.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class AccessTokenRequestDTO {

    private String clientId;

    private String clientSecret;

    private String grantType;

    private String scopes;

}
