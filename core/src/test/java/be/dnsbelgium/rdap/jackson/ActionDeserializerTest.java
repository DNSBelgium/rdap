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

import be.dnsbelgium.rdap.core.Event;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ActionDeserializerTest {

  @Test
  public void testNormal() throws IOException {
    Event.Action expected = Event.Action.Default.DELETION;
    Event.Action actual = deserialize("deletion");
    assertEquals(expected, actual);
  }


  private static Event.Action deserialize(String action) throws IOException {
    JsonFactory factory = new JsonFactory();
    ActionDeserializer deserializer = new ActionDeserializer();
    String json = (action == null) ? "null" : "\"" + action + "\"";
    JsonParser parser = factory.createJsonParser(json);
    parser.nextToken();
    return deserializer.deserialize(parser, null);
  }

}
