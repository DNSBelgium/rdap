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

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

public abstract class AbstractDeserializerTest<T extends JsonDeserializer, S extends Object> {

  private final ObjectMapper objectMapper = new ObjectMapper();

  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  public abstract T getDeserializer();

  @SuppressWarnings("unchecked")
  public S deserialize(String text) throws IOException {
    JsonFactory factory = new JsonFactory();
    T deserializer = getDeserializer();
    JsonParser parser = factory.createJsonParser(text);
    parser.nextToken();
    return (S) deserializer.deserialize(parser, null); // unchecked
  }

}
