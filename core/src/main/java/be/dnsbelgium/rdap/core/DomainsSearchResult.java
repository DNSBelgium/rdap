package be.dnsbelgium.rdap.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.LinkedHashSet;
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
      rdapConformance = new LinkedHashSet<>();
    }
    rdapConformance.add(conformance);
  }
}
