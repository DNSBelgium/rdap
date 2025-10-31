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
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.ImmutableList;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


@JsonPropertyOrder({"rdapConformance", "objectClassName"})
class Common {

  public static final String DEFAULT_RDAP_CONFORMANCE = "rdap_level_0";
  public static final String REDACTED_EXTENSION_CONFORMANCE = "redacted";

  public Set<String> rdapConformance;

  public List<Link> links;

  public List<Notice> notices;

  public List<Notice> remarks;

  public String lang;

  public String objectClassName;

  public List<Event> events;

  public List<Status> status;

  public DomainName port43;

  public List<Redacted> redacted;

  @JsonCreator
  public Common(
      @JsonProperty("links") List<Link> links,
      @JsonProperty("notices") List<Notice> notices,
      @JsonProperty("remarks") List<Notice> remarks,
      @JsonProperty("lang") String lang,
      @JsonProperty("objectClassName") String objectClassName,
      @JsonProperty("events") List<Event> events,
      @JsonProperty("status") List<Status> status,
      @JsonProperty("port43") DomainName port43,
      @JsonProperty("redacted") List<Redacted> redacted
  ) {
    this.links = links == null ? null : new ImmutableList.Builder<Link>().addAll(links).build();
    this.notices = notices == null ? null : new ImmutableList.Builder<Notice>().addAll(notices).build();
    this.remarks = remarks == null ? null : new ImmutableList.Builder<Notice>().addAll(remarks).build();
    this.lang = lang;
    this.objectClassName = objectClassName;
    this.events = events == null ? null : new ImmutableList.Builder<Event>().addAll(events).build();
    this.status = status == null ? null : new ImmutableList.Builder<Status>().addAll(status).build();
    this.port43 = port43;
    this.redacted = redacted == null ? null : new ImmutableList.Builder<Redacted>().addAll(redacted).build();
  }

  public Common(
      List<Link> links,
      List<Notice> notices,
      List<Notice> remarks,
      String lang,
      String objectClassName,
      List<Event> events,
      List<Status> status,
      DomainName port43
  ) {
    this(links, notices, remarks, lang, objectClassName, events, status, port43, null);
  }

  public void addRdapConformance(String conformance) {
    if (rdapConformance == null) {
      rdapConformance = new LinkedHashSet<>();
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

  public List<Notice> getRemarks() {
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

  public List<Redacted> getRedacted() {
    return redacted;
  }

  public void addRedacted(Redacted... redacted) {
    ImmutableList.Builder<Redacted> builder = new ImmutableList.Builder<>();
    if (this.redacted != null) {
      builder.addAll(this.redacted);
    }
    builder.add(redacted);
    this.redacted = builder.build();
  }
}
