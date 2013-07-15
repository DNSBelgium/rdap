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

import be.dns.rdap.core.Status;
import org.junit.Test;

import java.io.IOException;
import java.util.Locale;

public class StatusSerializerTest extends AbstractSerializerTest<StatusSerializer, Status> {

  @Test
  public void testSerialization() throws IOException {
    for (Status status : Status.Default.values()) {
      String expected = status.getValue().toLowerCase(Locale.ENGLISH);
      serializeAndAssertEquals(getObjectMapper().writeValueAsString(expected), status);
    }
  }

  @Test
  public void testNull() throws IOException {
    serializeAndAssertEquals(getObjectMapper().writeValueAsString(null), null);
    serializeAndAssertEquals(getObjectMapper().writeValueAsString(null), new Status() {
      @Override
      public String getValue() {
        return null;
      }
    });
  }

  @Override
  public StatusSerializer getSerializer() {
    return new StatusSerializer();
  }
}