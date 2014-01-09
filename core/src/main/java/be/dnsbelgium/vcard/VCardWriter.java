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

import java.io.IOException;
import java.io.OutputStream;

public class VCardWriter {

  private final OutputStream outputStream;

  private static final String CHARSET = "UTF-8";

  private static final int MAX_LENGTH = 75;

  private final byte[] line = new byte[MAX_LENGTH];

  private int pos = 0;

  public VCardWriter(OutputStream outputStream) {
    this.outputStream = outputStream;
  }

  public void write(String text) throws IOException {

    outputStream.write(text.getBytes(CHARSET));
  }

  public synchronized void fold() throws IOException {
    outputStream.write(line, 0, pos);
    outputStream.write(Characters.CRLF.getBytes(CHARSET));
    outputStream.write(Characters.SPACE.getBytes(CHARSET));
    pos = 1;
  }
}
