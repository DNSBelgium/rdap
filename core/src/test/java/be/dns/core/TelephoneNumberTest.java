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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

import static be.dns.junit.Assert.Closure;
import static be.dns.junit.Assert.assertThrows;
import static org.junit.Assert.assertEquals;

public class TelephoneNumberTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(TelephoneNumberTest.class);

  @Test
  public void testValidTelephoneNumber() {
    LOGGER.debug("should have maximum 15 characters, incl. country code");
    TelephoneNumber tn = TelephoneNumber.of("+32 1234567890123");
    assertEquals(tn.getNationalNumber(), new BigInteger("1234567890123"));
    LOGGER.debug("All digits can stick together");
    tn = TelephoneNumber.of("+321234567890123");
    assertEquals(new BigInteger("1234567890123"), tn.getNationalNumber());
    assertEquals(32, tn.getCountryCode());
    LOGGER.debug("can have separators");
    tn = TelephoneNumber.of("+32(123)45-67-89");
    assertEquals(tn.getNationalNumber(), new BigInteger("123456789"));
    tn = TelephoneNumber.of("+1-958-555-4321");
    assertEquals(tn.getNationalNumber(), new BigInteger("9585554321"));
  }

  @Test
  public void testInvalidCountryCode() {
    assertThrows(new Closure() {
      @Override
      public void execute() throws Throwable {
        TelephoneNumber.of(1000, new BigInteger("1234567890"));
      }
    }, IllegalArgumentException.class, "Should throw IllegalArgumentException");
  }

  @Test
  public void testTooLongTelephoneNumber() {
    assertThrows(new Closure() {
      @Override
      public void execute() throws Throwable {
        TelephoneNumber.of("+32 12345678901234");
      }
    }, IllegalArgumentException.class, "Should throw IllegalArgumentException");
  }

  @Test
  public void testInvalidCharacters() {

    assertThrows(new Closure() {
      @Override
      public void execute() throws Throwable {
        TelephoneNumber.of("+32 abcdefg");
      }
    }, IllegalArgumentException.class, "Should throw IllegalArgumentException");

    assertThrows(new Closure() {
      @Override
      public void execute() throws Throwable {
        TelephoneNumber.of("+32 %1234)56789");
      }
    }, IllegalArgumentException.class, "Should throw IllegalArgumentException");
  }

}
