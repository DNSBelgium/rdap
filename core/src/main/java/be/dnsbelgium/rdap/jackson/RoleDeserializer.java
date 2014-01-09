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

import be.dnsbelgium.rdap.core.Entity;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Locale;

public class RoleDeserializer extends JsonDeserializer<Entity.Role> {
  private static final Logger LOGGER = LoggerFactory.getLogger(RoleDeserializer.class);

  @Override
  public Entity.Role deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
    final String value = jp.getText();
    //LOGGER.debug("Creating Role of value '{}'", value);
    try {
      return Entity.Role.Default.valueOf(value.toUpperCase(Locale.ENGLISH));
    } catch (IllegalArgumentException iae) {
      LOGGER.debug("Not a default role, returning a free text role", iae);
      return new Entity.Role() {
        @Override
        public String getValue() {
          return value;
        }
      };
    }
  }
}
