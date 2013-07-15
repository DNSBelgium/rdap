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

import be.dns.vcard.Contact;
import be.dns.vcard.datatype.*;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class ContactDeserializer extends JsonDeserializer<Contact> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContactDeserializer.class);


  public static abstract class DataTypeDeserializer<T extends Value> {
    public abstract T deserialize(JsonNode jsonNode);
  }

  private Map<String, DataTypeDeserializer<Value>> dataTypeDeserializerMap;

  public ContactDeserializer() {
    super();
    this.dataTypeDeserializerMap = new HashMap<String, DataTypeDeserializer<Value>>();
    register("fn", new TextTypeDeserializer());
    register("n", new NTypeDeserializer());
    register("org", new ORGTypeDeserializer());
    register("adr", new ADRTypeDeserializer());
    register("email", new TextTypeDeserializer());
    register("lang", new LanguageTagTypeDeserializer());
    register("tel", new TELTypeDeserializer());
  }

  @Override
  public Contact deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    ObjectCodec oc = jp.getCodec();
    JsonNode node = oc.readTree(jp);

    List<Contact.Property> properties = new ArrayList<Contact.Property>();
    Iterator<JsonNode> propertyNodes = node.getElements();
    while (propertyNodes.hasNext()) {
      JsonNode propertyNode = propertyNodes.next();

      if (!propertyNode.isArray() && propertyNode.size() < 4) {
        // invalid propertyNode. skip it
        continue;
      }

      String[] fullName = propertyNode.get(0).getTextValue().split("\\.");
      if (fullName.length > 2) {
        // invalid. should maximum contain 1 dot
        continue;
      }
      String group = (fullName.length == 2) ? fullName[0] : null;
      String name = (fullName.length == 2) ? fullName[1] : fullName[0];
      Contact.Parameters.Builder builder = new Contact.Parameters.Builder();
      // start deserialize parameters
      Iterator<Map.Entry<String, JsonNode>> parameters = propertyNode.get(1).getFields();
      while (parameters.hasNext()) {
        Map.Entry<String, JsonNode> parameter = parameters.next();
        String key = parameter.getKey();
        if (parameter.getValue().isArray()) {
          // parameter could be multivalued
          Iterator<JsonNode> paramValues = parameter.getValue().getElements();
          while (paramValues.hasNext()) {
            String paramValue = paramValues.next().getTextValue();
            builder.add(key, paramValue);
          }
        } else {
          builder.add(key, parameter.getValue().getTextValue());
        }
      }
      // end deserialize parameters
      builder.add("VALUE", propertyNode.get(2).getTextValue());
      DataTypeDeserializer deserializer = getDeserializer(name);
      if (deserializer == null) continue;
      Value value = deserializer.deserialize(propertyNode);
      Contact.Property p = new Contact.Property(group, name, builder.build(), value);
      properties.add(p);
    }
    return new Contact(properties);
  }

  public void register(String propertyName, DataTypeDeserializer deserializer) {
    this.dataTypeDeserializerMap.put(propertyName.toLowerCase(Locale.ENGLISH), deserializer);
  }

  public DataTypeDeserializer getDeserializer(String propertyName) {
    return this.dataTypeDeserializerMap.get(propertyName.toLowerCase(Locale.ENGLISH));
  }

  public static class TextTypeDeserializer extends DataTypeDeserializer<Text> {
    @Override
    public Text deserialize(JsonNode jsonNode) {
      if (jsonNode.get(3) == null) return new Text(null);
      return new Text(jsonNode.get(3).getTextValue());
    }
  }

  public static class URITypeDeserializer extends DataTypeDeserializer<URIValue> {

    @Override
    public URIValue deserialize(JsonNode jsonNode) {
      try {
        return new URIValue(new URI(jsonNode.get(3).getTextValue()));
      } catch (URISyntaxException e) {
        return null;
      }
    }
  }

  public static class NTypeDeserializer extends DataTypeDeserializer<StructuredValue.NType> {


    @Override
    public StructuredValue.NType deserialize(JsonNode jsonNode) {

      return new StructuredValue.NType(DeserializerUtils.toStringArray(jsonNode.get(3)),
          DeserializerUtils.toStringArray(jsonNode.get(4)),
          DeserializerUtils.toStringArray(jsonNode.get(5)),
          DeserializerUtils.toStringArray(jsonNode.get(6)),
          DeserializerUtils.toStringArray(jsonNode.get(7))
      );
    }
  }

  public static class ORGTypeDeserializer extends DataTypeDeserializer<StructuredValue.ORGType> {

    @Override
    public StructuredValue.ORGType deserialize(JsonNode jsonNode) {
      String[] units = null;
      if (jsonNode.size() > 4) {
        units = new String[jsonNode.size() - 4];
        for (int i = 4; i < jsonNode.size(); i++) {
          units[i - 4] = jsonNode.get(i).getTextValue();
        }
      }
      return StructuredValue.ORGType.of(jsonNode.get(3).getTextValue(), units);
    }
  }

  public static class ADRTypeDeserializer extends DataTypeDeserializer<StructuredValue.ADRType> {


    @Override
    public StructuredValue.ADRType deserialize(JsonNode jsonNode) {
      return new StructuredValue.ADRType(
          DeserializerUtils.toStringArray(jsonNode.get(3)),
          DeserializerUtils.toStringArray(jsonNode.get(4)),
          DeserializerUtils.toStringArray(jsonNode.get(5)),
          DeserializerUtils.toStringArray(jsonNode.get(6)),
          DeserializerUtils.toStringArray(jsonNode.get(7)),
          DeserializerUtils.toStringArray(jsonNode.get(8)),
          DeserializerUtils.toStringArray(jsonNode.get(9))
      );
    }
  }

  public static class TELTypeDeserializer extends DataTypeDeserializer<Tel> {

    @Override
    public Tel deserialize(JsonNode jsonNode) {
      try {
        URI uri = new URI(jsonNode.get(3).getTextValue());
        return new Tel(uri.getSchemeSpecificPart());
      } catch (URISyntaxException e) {
        return null;
      }
    }
  }

  public static class LanguageTagTypeDeserializer extends DataTypeDeserializer<LanguageTag> {

    @Override
    public LanguageTag deserialize(JsonNode jsonNode) {
      return new LanguageTag(jsonNode.get(3).getTextValue());
    }
  }

  public static abstract class DeserializerUtils {
    public static List<String> toStringArray(JsonNode node) {
      if (node == null) return null;
      if (node.isArray()) {
        List<String> result = new ArrayList<String>();
        Iterator<JsonNode> it = node.iterator();
        while (it.hasNext())
          result.add(it.next().getTextValue());
        return result;
      } else {
        return Arrays.asList(node.getTextValue());
      }

    }

  }


}
