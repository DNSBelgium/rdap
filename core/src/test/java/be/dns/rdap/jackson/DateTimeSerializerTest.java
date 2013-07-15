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

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;

import java.io.IOException;

public class DateTimeSerializerTest extends AbstractSerializerTest<DateTimeSerializer, DateTime> {

  @Override
  public DateTimeSerializer getSerializer() {
    return new DateTimeSerializer();
  }

  @Test
  public void testNormal() throws IOException {
    DateTime now = DateTime.now().withMillisOfSecond(0);
    serializeAndAssertEquals(getObjectMapper().writeValueAsString(now.toString(ISODateTimeFormat.dateTimeNoMillis())), now);
  }

  @Test
  public void testNullDateTime() throws IOException {
    serializeAndAssertEquals(getObjectMapper().writeValueAsString(null), null);
  }

}