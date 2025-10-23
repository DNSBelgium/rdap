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
package be.dnsbelgium.rdap.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class CustomObjectMapper extends ObjectMapper {

  public CustomObjectMapper() {
    configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    // Although currently still annotated with @Deprecated, this deprecation will (hopefully) be reverted as of
    // Jackson 2.21.0 (see https://github.com/FasterXML/jackson-databind/issues/1547), so we will use it again.
    configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
    setSerializationInclusion(JsonInclude.Include.NON_NULL);

    SimpleModule simpleModule = new SimpleModule("SimpleModule",
        new Version(1, 0, 0, null, null, null));
    simpleModule.addSerializer(new RDAPContactSerializer());
    simpleModule.addSerializer(new StructuredValueSerializer());
    simpleModule.addSerializer(new TextListSerializer());
    simpleModule.addSerializer(new TextSerializer());
    simpleModule.addSerializer(new URIValueSerializer());
    simpleModule.addSerializer(new DomainNameSerializer());
    simpleModule.addSerializer(new DateTimeSerializer());
    simpleModule.addSerializer(new StatusSerializer());
    simpleModule.addSerializer(new EntityRoleSerializer());
    simpleModule.addSerializer(new EventActionSerializer());

    registerModule(simpleModule);
  }

}
