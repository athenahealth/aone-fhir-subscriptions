package com.athenahealth.eventing.partner.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
/*
This is token response object
 */
public class IAMResponse {

    //type of token eg. Bearer
    @JsonProperty("token_type")
    private String tokenType;

    //token expiry time in seconds eg 3600
    @JsonProperty("expires_in")
    private int expiresIn;

    //token which will be used to get inflated payload
    @JsonProperty("access_token")
    private String accessToken;

    //requested scope of token
    private String scope;
}
