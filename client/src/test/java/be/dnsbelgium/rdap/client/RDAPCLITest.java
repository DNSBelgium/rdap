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
package be.dnsbelgium.rdap.client;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RDAPCLITest {

  @Test
  public void testGuess() {
    assertEquals(RDAPCLI.Type.AUTNUM,RDAPCLI.guess("12345"));
    assertEquals(RDAPCLI.Type.IP,RDAPCLI.guess("127.0.0.0/8"));
    assertEquals(RDAPCLI.Type.DOMAIN, RDAPCLI.guess("foo.example"));
    assertEquals(RDAPCLI.Type.ENTITY,RDAPCLI.guess("handle"));
  }
}
