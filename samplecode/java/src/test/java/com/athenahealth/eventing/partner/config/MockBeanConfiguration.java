package com.athenahealth.eventing.partner.config;

import com.athenahealth.eventing.partner.service.HttpService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MockBeanConfiguration {

    @Bean
    @Primary
    public HttpService mockHttpService() {
        return Mockito.mock(HttpService.class);
    }

}
