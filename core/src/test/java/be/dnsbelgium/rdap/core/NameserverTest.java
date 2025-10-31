package be.dnsbelgium.rdap.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import java.util.List;

import static be.dnsbelgium.rdap.core.Common.DEFAULT_RDAP_CONFORMANCE;
import static be.dnsbelgium.rdap.core.Common.REDACTED_EXTENSION_CONFORMANCE;
import static be.dnsbelgium.rdap.jackson.TestObjectMapper.assertJsonMapping;

public class NameserverTest {

  @Test
  public void testUnredactedNameserverSample() throws JsonProcessingException {
    String sample = """
        {
          "rdapConformance" : [ "rdap_level_0" ],
          "objectClassName" : "nameserver",
          "handle" : "abc-123"
        }""";

    Nameserver nameserver = new Nameserver(null, null, null, null, null,
        null, null, "abc-123", null, null, null);
    nameserver.addRdapConformance(DEFAULT_RDAP_CONFORMANCE);

    assertJsonMapping(nameserver, sample);
  }

  @Test
  public void testRedactedNameserverSample() throws JsonProcessingException {
    String sample = """
        {
          "rdapConformance" : [ "rdap_level_0", "redacted" ],
          "objectClassName" : "nameserver",
          "redacted" : [ {
            "name" : {
              "description" : "Nameserver handle"
            }
          } ]
        }""";

    Redacted redacted = new Redacted(Redacted.Name.fromDescription("Nameserver handle"));
    Nameserver nameserver = new Nameserver(null, null, null, null, null,
        null, null, null, null, null, null, List.of(redacted));
    nameserver.addRdapConformance(DEFAULT_RDAP_CONFORMANCE);
    nameserver.addRdapConformance(REDACTED_EXTENSION_CONFORMANCE);

    assertJsonMapping(nameserver, sample);
  }

}