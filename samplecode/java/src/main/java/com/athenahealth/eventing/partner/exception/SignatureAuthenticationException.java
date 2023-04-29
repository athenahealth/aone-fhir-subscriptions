package com.athenahealth.eventing.partner.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class SignatureAuthenticationException extends BaseException  {

    public SignatureAuthenticationException() {
        super("Signature authentication failed");
    }

}
