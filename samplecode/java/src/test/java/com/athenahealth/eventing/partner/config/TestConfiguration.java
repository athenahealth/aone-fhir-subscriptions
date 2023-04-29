package com.athenahealth.eventing.partner.config;


import com.athenahealth.eventing.partner.configuration.BeanConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;


@Slf4j
@Configuration
@EnableAutoConfiguration
@ComponentScan
@ComponentScan("com.athenahealth.eventing.partner")
@PropertySource("classpath:application.yaml")
@Import(BeanConfiguration.class)
public class TestConfiguration {

}
