package be.dnsbelgium.rdap;

import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;

public class RdapMediaType {

  public static final MediaType APPLICATION_RDAP_JSON = new MediaType("application", "rdap+json");
  public static final String APPLICATION_RDAP_JSON_VALUE = APPLICATION_RDAP_JSON.toString();
  public static final MediaType APPLICATION_RDAP_JSON_UTF8 = new MediaType("application", "rdap+json", StandardCharsets.UTF_8);
  public static final String APPLICATION_RDAP_JSON_UTF8_VALUE = APPLICATION_RDAP_JSON_UTF8.toString();

}
