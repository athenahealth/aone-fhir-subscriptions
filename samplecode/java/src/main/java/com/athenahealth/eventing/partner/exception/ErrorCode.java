package com.athenahealth.eventing.partner.exception;

public enum ErrorCode {
    V1("Invalid bundle message");


    private final String value;

    ErrorCode(String value) {
        this.value = value;
    }

}
