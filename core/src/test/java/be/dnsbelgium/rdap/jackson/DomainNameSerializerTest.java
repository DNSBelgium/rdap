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

import be.dnsbelgium.core.DomainName;
import org.junit.Test;

import java.io.IOException;

public class DomainNameSerializerTest extends AbstractSerializerTest<DomainNameSerializer, DomainName> {

  @Test
  public void testSerialization() throws IOException {
    String expected = "example.com";
    serializeAndAssertEquals(getObjectMapper().writeValueAsString(expected), DomainName.of(expected));
    expected = "xn--bcher-kva.example";
    serializeAndAssertEquals(getObjectMapper().writeValueAsString(expected), DomainName.of(expected));
    expected = "bücher.example";
    serializeAndAssertEquals(getObjectMapper().writeValueAsString(expected), DomainName.of(expected));
  }

  @Test
  public void testNullDomainName() throws IOException {
    serializeAndAssertEquals(getObjectMapper().writeValueAsString(null), null);
  }

  @Override
  public DomainNameSerializer getSerializer() {
    return new DomainNameSerializer();
  }
}