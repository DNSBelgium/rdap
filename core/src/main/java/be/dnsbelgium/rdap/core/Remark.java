package be.dnsbelgium.rdap.core;

import com.google.common.collect.ImmutableList;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

public class Remark {

  private final String type;

  private final List<String> description;

  private final List<Link> links;

  @JsonCreator
  public Remark(
          @JsonProperty("type") String type,
          @JsonProperty("description") List<String> description,
          @JsonProperty("links") List<Link> links) {
    this.type = type;
    this.description = (description == null) ? null : new ImmutableList.Builder<String>().addAll(description).build();
    this.links = (links == null) ? null : new ImmutableList.Builder<Link>().addAll(links).build();
  }

  public String getType() {
    return type;
  }

  public List<String> getDescription() {
    return description;
  }

  public List<Link> getLinks() {
    return links;
  }
}
