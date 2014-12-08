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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * E.164 Telephone Number
 *
 * Immutable, thread safe
 */
public final class TelephoneNumber {

  private static final Logger LOGGER = LoggerFactory.getLogger(TelephoneNumber.class);

  private static final Properties COUNTRY_CODES;

  static {
    COUNTRY_CODES = new Properties();
    try {
      COUNTRY_CODES.load(TelephoneNumber.class.getResourceAsStream("country_calling_codes.csv"));
    } catch (IOException e) {
      // should never occur
      LOGGER.error("Could not load country calling codes", e);
    }
  }

  public static String getRegion(int countryCode) {
    return COUNTRY_CODES.getProperty(Integer.toString(countryCode));
  }

  private final int countryCode;

  private final BigInteger nationalNumber;

  private TelephoneNumber(int countryCode, BigInteger nationalNumber) {
    checkArgument(countryCode >= 0 && countryCode < 1000, "countryCode %s must be between 0 and 999", countryCode);
    checkArgument((countryCode + nationalNumber.toString()).length() <= 15, "TelephoneNumber maximum 15 digits long");
    checkArgument(TelephoneNumber.getRegion(countryCode) != null, "unknown countryCode: %s", countryCode);
    this.countryCode = countryCode;
    this.nationalNumber = new BigInteger(nationalNumber.toByteArray());
  }


  public static TelephoneNumber of(String number) {
    String normalized = number.replaceAll("[\\(\\)\\.\\s-_]", "");
    LOGGER.debug("Normalized telephone number: {}", normalized);
    if (!normalized.matches("^\\+?\\d+$")) {
      throw new IllegalArgumentException("bad number");
    }
    String prefix = "", suffix = "";
    if (normalized.startsWith("+")) {
      for (int i = 1; i <= 3; i++) {
        prefix = normalized.substring(1, i);
        suffix = normalized.substring(i);
        LOGGER.debug("prefix: {} suffix: {}", prefix, suffix);
        if (COUNTRY_CODES.containsKey(prefix)) {
          break;
        }
      }
    } else {
      prefix = "0";
      suffix = normalized;
    }
    return new TelephoneNumber(Integer.valueOf(prefix), new BigInteger(suffix));
  }

  public static TelephoneNumber of(int countryCode, BigInteger nationalNumber) {
    return new TelephoneNumber(countryCode, nationalNumber);
  }

  public int getCountryCode() {
    return countryCode;
  }

  public BigInteger getNationalNumber() {
    return new BigInteger(nationalNumber.toByteArray());
  }

  public String getRegion() {
    return TelephoneNumber.getRegion(this.countryCode);
  }

  public String getStringValue() {
    return "+" + countryCode + "." + nationalNumber;
  }

  @Override
  public String toString() {
    return "countryCode=[" + countryCode + "] nationalNumber=[" + nationalNumber + "]";
  }
}
