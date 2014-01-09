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
package be.dnsbelgium.rdap.jackson

import be.dnsbelgium.vcard.Contact
import be.dnsbelgium.vcard.datatype.StructuredValue
import org.codehaus.jackson.map.BeanProperty
import org.codehaus.jackson.map.JsonSerializer
import org.codehaus.jackson.map.SerializerProvider
import org.junit.Test

import static org.mockito.Matchers.any
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

public class StructuredValueSerializerTest extends AbstractSerializerTest<ContactSerializer, StructuredValue> {

  @Test
  public void testSingleValues() {

    def contact = Contact.of(
        Contact.Property.of("n", new StructuredValue.NType(["family"], ["given"], ["additional"], ["prefix"], ["suffix"]))
    )

    SerializerProvider serializerProvider = mock(SerializerProvider.class);


    // required to cast first param to Class, otherwise not obvious which method to mock
    when(serializerProvider.findValueSerializer((Class) eq(StructuredValue.NType.class), any(BeanProperty.class)))
        .thenReturn((JsonSerializer) new StructuredValueSerializer());
    when(serializerProvider.findValueSerializer((Class) eq(be.dnsbelgium.vcard.datatype.AbstractList.TextList.class), any(BeanProperty.class)))
        .thenReturn((JsonSerializer) new TextListSerializer());

    def expected = [
        ["version", [:], "text", "4.0"],
        ["n", [:], "text", "family", "given", "additional", "prefix", "suffix"]
    ]

    this.serializeAndAssertEquals(getObjectMapper().writeValueAsString(expected), contact, serializerProvider);
  }

  @Override
  ContactSerializer getSerializer() {
    return new ContactSerializer();
  }
}