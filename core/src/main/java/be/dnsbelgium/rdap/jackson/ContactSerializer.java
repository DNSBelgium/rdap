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

import be.dnsbelgium.vcard.Contact;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

public class ContactSerializer extends JsonSerializer<Contact> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContactSerializer.class);


  @Override
  public Class<Contact> handledType() {
    return Contact.class;
  }

  @Override
  public void serialize(Contact contact, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
    jsonGenerator.writeStartArray();
    // start write version
    jsonGenerator.writeStartArray();
    jsonGenerator.writeString("version");
    jsonGenerator.writeStartObject();
    jsonGenerator.writeEndObject();
    jsonGenerator.writeString("text");
    jsonGenerator.writeString("4.0");
    jsonGenerator.writeEndArray();
    // end write version
    for (Contact.Property property : contact.getProperties()) {
      // start write property
      jsonGenerator.writeStartArray();
      // start write property name
      String key = (property.getGroup() == null) ? property.getName() : property.getGroup() + "." + property.getName();
      jsonGenerator.writeString(property.getName().toLowerCase(Locale.ENGLISH));
      // end write property name
      // start write property parameters
      jsonGenerator.writeStartObject();
      if (property.getGroup() != null) {
        jsonGenerator.writeFieldName("group");
        jsonGenerator.writeString(property.getGroup());
      }
      if (property.getParameters() != null) {

        Iterator<String> it = property.getParameters().keys();
        while (it.hasNext()) {
          String k = it.next();
          if (k.equalsIgnoreCase("value")) {
            continue;
          }
          Set<String> values = property.getParameters().get(k);
          if (values.size() == 0) {
            // no parameters for this property, skip this step
            continue;
          }
          jsonGenerator.writeFieldName(k.toLowerCase(Locale.ENGLISH));
          if (values.size() == 1) {
            jsonGenerator.writeString(values.toArray(new String[values.size()])[0]);
            continue;
          }
          // start write all property parameter values (array)
          jsonGenerator.writeStartArray();
          for (String str : property.getParameters().get(k)) {
            jsonGenerator.writeString(str);
          }
          jsonGenerator.writeEndArray();
          // end write all property parameter values (array)

        }
      }
      jsonGenerator.writeEndObject();
      // end write property parameters
      // start write property type
      String value = "text";
      if (property.getValue() != null && property.getValue().getTypeName() != null){
        value = property.getValue().getTypeName();
      }
      jsonGenerator.writeString(value);
      // end write property type
      // start write property value
      JsonSerializer s = serializerProvider.findValueSerializer(property.getValue().getClass(), null);
      s.serialize(property.getValue(), jsonGenerator, serializerProvider);
      // end write property value
      jsonGenerator.writeEndArray();
      // end write property
    }
    jsonGenerator.writeEndArray();
  }

  @Override
  public void serializeWithType(Contact value, JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
    super.serializeWithType(value, jgen, provider, typeSer);
  }
}
