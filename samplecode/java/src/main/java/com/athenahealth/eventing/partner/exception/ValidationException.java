package com.athenahealth.eventing.partner.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ValidationException extends BaseException {

    public ValidationException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

}
