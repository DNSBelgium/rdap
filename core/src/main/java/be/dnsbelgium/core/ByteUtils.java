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

public final class ByteUtils {

  private ByteUtils() {

  }

  /**
   * Create a bit mask of size max_m with the m left most bits set to 1
   * @param m
   * @param max_m
   * @return
   */
  public static byte[] getMask(int m, int max_m) {
    byte[] b = new byte[max_m / 8];
    for (int i = 0; i < b.length; i++) {
      b[i] = (byte) 0xff;
    }
    int j = b.length - 1;
    int k = 1;
    for (int i = 1; i <= max_m - m; i++) {
      b[j] = (byte) ((b[j] << 1) & 0xff);
      k++;
      if (k > 8) {
        k = 1;
        j--;
      }
    }
    return b;
  }

  public static String byteArrayToString(byte[] b_array, boolean spaces) {
    String result = "";
    for (byte b : b_array) {
      result += (spaces ? "\u0020" : "") + byteToString(b);
    }
    return result.trim();
  }

  public static String byteToString(byte b) {
    StringBuffer sb = new StringBuffer();
    for (int mask = 0x01; mask != 0x100; mask <<= 1) {
      boolean value = (b & mask) != 0;
      sb.insert(0, (value ? "1" : "0"));
    }
    return sb.toString();
  }

  public static byte[] and(byte[] b1, byte[] b2) {
    if (b1.length != b2.length) {
      throw new IllegalArgumentException("arrays should have isSame size");
    }
    byte[] result = new byte[b1.length];
    for (int i = 0; i < b1.length; i++) {
      result[i] = (byte) (b1[i] & b2[i]);
    }
    return result;
  }

  public static boolean isSame(byte[] b1, byte[] b2) {
    if (b1.length != b2.length) {
      throw new IllegalArgumentException("arrays should have isSame size");
    }
    for (int i = 0; i < b1.length; i++) {
      if (b1[i] != b2[i]) {
        return false;
      }
    }
    return true;
  }
}
