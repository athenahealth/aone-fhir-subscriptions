package com.athenahealth.eventing.partner.api;

import com.athenahealth.eventing.partner.configuration.ApplicationConstants;
import com.athenahealth.eventing.partner.exception.SignatureAuthenticationException;
import com.athenahealth.eventing.partner.service.PartnerExecutorService;
import com.athenahealth.eventing.partner.util.HmacUtil;
import io.micrometer.core.instrument.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@RestController
@Slf4j
public class PartnerController {

    @Autowired
    private PartnerExecutorService partnerExecutorService;

    @PostMapping(path = "process-event",
        consumes = ApplicationConstants.PAYLOAD_MIME_TYPE)
    public void testController(@RequestBody(required = false) String request,
                               @RequestHeader(required = true, name = ApplicationConstants.X_HUB_SIGNATURE) String signature)  {
        InputStream inputStream = new ByteArrayInputStream(request.getBytes());
        String result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        String expectedSignature = HmacUtil.calculateHmac(result, ApplicationConstants.HMAC_LOCAL_TEST_SECRET);

        if(!HmacUtil.compareHmac(signature, expectedSignature, ApplicationConstants.HMAC_LOCAL_TEST_SECRET)) {
            log.error("Signature Authentication failed.");
            throw new SignatureAuthenticationException();
        } else {
            log.info("Signature Authentication success.");
            log.info("Message Received: " + result);
            partnerExecutorService.inflatePayload(request);
            /*
             * Note: in this example code we are inflating the payload here within the webhook for simplicity.
             * For production use at scale, we recommend that you decouple acknowledgement of events from the
             * inflation and processing step.  Best practice is to keep your webhook as lightweight as possible,
             * ideally just persisting the event payload to a durable queue to be processed asynchronously.  See
             * the README documentation for more details on this topic.
             */
        }

    }

}
