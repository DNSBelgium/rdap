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
import com.google.common.collect.ImmutableSet;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Thread safe
 */
class Common {

  public static final String DEFAULT_RDAP_CONFORMANCE = "rdap_level_0";

  public Set<String> rdapConformance;

  public List<Link> links;

  public List<Notice> notices;

  public List<Remark> remarks;

  public String lang;

  public String objectClassName;

  public List<Event> events;

  public List<Status> status;

  public DomainName port43;

  @JsonCreator
  public Common(
      @JsonProperty("links") List<Link> links,
      @JsonProperty("notices") List<Notice> notices,
      @JsonProperty("remarks") List<Remark> remarks,
      @JsonProperty("lang") String lang,
      @JsonProperty("objectClassName") String objectClassName,
      @JsonProperty("events") List<Event> events,
      @JsonProperty("status") List<Status> status,
      @JsonProperty("port43") DomainName port43
  ) {
    this.links = links == null ? null : new ImmutableList.Builder<Link>().addAll(links).build();
    this.notices = notices == null ? null : new ImmutableList.Builder<Notice>().addAll(notices).build();
    this.remarks = remarks == null ? null : new ImmutableList.Builder<Remark>().addAll(remarks).build();
    this.lang = lang;
    this.objectClassName = objectClassName;
    this.events = events == null ? null : new ImmutableList.Builder<Event>().addAll(events).build();
    this.status = status == null ? null : new ImmutableList.Builder<Status>().addAll(status).build();
    this.port43 = port43;
  }

  public void addRdapConformance(String conformance) {
    if (rdapConformance == null) {
      rdapConformance = new HashSet<String>();
    }
    rdapConformance.add(conformance);
  }

  public Set<String> getRdapConformance() {
    return rdapConformance;
  }

  public List<Link> getLinks() {
    return links;
  }

  public List<Notice> getNotices() {
    return notices;
  }

  public List<Remark> getRemarks() {
    return remarks;
  }

  public String getLang() {
    return lang;
  }

  public String getObjectClassName() {
    return objectClassName;
  }

  public List<Event> getEvents() {
    return events;
  }

  public List<Status> getStatus() {
    return status;
  }

  public DomainName getPort43() {
    return port43;
  }
}
