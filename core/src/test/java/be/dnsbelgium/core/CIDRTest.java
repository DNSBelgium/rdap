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
package be.dnsbelgium.core;

import be.dnsbelgium.junit.Assert;
import org.junit.Test;

public class CIDRTest {

  @Test
  public void testDoesItSmoke() {
    Assert.assertThrows(new Assert.Closure() {
      @Override
      public void execute() throws Throwable {
        CIDR.of("1.1.1.1/-1");
      }
    }, IllegalArgumentException.class, "negative prefix size");

  }

}
