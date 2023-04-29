package com.athenahealth.eventing.partner.dto;

import com.athenahealth.eventing.partner.exception.ErrorCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ErrorMessage {

    private String status ="FAILURE" ;

    private String message;

    private int code;

    private ErrorCode errorCode;
}
