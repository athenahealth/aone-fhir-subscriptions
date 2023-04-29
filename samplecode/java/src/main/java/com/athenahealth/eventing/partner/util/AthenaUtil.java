package com.athenahealth.eventing.partner.util;

import com.athenahealth.eventing.partner.configuration.EntityConfig;
import lombok.experimental.UtilityClass;
import org.springframework.util.CollectionUtils;

@UtilityClass
public class AthenaUtil {

    public static String getMdpEndpoint(String entityType) {
        EntityConfig entityConfig = BeanUtil.getBean(EntityConfig.class);
        if (!CollectionUtils.isEmpty(entityConfig.getEntityType())) {
            return entityConfig.getEntityType().get(entityType);
        }
        return entityType;

    }
}
