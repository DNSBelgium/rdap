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

import be.dnsbelgium.vcard.Contact;

import java.util.Arrays;
import java.util.List;

public abstract class StructuredValue<T extends Value> implements Value {

  public static class Component<T extends Value> {
    public final String name;

    public final T value;

    public Component(String name, T value) {
      this.name = name;
      this.value = value;
    }
  }

  private final Component[] components;

  public StructuredValue(Component<T>... components) {
    this.components = components;
  }

  public Component[] getComponents() {
    return components.clone();
  }

  public static class NType extends StructuredValue<AbstractList.TextList> {


    public NType(Component<AbstractList.TextList>... components) {
      super(components);
    }


    public NType(List<String> familyNames, List<String> givenNames, List<String> additionalNames, List<String> prefixes, List<String> suffixes) {
      super(new Component[]{
          new Component<AbstractList.TextList>(Contact.Property.N.ComponentName.FAMILY_NAMES.name(), AbstractList.TextList.of(familyNames)),
          new Component<AbstractList.TextList>(Contact.Property.N.ComponentName.GIVEN_NAMES.name(), AbstractList.TextList.of(givenNames)),
          new Component<AbstractList.TextList>(Contact.Property.N.ComponentName.ADDITIONAL_NAMES.name(), AbstractList.TextList.of(additionalNames)),
          new Component<AbstractList.TextList>(Contact.Property.N.ComponentName.HONORIFIC_PREFIXES.name(), AbstractList.TextList.of(prefixes)),
          new Component<AbstractList.TextList>(Contact.Property.N.ComponentName.HONORIFIC_SUFFIXES.name(), AbstractList.TextList.of(suffixes))

      });
    }

    @Override
    public String getTypeName() {
      return "text";
    }
  }

  public static class ORGType extends StructuredValue<Text> {

    public ORGType(Component<Text>... components) {
      super(components);
    }

    public static ORGType of(String name, String... units) {
      if (units == null) {
        units = new String[0];
      }
      Component<Text>[] components = new Component[units.length + 1];
      components[0] = new Component<Text>("name", Text.of(name));
      for (int i = 0; i < units.length; i++) {
        components[i + 1] = new Component<Text>("unit", Text.of(units[i]));
      }
      return new ORGType(components);
    }

    @Override
    public String getTypeName() {
      return "text";
    }
  }

  public static class ADRType extends StructuredValue<AbstractList.TextList> {

    public ADRType(Component<AbstractList.TextList>... components) {
      super(components);
    }

    public ADRType(List<String> pobox, List<String> ext, List<String> street, List<String> locality, List<String> region, List<String> code, List<String> country) {
      super(new Component[]{
          new Component<AbstractList.TextList>("pobox", AbstractList.TextList.of(pobox)),
          new Component<AbstractList.TextList>("ext", AbstractList.TextList.of(ext)),
          new Component<AbstractList.TextList>("street", AbstractList.TextList.of(street)),
          new Component<AbstractList.TextList>("locality", AbstractList.TextList.of(locality)),
          new Component<AbstractList.TextList>("region", AbstractList.TextList.of(region)),
          new Component<AbstractList.TextList>("code", AbstractList.TextList.of(code)),
          new Component<AbstractList.TextList>("country", AbstractList.TextList.of(country))
      });
    }

    public static ADRType of(String pobox, String ext, String street, String locality, String region, String code, String country) {
      return new ADRType(Arrays.asList(pobox), Arrays.asList(ext), Arrays.asList(street), Arrays.asList(locality), Arrays.asList(region), Arrays.asList(code), Arrays.asList(country));
    }

    @Override
    public String getTypeName() {
      return "text";
    }
  }

}
