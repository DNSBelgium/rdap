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

import com.google.common.collect.ImmutableList;
import org.codehaus.jackson.annotate.JsonProperty;
import org.joda.time.DateTime;

import java.util.List;

public class Event {

  public interface Action {

    public static enum Default implements Action {
      REGISTRATION, REREGISTRATION, LAST_CHANGED, EXPIRATION, DELETION, REINSTANTIATION, TRANSFER, LOCKED, UNLOCKED;
      private final String value;

      private Default() {
        this.value = name();
      }

      @Override
      public String getValue() {
        return value;
      }
    }
    String getValue();
  }

  private final Action eventAction;

  private final String eventActor;

  private final DateTime eventDate;

  private final List<Link> links;

  public Event(
      @JsonProperty("eventAction") Action eventAction,
      @JsonProperty("eventActor") String eventActor,
      @JsonProperty("eventDate") DateTime eventDate,
      @JsonProperty("links") List<Link> links) {
    this.eventAction = eventAction;
    this.eventActor = eventActor;
    this.eventDate = eventDate;
    this.links = (links == null) ? null : new ImmutableList.Builder<Link>().addAll(links).build();
  }

  public Action getEventAction() {
    return eventAction;
  }

  public String getEventActor() {
    return eventActor;
  }

  public DateTime getEventDate() {
    return eventDate;
  }

  public List<Link> getLinks() {
    return links;
  }
}
