package com.athenahealth.eventing.partner.api;

import com.athenahealth.eventing.partner.config.FileUtils;
import com.athenahealth.eventing.partner.config.HttpUtility;
import com.athenahealth.eventing.partner.config.IntegrationTest;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@IntegrationTest
@ExtendWith(SpringExtension.class)
class PartnerControllerTest {

    @Autowired
    private HttpUtility httpUtility;

    @LocalServerPort
    private int port;

    @Test
    void partnerWebhookTest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", "text/plain");
        ResponseEntity<String> result = httpUtility.postForEntity(headers, port,"/test", FileUtils.getResourceFileContentAsString("mock/bundle.json"), String.class);
        Assert.assertEquals(200, result.getStatusCodeValue());

    }
}
