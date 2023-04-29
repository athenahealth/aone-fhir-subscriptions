package com.athenahealth.eventing.partner.configuration;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ApplicationConstants {

  public static final String AUTHORIZATION_HEADER_KEY = "Authorization";
  public static final String X_HUB_SIGNATURE = "X-Hub-Signature";
  public static final String HMAC_LOCAL_TEST_SECRET = "SecretHashKeyForConsumer1";
  public static final String URN_REGEX = "^urn:athenahealth:athenanet:(\\w+):(\\d+)$";
  public static final String CLIENT_ID_KEY = "client-id";
  public static final String CLIENT_SECRET_KEY = "client-secret";
  public static final String GRANT_TYPE_KEY = "grant_type";
  public static final String SCOPE_KEY = "scope";
  public static final String CONTENT_TYPE_KEY = "Content-Type";
  public static final String PAYLOAD_MIME_TYPE = "application/fhir+json";


}
