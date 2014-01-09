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

import org.codehaus.jackson.annotate.JsonProperty;
import org.joda.time.DateTime;

public class Event {

  public interface Action {

    public static enum Default implements Action {
      REGISTRATION, REREGISTRATION, LAST_CHANGED, EXPIRATION, DELETION, REINSTANTIATION, TRANSFER;
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

  public Event(
      @JsonProperty("eventAction") Action eventAction,
      @JsonProperty("eventActor") String eventActor,
      @JsonProperty("eventDate") DateTime eventDate) {
    this.eventAction = eventAction;
    this.eventActor = eventActor;
    this.eventDate = eventDate;
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
}
