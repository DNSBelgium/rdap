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

import java.net.InetAddress;
import java.net.UnknownHostException;

public class CIDR {

  private final byte[] address;

  private final int size;

  private final byte[] mask;

  public CIDR(byte[] address, int size) {
    if (size < 1 || size > address.length * 8) {
      throw new IllegalArgumentException();
    }
    // check whether least significant bits are zero
    byte[] mask = ByteUtils.getMask(size, address.length * 8);
    byte[] maskedAddress = ByteUtils.and(address, mask);

    if (!ByteUtils.equals(address, maskedAddress)) {
      throw new IllegalArgumentException("Invalid address");
    }
    this.address = address.clone();
    this.size = size;
    this.mask = mask;
  }

  public byte[] getAddress() {
    return address.clone();
  }

  public int getSize() {
    return size;
  }

  public byte[] getMask() {
    return mask.clone();
  }

  public boolean contains(InetAddress inet) {
    byte[] inetaddr = inet.getAddress();
    if (inetaddr.length != address.length) {
      throw new IllegalArgumentException("argument is other type of inet address");
    }
    byte[] result = ByteUtils.and(inetaddr, mask);
    return ByteUtils.equals(this.address, result);
  }

  public static CIDR of(String cidr) {
    String[] parts = cidr.split("\\/");
    if (parts.length == 1) {
      try {
        InetAddress address = InetAddress.getByName(parts[1]);
        return new CIDR(address.getAddress(), address.getAddress().length);
      } catch (UnknownHostException e) {
        throw new IllegalArgumentException(e);
      }
    }
    if (parts.length != 2) {
      throw new IllegalArgumentException("Invalid format");
    }
    try {
      InetAddress address = InetAddress.getByName(parts[0]);
      return new CIDR(address.getAddress(),Integer.parseInt(parts[1]));
    } catch (UnknownHostException e) {
      throw new IllegalArgumentException(e);
    }
  }

}
