package be.dns.rdap.jackson;

/*
 * #%L
 * Core
 * %%
 * Copyright (C) 2013 DNS Belgium vzw
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import be.dns.core.DomainName;
import be.dns.rdap.core.Domain;
import be.dns.rdap.core.Entity;
import be.dns.rdap.core.Event;
import be.dns.rdap.core.Status;
import be.dns.vcard.Contact;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.module.SimpleModule;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class CustomObjectMapper extends ObjectMapper {

  public CustomObjectMapper() {
    super.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
    setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
    configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
    SimpleModule simpleModule = new SimpleModule("SimpleModule",
        new Version(1, 0, 0, null));

    simpleModule.addSerializer(new ContactSerializer());
    simpleModule.addSerializer(new StructuredValueSerializer());
    simpleModule.addSerializer(new TextListSerializer());
    simpleModule.addSerializer(new TextSerializer());
    simpleModule.addSerializer(new URIValueSerializer());
    simpleModule.addSerializer(new DomainNameSerializer());
    simpleModule.addSerializer(new DateTimeSerializer());
    simpleModule.addSerializer(new StatusSerializer());
    for (JsonSerializer serializer: getSerializers()) {
      simpleModule.addSerializer(serializer);
    }

    simpleModule.addDeserializer(Contact.class, new ContactDeserializer());
    simpleModule.addDeserializer(DomainName.class, new DomainNameDeserializer());
    simpleModule.addDeserializer(Entity.Role.class, new RoleDeserializer());
    simpleModule.addDeserializer(DateTime.class, new DateTimeDeserializer());
    simpleModule.addDeserializer(Event.Action.class, new ActionDeserializer());
    simpleModule.addDeserializer(Status.class, new StatusDeserializer());
    simpleModule.addDeserializer(Domain.Variant.Relation.class, new RelationDeserializer());

    registerModule(simpleModule);
  }

  public List<JsonSerializer> getSerializers() {
    return new ArrayList<JsonSerializer>();
  }

}
