package com.athenahealth.eventing.partner.exception;

public class BaseException extends RuntimeException {

    private final ErrorCode errorCode;

    public BaseException(String theMessage, ErrorCode errorCode) {
        super(theMessage);
        this.errorCode = errorCode;
    }

    public BaseException(String theMessage) {
        super(theMessage);
        errorCode = null;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
