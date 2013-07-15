package be.dns.vcard.datatype;

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

import be.dns.core.TelephoneNumber;
import com.google.common.collect.Lists;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Property Value as specified in <a href="http://tools.ietf.org/html/rfc6350#section-4">[RFC6350], Section 4</a>
 */
public interface Value {

  public final static class Factory {

    public static Value of(String value) {
      return Text.of(value);
    }

    public static Value of(TelephoneNumber value) throws URISyntaxException {
      return new Tel(value);
    }

    public static Value of(URI value) {
      return new URIValue(value);
    }

    public static Value of(Iterable<String> value) {
      List<Text> list = new ArrayList<Text>();
      for (String str : value) list.add(Text.of(str));
      return new AbstractList.TextList(list);
    }

  }

  public String getTypeName();


}