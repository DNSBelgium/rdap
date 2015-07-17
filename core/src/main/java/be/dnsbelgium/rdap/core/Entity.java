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
import be.dnsbelgium.vcard.Contact;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Locale;

public final class Entity extends Common {

  public static final String OBJECT_CLASS_NAME = "entity";

  public interface Role {

    String getValue();

    public static enum Default implements Role {
      REGISTRANT, TECHNICAL, ADMINISTRATIVE, ABUSE, BILLING, REGISTRAR, RESELLER, SPONSOR, PROXY, NOTIFICATIONS, NOC;

      private final String value;

      private Default() {
        this.value = name().toLowerCase(Locale.ENGLISH);
      }

      @Override
      public String getValue() {
        return value;
      }
    }

    class BasicRole implements Role {

      private final String value;

      public BasicRole(String value) {
        this.value = value;
      }

      @Override
      public String getValue() {
        return value;
      }
    }

  }


  private final String handle;

  private final Contact vcardArray;

  private final List<Role> roles;

  private final List<Event> asEventActor;

  private final List<PublicId> publicIds;

  @JsonCreator
  public Entity(
      @JsonProperty("links") List<Link> links,
      @JsonProperty("notices") List<Notice> notices,
      @JsonProperty("remarks") List<Notice> remarks,
      @JsonProperty("lang") String lang,
      @JsonProperty("objectClassName") String objectClassName,
      @JsonProperty("events") List<Event> events,
      @JsonProperty("status") List<Status> status,
      @JsonProperty("port43") DomainName port43,
      @JsonProperty("handle") String handle,
      @JsonProperty("vcardArray") Contact vcardArray,
      @JsonProperty("roles") List<Role> roles,
      @JsonProperty("asEventActor") List<Event> asEventActor,
      @JsonProperty("publicIds") List<PublicId> publicIds) {
    super(links, notices, remarks, lang, objectClassName, events, status, port43);
    this.handle = handle;
    this.vcardArray = vcardArray;
    this.roles = roles == null ? null : new ImmutableList.Builder<Role>().addAll(roles).build();
    this.asEventActor = asEventActor == null ? null : new ImmutableList.Builder<Event>().addAll(asEventActor).build();
    this.publicIds = publicIds == null ? null : new ImmutableList.Builder<PublicId>().addAll(publicIds).build();
  }

  public String getHandle() {
    return handle;
  }

  public Contact getvcardArray() {
    return vcardArray;
  }

  public List<Role> getRoles() {
    return roles;
  }

  public List<Event> getAsEventActor() {
    return asEventActor;
  }

  public List<PublicId> getPublicIds() {
    return publicIds;
  }
}
