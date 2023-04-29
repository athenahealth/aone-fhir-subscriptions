package com.athenahealth.eventing.partner.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class RecordNotFoundException extends BaseException {

    private static final long serialVersionUID = 1L;

    public RecordNotFoundException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public RecordNotFoundException(String message) {
        super(message);
    }
}
