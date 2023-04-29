package com.athenahealth.eventing.partner.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "com.athenahealth.mdp")
@Setter
@Getter
public class EntityConfig {

    private String host;

    private Map<String, String> entityType;
}
