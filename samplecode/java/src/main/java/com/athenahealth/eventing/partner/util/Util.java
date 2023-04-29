package com.athenahealth.eventing.partner.util;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class Util {

    public static List<String> extractPatterns(String pattern, String value, int noOfOutput) {
        List<String> extractedValue = new ArrayList<>();
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(value);
        if (m.find()) {
            int i = 1;
            while (i <= noOfOutput) {
                extractedValue.add(m.group(i));
                i++;
            }
        }
        return extractedValue;
    }

}
