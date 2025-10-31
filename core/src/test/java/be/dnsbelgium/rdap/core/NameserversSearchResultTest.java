package be.dnsbelgium.rdap.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import java.util.List;

import static be.dnsbelgium.rdap.core.Common.DEFAULT_RDAP_CONFORMANCE;
import static be.dnsbelgium.rdap.core.Common.REDACTED_EXTENSION_CONFORMANCE;
import static be.dnsbelgium.rdap.jackson.TestObjectMapper.assertJsonMapping;

public class NameserversSearchResultTest {

  private final Nameserver unredactedNameserver1 = new Nameserver(null, null, null, null, null,
      null, null, "abc-123", null, null, null);
  private final Nameserver unredactedNameserver2 = new Nameserver(null, null, null, null, null,
      null, null, "xyz-789", null, null, null);
  private final Redacted redacted = new Redacted(Redacted.Name.fromDescription("Nameserver handle"));
  private final Nameserver redactedNameserver = new Nameserver(null, null, null, null, null,
      null, null, null, null, null, null, List.of(redacted));

  @Test
  public void testUnredactedNameserverSearchResultSample() throws JsonProcessingException {
    String sample = """
        {
          "rdapConformance" : [ "rdap_level_0" ],
          "nameserverSearchResults" : [ {
            "objectClassName" : "nameserver",
            "handle" : "abc-123"
          }, {
            "objectClassName" : "nameserver",
            "handle" : "xyz-789"
          } ]
        }""";

    NameserversSearchResult nameserverSearchResult = new NameserversSearchResult(List.of(unredactedNameserver1, unredactedNameserver2));
    nameserverSearchResult.addRdapConformance(DEFAULT_RDAP_CONFORMANCE);

    assertJsonMapping(nameserverSearchResult, sample);
  }

  @Test
  public void testRedactedNameserverSearchResultSample() throws JsonProcessingException {
    String sample = """
        {
          "rdapConformance" : [ "rdap_level_0", "redacted" ],
          "nameserverSearchResults" : [ {
            "objectClassName" : "nameserver",
            "redacted" : [ {
              "name" : {
                "description" : "Nameserver handle"
              }
            } ]
          }, {
            "objectClassName" : "nameserver",
            "handle" : "xyz-789"
          }, {
            "objectClassName" : "nameserver",
            "redacted" : [ {
              "name" : {
                "description" : "Nameserver handle"
              }
            } ]
          } ]
        }""";

    NameserversSearchResult nameserverSearchResult = new NameserversSearchResult(List.of(redactedNameserver, unredactedNameserver2, redactedNameserver));
    nameserverSearchResult.addRdapConformance(DEFAULT_RDAP_CONFORMANCE);
    nameserverSearchResult.addRdapConformance(REDACTED_EXTENSION_CONFORMANCE);

    assertJsonMapping(nameserverSearchResult, sample);
  }

}