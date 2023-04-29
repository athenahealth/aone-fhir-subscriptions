package com.athenahealth.eventing.partner.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ClientException extends BaseException {

    private static final long serialVersionUID = 1L;


    public ClientException(String message) {
        super(message);
    }
}
