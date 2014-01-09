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
package be.dnsbelgium.vcard;

import be.dnsbelgium.vcard.datatype.URIValue;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

public class URITypeTest {

  @Test
  public void testValidURIType() throws URISyntaxException {
    URIValue type = new URIValue(new URI("scheme", "schemeSpecificPart", "fragment"));
    assertEquals("scheme:schemeSpecificPart#fragment", type.getStringValue());
  }

  @Test
  public void testValidURITypeNoFragment() throws URISyntaxException {
    URIValue type = new URIValue(new URI("scheme", "schemeSpecificPart", null));
    assertEquals("scheme:schemeSpecificPart", type.getStringValue());
  }


}
