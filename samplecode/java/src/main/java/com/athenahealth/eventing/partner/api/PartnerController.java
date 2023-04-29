package com.athenahealth.eventing.partner.api;

import com.athenahealth.eventing.partner.configuration.ApplicationConstants;
import com.athenahealth.eventing.partner.service.PartnerExecutorService;
import com.athenahealth.eventing.partner.util.HmacUtil;
import io.micrometer.core.instrument.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@Slf4j
public class PartnerController {

    @Autowired
    private PartnerExecutorService partnerExecutorService;

    @PostMapping(path = "{test}",
            consumes = MediaType.TEXT_PLAIN_VALUE)
    public void testController(@PathVariable("test") String test,
                               @RequestBody(required = false) String request,
                               @RequestHeader Map<String, String> headers)  {
        InputStream inputStream = new ByteArrayInputStream(request.getBytes());
        String result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        if (headers.containsKey(ApplicationConstants.X_HUB_SECRET)) {
            String xhubSecret = HmacUtil.calculateHmac(result, ApplicationConstants.HMAC_LOCAL_TEST_SECRET);
            if(!HmacUtil.compareHmac(headers.get(ApplicationConstants.X_HUB_SECRET),
                    xhubSecret, ApplicationConstants.HMAC_LOCAL_TEST_SECRET)) {
                log.error("Signature Authentication failed.");
            }else {
                log.info("Signature Authentication success.");
                partnerExecutorService.inflatePayload(request);
            }
        } else {
            log.warn("x-hub-secret header was not present, for signature based authentication.");
        }
        log.info("header = " + headers);
        log.info("Message Received: " + result);
    }

}
