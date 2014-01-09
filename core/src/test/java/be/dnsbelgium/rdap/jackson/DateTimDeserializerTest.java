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

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DateTimDeserializerTest extends AbstractDeserializerTest<DateTimeDeserializer, DateTime> {

  @Override
  public DateTimeDeserializer getDeserializer() {
    return new DateTimeDeserializer();
  }

  @Test
  public void testNormal() throws IOException {
    DateTime expected = DateTime.now().withMillisOfSecond(0); // don't take into account millis: they are not serialized
    DateTime actual = deserialize(getObjectMapper().writeValueAsString(expected.toString(ISODateTimeFormat.dateTimeNoMillis())));
    assertEquals(expected, actual);
  }

  @Test
  public void testEmptyString() throws IOException {
    DateTime actual = deserialize("");
    assertNull(actual);
  }

  @Test
  public void testNullDateTime() throws IOException {
    DateTime actual = deserialize("null");
    assertNull(actual);
  }
}
