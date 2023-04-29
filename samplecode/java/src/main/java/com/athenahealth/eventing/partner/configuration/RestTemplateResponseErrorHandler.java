package com.athenahealth.eventing.partner.configuration;

import com.athenahealth.eventing.partner.exception.BaseException;
import com.athenahealth.eventing.partner.exception.RecordNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

@Component
public class RestTemplateResponseErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse httpResponse)
            throws IOException {

        return (
                httpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR
                        || httpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR);
    }

    @Override
    public void handleError(ClientHttpResponse httpResponse)
            throws IOException {
        if (httpResponse.getStatusCode()
                .series() == HttpStatus.Series.SERVER_ERROR) {
            throw new BaseException("Something went wrong");
        }
        if (httpResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new RecordNotFoundException("No record available");
        } else if (httpResponse.getStatusCode()
                .series() == HttpStatus.Series.CLIENT_ERROR) {
            throw new HttpClientErrorException(httpResponse.getStatusCode(), httpResponse.getStatusText());
        }
    }

}

