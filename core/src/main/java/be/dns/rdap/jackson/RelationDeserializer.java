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

import be.dns.rdap.core.Domain;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RelationDeserializer extends JsonDeserializer<Domain.Variant.Relation> {

  private static final Logger LOGGER = LoggerFactory.getLogger(RelationDeserializer.class);

  @Override
  public Domain.Variant.Relation deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
    LOGGER.debug("Deserializing {}", jp.getText());
    return Domain.Variant.Relation.Factory.of(jp.getText());
  }

}
