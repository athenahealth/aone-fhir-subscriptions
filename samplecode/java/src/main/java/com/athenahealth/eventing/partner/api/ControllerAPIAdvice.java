package com.athenahealth.eventing.partner.api;

import com.athenahealth.eventing.partner.dto.ErrorMessage;
import com.athenahealth.eventing.partner.exception.BaseException;
import com.athenahealth.eventing.partner.exception.RecordNotFoundException;
import com.athenahealth.eventing.partner.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
@Slf4j
public class ControllerAPIAdvice {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorMessage> validationException(ValidationException ex, WebRequest request) {
        ErrorMessage errorMessage = getErrorMessage(ex);
        return new ResponseEntity<>(errorMessage, HttpStatus.valueOf(HttpStatus.BAD_REQUEST.value()));

    }

    @ExceptionHandler(RecordNotFoundException.class)
    public ResponseEntity<ErrorMessage> recordNotFoundException(RecordNotFoundException ex, WebRequest request) {
        ErrorMessage errorMessage = getErrorMessage(ex);
        return new ResponseEntity<>(errorMessage, HttpStatus.valueOf(HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorMessage> clientException(HttpClientErrorException ex, WebRequest request) {
        log.error("error : ", ex);
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setMessage(ex.getMessage());
        errorMessage.setCode(ex.getRawStatusCode());
        return new ResponseEntity<>(errorMessage, ex.getStatusCode());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> exception(Exception ex, WebRequest request) {
        log.error("error : ", ex);
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setMessage(ex.getMessage());
        errorMessage.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(errorMessage,HttpStatus.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));

    }

    private ErrorMessage getErrorMessage(BaseException ex) {
        log.error("error : ", ex);
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setMessage(ex.getMessage());
        errorMessage.setErrorCode(ex.getErrorCode());
        return errorMessage;
    }
}
