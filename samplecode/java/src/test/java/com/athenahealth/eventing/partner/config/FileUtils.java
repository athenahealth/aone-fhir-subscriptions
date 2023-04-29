package com.athenahealth.eventing.partner.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class FileUtils {

    private FileUtils(){}

    public static String getResourceFileContentAsString(final String fileName) {
        String data = "";
        final ClassPathResource cpr = new ClassPathResource(fileName);
        try {
            final byte[] bdata = FileCopyUtils.copyToByteArray(cpr.getInputStream());
            data = new String(bdata, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            log.error("Error :", e);
        }
        return data;
    }
}
