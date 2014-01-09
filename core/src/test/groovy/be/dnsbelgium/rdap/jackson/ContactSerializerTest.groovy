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
import be.dnsbelgium.vcard.datatype.Text
import org.codehaus.jackson.map.BeanProperty
import org.codehaus.jackson.map.JsonSerializer
import org.codehaus.jackson.map.SerializerProvider
import org.junit.Test

import static org.mockito.Matchers.any
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

public class ContactSerializerTest extends AbstractSerializerTest<ContactSerializer, Contact> {

    @Override
    ContactSerializer getSerializer() {
        return new ContactSerializer();
    }

    @Test
    public void testNoParameters() {

        def contact = Contact.of(
                Contact.Property.of("fn", Text.of("John Doe")),
                Contact.Property.of("email", Text.of("jdoe@example.com"))
        )

        SerializerProvider serializerProvider = mock(SerializerProvider.class);
        // required to cast first param to Class, otherwise not obvious which method to mock
        when(serializerProvider.findValueSerializer((Class) eq(Text.class), any(BeanProperty.class)))
                .thenReturn((JsonSerializer) new TextSerializer())
                .thenReturn((JsonSerializer) new TextSerializer());

        def expected = [
                ["version", [:], "text", "4.0"],
                ["fn", [:], "text", "John Doe"],
                ["email", [:], "text", "jdoe@example.com"]
        ]

        this.serializeAndAssertEquals(getObjectMapper().writeValueAsString(expected), contact, serializerProvider);
    }

    @Test
    public void testParametersAndGroup() {

        def contact = Contact.of(
                Contact.Property.of("group1", "key1", Contact.Parameters.of(["param1": ["value1"] as Set]), Text.of("text"))
        )

        SerializerProvider serializerProvider = mock(SerializerProvider.class);
        // required to cast first param to Class, otherwise not obvious which method to mock
        when(serializerProvider.findValueSerializer((Class) eq(Text.class), any(BeanProperty.class)))
                .thenReturn((JsonSerializer) new TextSerializer());

        def expected = [
                ["version", [:], "text", "4.0"],
                ["key1", ["group": "group1", "param1": "value1"], "text", "text"],
        ]

        this.serializeAndAssertEquals(getObjectMapper().writeValueAsString(expected), contact, serializerProvider);


    }
}