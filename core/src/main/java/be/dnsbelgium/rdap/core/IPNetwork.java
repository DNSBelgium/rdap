/**
 * Copyright 2014 DNS Belgium vzw
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.dnsbelgium.rdap.core;

import be.dnsbelgium.core.DomainName;
import com.google.common.collect.ImmutableList;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

import java.net.InetAddress;
import java.util.List;
import java.util.Set;

@JsonPropertyOrder({"rdapConformance"})
public class IPNetwork extends Common {

  public static final String OBJECT_CLASS_NAME = "ip network";

  private final String handle;

  private final InetAddress startAddress , endAddress;

  private final String name;

  private final String type;

  private final String country;

  private final String parentHandle;

  private final List<Entity> entities;


  @JsonCreator
  public IPNetwork(
      @JsonProperty("links") List<Link> links,
      @JsonProperty("notices") List<Notice> notices,
      @JsonProperty("remarks") List<Notice> remarks,
      @JsonProperty("lang") String lang,
      @JsonProperty("objectClassName") String objectClassName,
      @JsonProperty("events") List<Event> events,
      @JsonProperty("status") List<Status> status,
      @JsonProperty("port43") DomainName port43,
      @JsonProperty("handle") String handle,
      @JsonProperty("startAddress") InetAddress startAddress,
      @JsonProperty("endAddress") InetAddress endAddress,
      @JsonProperty("name") String name,
      @JsonProperty("type") String type,
      @JsonProperty("country") String country,
      @JsonProperty("parentHandle") String parentHandle,
      @JsonProperty("entities") List<Entity> entities
  ) {
    super(links, notices, remarks, lang, objectClassName, events, status, port43);
    this.handle = handle;
    this.startAddress = startAddress;
    this.endAddress = endAddress;
    this.name = name;
    this.type = type;
    this.country = country;
    this.parentHandle = parentHandle;
    this.entities = entities == null? new ImmutableList.Builder<Entity>().build():new ImmutableList.Builder<Entity>().addAll(entities).build();
  }

  public String getHandle() {
    return handle;
  }

  public InetAddress getStartAddress() {
    return startAddress;
  }

  public InetAddress getEndAddress() {
    return endAddress;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public String getCountry() {
    return country;
  }

  public String getParentHandle() {
    return parentHandle;
  }

  public List<Entity> getEntities() {
    return entities;
  }
}
