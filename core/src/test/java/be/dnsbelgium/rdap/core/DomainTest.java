package be.dnsbelgium.rdap.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import java.util.List;

import static be.dnsbelgium.rdap.core.Common.DEFAULT_RDAP_CONFORMANCE;
import static be.dnsbelgium.rdap.core.Common.REDACTED_EXTENSION_CONFORMANCE;
import static be.dnsbelgium.rdap.jackson.TestObjectMapper.assertJsonMapping;

public class DomainTest {

  @Test
  public void testUnredactedDomainSample() throws JsonProcessingException {
    String sample = """
        {
          "rdapConformance" : [ "rdap_level_0" ],
          "objectClassName" : "domain",
          "handle" : "abc-123"
        }""";

    Domain domain = new Domain(null, null, null, null, null, null, null,
        "abc-123", null, null, null, null, null, null,
        null, null);
    domain.addRdapConformance(DEFAULT_RDAP_CONFORMANCE);

    assertJsonMapping(domain, sample);
  }

  @Test
  public void testRedactedDomainSample() throws JsonProcessingException {
    String sample = """
        {
          "rdapConformance" : [ "rdap_level_0", "redacted" ],
          "objectClassName" : "domain",
          "redacted" : [ {
            "name" : {
              "type" : "Registry Domain ID"
            }
          } ]
        }""";

    Redacted redacted = new Redacted(Redacted.Name.fromType(Redacted.Name.Type.Default.REGISTRY_DOMAIN_ID));
    Domain domain = new Domain(null, null, null, null, null, null, null,
        null, null, null, null, null, null, null,
        null, null, List.of(redacted));
    domain.addRdapConformance(DEFAULT_RDAP_CONFORMANCE);
    domain.addRdapConformance(REDACTED_EXTENSION_CONFORMANCE);

    assertJsonMapping(domain, sample);
  }

}