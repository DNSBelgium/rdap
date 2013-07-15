package be.dns.rdap.jackson;

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

import be.dns.rdap.core.Status;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;
import java.util.Locale;

public class StatusSerializer extends JsonSerializer<Status> {

  @Override
  public Class<Status> handledType() {
    return Status.class;
  }

  @Override
  public void serialize(Status value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
    if (value == null)
      jgen.writeNull();
    else if (value.getValue() == null)
      jgen.writeNull();
    else
      jgen.writeString(value.getValue().toLowerCase(Locale.ENGLISH));
  }
}
