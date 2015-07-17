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

import be.dnsbelgium.vcard.datatype.AbstractList;
import be.dnsbelgium.vcard.datatype.StructuredValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class StructuredValueSerializer extends JsonSerializer<StructuredValue> {

  public static final Logger LOGGER = LoggerFactory.getLogger(StructuredValueSerializer.class);

  @Override
  public Class<StructuredValue> handledType() {
    return StructuredValue.class;
  }

  @Override
  public void serialize(StructuredValue value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
    for (StructuredValue.Component c : value.getComponents()) {
      // in a structured context, a list-component must start with [ and end with ]
      if (AbstractList.class.isAssignableFrom(c.value.getClass()) && ((AbstractList) c.value).getValues() != null && ((AbstractList) c.value).getValues().size() > 1) {
        jgen.writeStartArray();
        provider.findValueSerializer(c.value.getClass(), null).serialize(c.value, jgen, provider);
        jgen.writeEndArray();
      } else {
        provider.findValueSerializer(c.value.getClass(), null).serialize(c.value, jgen, provider);
      }
    }
  }
}
