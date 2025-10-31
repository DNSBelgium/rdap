package be.dnsbelgium.rdap.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import java.util.List;

import static be.dnsbelgium.rdap.core.Common.DEFAULT_RDAP_CONFORMANCE;
import static be.dnsbelgium.rdap.core.Common.REDACTED_EXTENSION_CONFORMANCE;
import static be.dnsbelgium.rdap.jackson.TestObjectMapper.assertJsonMapping;

public class AutNumTest {

  @Test
  public void testUnredactedAutNumSample() throws JsonProcessingException {
    String sample = """
        {
          "rdapConformance" : [ "rdap_level_0" ],
          "objectClassName" : "autnum",
          "handle" : "AS12345",
          "startAutnum" : 6000,
          "endAutnum" : 6300
        }""";

    AutNum autNum = new AutNum(null, null, null, null, null, null, null,
        "AS12345", 6000, 6300, null, null, null);
    autNum.addRdapConformance(DEFAULT_RDAP_CONFORMANCE);

    assertJsonMapping(autNum, sample);
  }

  @Test
  public void testRedactedAutNumSample() throws JsonProcessingException {
    String sample = """
        {
          "rdapConformance" : [ "rdap_level_0", "redacted" ],
          "objectClassName" : "autnum",
          "startAutnum" : 6000,
          "endAutnum" : 6300,
          "redacted" : [ {
            "name" : {
              "description" : "AutNum handle"
            }
          } ]
        }""";

    Redacted redacted = new Redacted(Redacted.Name.fromDescription("AutNum handle"));
    AutNum autNum = new AutNum(null, null, null, null, null, null, null,
        null, 6000, 6300, null, null, null, List.of(redacted));
    autNum.addRdapConformance(DEFAULT_RDAP_CONFORMANCE);
    autNum.addRdapConformance(REDACTED_EXTENSION_CONFORMANCE);

    assertJsonMapping(autNum, sample);
  }

}