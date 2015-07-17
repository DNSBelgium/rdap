package be.dnsbelgium.rdap.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonPropertyOrder({"rdapConformance"})
public class NameserversSearchResult {

  public Set<String> rdapConformance;
  public List<Nameserver> nameserverSearchResults;

  @JsonCreator
  public NameserversSearchResult(@JsonProperty("nameserverSearchResults") List<Nameserver> nameserverSearchResults) {
    this.nameserverSearchResults = nameserverSearchResults;
  }

  public void addRdapConformance(String conformance) {
    if (rdapConformance == null) {
      rdapConformance = new HashSet<String>();
    }
    rdapConformance.add(conformance);
  }
}
