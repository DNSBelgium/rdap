package be.dns.core;

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

import be.dns.junit.Assert;
import org.junit.Test;

import static be.dns.junit.Assert.assertThrows;
import static org.junit.Assert.*;

public class DomainNameTest {

  @Test
  public void testOnlyEmptyLabelAtTheEnd() {
    DomainName dn = DomainName.of("www.example.com.");
    assertEquals(3, dn.getLabels().indexOf(Label.RootLabel.getInstance()));

    assertThrows(new Assert.Closure() {
      @Override
      public void execute() throws Throwable {
        DomainName.of("www..example.com");
      }
    }, IllegalArgumentException.class, "Should throw IllegalArgumentException");

    assertThrows(new Assert.Closure() {
      @Override
      public void execute() throws Throwable {
        DomainName.of(".www.example.com");
      }
    }, IllegalArgumentException.class, "Should throw IllegalArgumentException");
  }

  @Test
  public void testLevelSize() {
    assertEquals(3, DomainName.of("www.example.com").getLevelSize());
    assertEquals(3, DomainName.of("www.example.com.").getLevelSize());
  }

  @Test
  public void testIsFQDN() {
    assertFalse(DomainName.of("www.example.com").isFQDN());
    assertTrue(DomainName.of("www.example.com.").isFQDN());
  }

  @Test
  public void testToFQDN() {
    DomainName dn = DomainName.of("www.example.com");
    assertEquals(4, dn.toFQDN().getLabels().size());
    assertEquals("www.example.com.", dn.toFQDN().getStringValue());
  }

}
