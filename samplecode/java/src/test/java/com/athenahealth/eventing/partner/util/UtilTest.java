package com.athenahealth.eventing.partner.util;

import com.athenahealth.eventing.partner.configuration.ApplicationConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class UtilTest {

    @Test
    void extractPatterTest() {
       List<String> list = Util.extractPatterns(ApplicationConstants.URN_REGEX, "urn:athenahealth:athenanet:appointment:432", 2);
        Assertions.assertEquals("432", list.get(1));
        Assertions.assertEquals("appointment", list.get(0));
    }
}
