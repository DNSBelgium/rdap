package be.dnsbelgium.rdap.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import java.util.List;

import static be.dnsbelgium.rdap.core.Common.DEFAULT_RDAP_CONFORMANCE;
import static be.dnsbelgium.rdap.core.Common.REDACTED_EXTENSION_CONFORMANCE;
import static be.dnsbelgium.rdap.jackson.TestObjectMapper.assertJsonMapping;

public class DomainsSearchResultTest {

  private final Domain unredactedDomain1 = new Domain(null, null, null, null, null,
      null, null, "abc-123", null, null, null, null,
      null, null, null, null);
  private final Domain unredactedDomain2 = new Domain(null, null, null, null, null,
      null, null, "xyz-789", null, null, null, null,
      null, null, null, null);
  private final Redacted redacted = new Redacted(Redacted.Name.fromType(Redacted.Name.Type.Default.REGISTRY_DOMAIN_ID));
  private final Domain redactedDomain = new Domain(null, null, null, null, null, null,
      null, null, null, null, null, null, null,
      null, null, null, List.of(redacted));

  @Test
  public void testUnredactedDomainSearchResultSample() throws JsonProcessingException {
    String sample = """
        {
          "rdapConformance" : [ "rdap_level_0" ],
          "domainSearchResults" : [ {
            "objectClassName" : "domain",
            "handle" : "abc-123"
          }, {
            "objectClassName" : "domain",
            "handle" : "xyz-789"
          } ]
        }""";

    DomainsSearchResult domainSearchResult = new DomainsSearchResult(List.of(unredactedDomain1, unredactedDomain2));
    domainSearchResult.addRdapConformance(DEFAULT_RDAP_CONFORMANCE);

    assertJsonMapping(domainSearchResult, sample);
  }

  @Test
  public void testRedactedDomainSearchResultSample() throws JsonProcessingException {
    String sample = """
        {
          "rdapConformance" : [ "rdap_level_0", "redacted" ],
          "domainSearchResults" : [ {
            "objectClassName" : "domain",
            "redacted" : [ {
              "name" : {
                "type" : "Registry Domain ID"
              }
            } ]
          }, {
            "objectClassName" : "domain",
            "handle" : "xyz-789"
          }, {
            "objectClassName" : "domain",
            "redacted" : [ {
              "name" : {
                "type" : "Registry Domain ID"
              }
            } ]
          } ]
        }""";

    DomainsSearchResult domainSearchResult = new DomainsSearchResult(List.of(redactedDomain, unredactedDomain2, redactedDomain));
    domainSearchResult.addRdapConformance(DEFAULT_RDAP_CONFORMANCE);
    domainSearchResult.addRdapConformance(REDACTED_EXTENSION_CONFORMANCE);

    assertJsonMapping(domainSearchResult, sample);
  }

}