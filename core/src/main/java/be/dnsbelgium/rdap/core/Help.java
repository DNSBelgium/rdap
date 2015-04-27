package be.dnsbelgium.rdap.core;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonPropertyOrder({"rdapConformance"})
public class Help {

  public Set<String> rdapConformance;
  public List<Notice> notices;

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
