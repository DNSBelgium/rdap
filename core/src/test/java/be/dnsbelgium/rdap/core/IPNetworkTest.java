package be.dnsbelgium.rdap.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import java.util.List;

import static be.dnsbelgium.rdap.core.Common.DEFAULT_RDAP_CONFORMANCE;
import static be.dnsbelgium.rdap.core.Common.REDACTED_EXTENSION_CONFORMANCE;
import static be.dnsbelgium.rdap.jackson.TestObjectMapper.assertJsonMapping;

public class IPNetworkTest {

  @Test
  public void testUnredactedIpNetworkSample() throws JsonProcessingException {
    String sample = """
        {
          "rdapConformance" : [ "rdap_level_0" ],
          "objectClassName" : "ip network",
          "handle" : "abc-123"
        }""";

    IPNetwork ipNetwork = new IPNetwork(null, null, null, null, IPNetwork.OBJECT_CLASS_NAME,
        null, null, null, "abc-123", null, null, null, null,
        null, null, null);
    ipNetwork.addRdapConformance(DEFAULT_RDAP_CONFORMANCE);

    assertJsonMapping(ipNetwork, sample);
  }

  @Test
  public void testRedactedIpNetworkSample() throws JsonProcessingException {
    String sample = """
        {
          "rdapConformance" : [ "rdap_level_0", "redacted" ],
          "objectClassName" : "ip network",
          "redacted" : [ {
            "name" : {
              "description" : "IP Network handle"
            }
          } ]
        }""";

    Redacted redacted = new Redacted(Redacted.Name.fromDescription("IP Network handle"));
    IPNetwork ipNetwork = new IPNetwork(null, null, null, null, IPNetwork.OBJECT_CLASS_NAME,
        null, null, null, null, null, null, null, null,
        null, null, null, List.of(redacted));
    ipNetwork.addRdapConformance(DEFAULT_RDAP_CONFORMANCE);
    ipNetwork.addRdapConformance(REDACTED_EXTENSION_CONFORMANCE);

    assertJsonMapping(ipNetwork, sample);
  }

}