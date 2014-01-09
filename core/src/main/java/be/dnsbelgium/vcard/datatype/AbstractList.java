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
package be.dnsbelgium.vcard.datatype;

import com.google.common.collect.ImmutableList;

import java.util.List;

public abstract class AbstractList<T extends Value> implements Value {

  public final List<T> values;

  public AbstractList(List<T> values) {
    if (values == null) {
      this.values = null;
    } else {
      this.values = new ImmutableList.Builder<T>().addAll(values).build();
    }
  }

  public List<T> getValues() {
    return values;
  }

  public static class TextList extends AbstractList<Text> {

    public TextList(List<Text> values) {
      super(values);
    }

    public static TextList of(List<String> values) {
      if (values == null) {
        return new TextList(null);
      }
      ImmutableList.Builder<Text> builder = new ImmutableList.Builder<Text>();
      for (String value : values) {
        builder.add(new Text(value));
      }
      return new TextList(builder.build());
    }

    @Override
    public String getTypeName() {
      return "text";
    }
  }
}
