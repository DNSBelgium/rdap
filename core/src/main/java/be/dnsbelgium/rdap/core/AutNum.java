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
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;
import java.util.Set;

public class AutNum extends Common {

  private final String handle;

  private final int startAutnum;

  private final int endAutnum;

  private final String name;

  private final String type;

  private final String country;

  public AutNum(
      @JsonProperty("rdapConformance") Set<String> rdapConformance,
      @JsonProperty("links") List<Link> links,
      @JsonProperty("notices") List<Notice> notices,
      @JsonProperty("remarks") List<Notice> remarks,
      @JsonProperty("lang") String lang,
      @JsonProperty("events") List<Event> events,
      @JsonProperty("status") List<Status> status,
      @JsonProperty("port43") DomainName port43,
      @JsonProperty("handle") String handle,
      @JsonProperty("startAutnum") int startAutnum,
      @JsonProperty("endAutnum") int endAutnum,
      @JsonProperty("name") String name,
      @JsonProperty("type") String type,
      @JsonProperty("country") String country) {
    super(rdapConformance, links, notices, remarks, lang, events, status, port43);
    this.handle = handle;
    this.startAutnum = startAutnum;
    this.endAutnum = endAutnum;
    this.name = name;
    this.type = type;
    this.country = country;
  }

  public String getHandle() {
    return handle;
  }

  public int getStartAutnum() {
    return startAutnum;
  }

  public int getEndAutnum() {
    return endAutnum;
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
}
