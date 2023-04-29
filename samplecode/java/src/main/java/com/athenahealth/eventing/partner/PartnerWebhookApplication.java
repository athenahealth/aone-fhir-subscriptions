package com.athenahealth.eventing.partner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;

import javax.annotation.PreDestroy;

@SpringBootApplication
@ComponentScan(basePackageClasses = PartnerWebhookApplication.class)
@EnableConfigurationProperties
@Slf4j
@Profile("!test")
public class PartnerWebhookApplication {

  public static void main(final String[] args) {
    SpringApplication.run(PartnerWebhookApplication.class, args);
  }

  @PreDestroy
  public void onExit() {
    log.info( "Service Terminated");
  }

}
