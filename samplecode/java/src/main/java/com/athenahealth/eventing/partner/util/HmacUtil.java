package com.athenahealth.eventing.partner.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.util.Base64;

@Slf4j
@UtilityClass
public class HmacUtil {
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    public static String calculateHmac(String data, String secret) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), HMAC_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(secretKey);
            byte[] hmacData = mac.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(hmacData);
        } catch (Exception ex) {
            log.error("Error while generating Hmac {}", ex);
        }
        return null;
    }

    public static boolean compareHmac(String message1, String message2, String secret)  {
        try {
            Mac algo = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), HMAC_ALGORITHM);
            algo.init(secretKey);

            byte[] hash1 = algo.doFinal(message1.getBytes());
            byte[] hash2 = algo.doFinal(message2.getBytes());

            return Arrays.equals(hash1, hash2);
        } catch (Exception ex) {
            log.error("Error while comparing Hmac {}", ex);
        }
        return false;
    }
}
