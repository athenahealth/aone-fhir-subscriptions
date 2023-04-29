package com.athenahealth.eventing.partner.util;

import com.athenahealth.eventing.partner.exception.ErrorCode;
import com.athenahealth.eventing.partner.exception.ValidationException;
import lombok.experimental.UtilityClass;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Objects;

@UtilityClass
public class ValidationUtils {

    public static void validateEmptyString(String value, String errorMessage, ErrorCode code){
        if(!StringUtils.hasText(value)){
            throw new ValidationException(errorMessage, code);
        }
    }

    public static void validateNull(Object value, String errorMessage, ErrorCode code){
        if(Objects.isNull(value)){
            throw new ValidationException(errorMessage, code);
        }
    }

    public static <T> void validateEmpty(Collection<T> value, String errorMessage, ErrorCode code){
        if(CollectionUtils.isEmpty(value)){
            throw new ValidationException(errorMessage, code);
        }
    }

    public static void validateTrue(boolean value, String errorMessage, ErrorCode code){
        if(!value){
            throw new ValidationException(errorMessage, code);
        }
    }
}
