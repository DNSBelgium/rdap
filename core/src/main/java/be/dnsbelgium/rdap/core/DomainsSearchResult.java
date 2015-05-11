package be.dnsbelgium.rdap.core;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonPropertyOrder({"rdapConformance"})
public class DomainsSearchResult {

  public Set<String> rdapConformance;
  public List<Domain> domainSearchResults;

  @JsonCreator
  public DomainsSearchResult(@JsonProperty("domainSearchResults") List<Domain> domainSearchResults) {
    this.domainSearchResults = domainSearchResults;
  }

  public void addRdapConformance(String conformance) {
    if (rdapConformance == null) {
      rdapConformance = new HashSet<String>();
    }
    rdapConformance.add(conformance);
  }
}
