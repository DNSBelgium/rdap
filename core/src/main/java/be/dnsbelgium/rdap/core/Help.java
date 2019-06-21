package be.dnsbelgium.rdap.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonPropertyOrder({"rdapConformance"})
public class Help {

  public Set<String> rdapConformance;
  public List<Notice> notices;
  public List<Link> links = new ArrayList<>();

  public Help(@JsonProperty("notices") List<Notice> notices) {
    this.notices = notices;
  }

  public void addRdapConformance(String conformance) {
    if (rdapConformance == null) {
      rdapConformance = new HashSet<String>();
    }
    rdapConformance.add(conformance);
  }
}
