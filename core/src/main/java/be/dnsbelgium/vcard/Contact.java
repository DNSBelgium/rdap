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

import be.dnsbelgium.core.TelephoneNumber;
import be.dnsbelgium.vcard.datatype.AbstractList;
import be.dnsbelgium.vcard.datatype.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * <p>Usage:</p>
 * 
 * <pre>
 * Contact c = Contact.of(
 *   Contact.Property("fn", Text.of("John Doe")),
 *   Contact.Property("email", Text.of("jdoe@example.com")),
 *   Contact.Property("fax", Tel.of("+32.123456789"))
 * );
 * </pre>
 */
public class Contact {

  public static Contact of(Property... properties) {
    return new Contact(Lists.newArrayList(properties));
  }

  public static abstract class Parameters {

    public abstract Set<String> get(String key);

    public abstract Iterator<String> keys();

    public static Parameters of(Map<String, Set<String>> parameters) {
      final ImmutableMap.Builder<String, Set<String>> builder = new ImmutableMap.Builder<String, Set<String>>();

      for (Map.Entry<String, Set<String>> entry: parameters.entrySet()) {
        builder.put(entry.getKey(), new ImmutableSet.Builder<String>().addAll(entry.getValue()).build());
      }

      return new Builder.MapParameters(builder.build());
    }

    public static class Builder {

      private Map<String, Set<String>> parameters = new HashMap<String, Set<String>>();

      public Builder add(String key, String value) {
        if (value.contains("\"")) {
          throw new IllegalArgumentException("value must not contain DQUOTE");
        }
        if (!parameters.containsKey(key)) {
          parameters.put(key, new HashSet<String>());
        }
        parameters.get(key).add(value);
        return this;
      }

      public Builder remove(String key, String value) {
        if (parameters.containsKey(key)) {
          parameters.get(key).remove(value);
        }
        return this;
      }

      public Builder addLanguage(String lang) {
        return add("LANGUAGE", lang);
      }

      public Builder addPref(int pref) {
        if (pref < 1 || pref > 100) {
          throw new IllegalArgumentException("pref must be an integer between 1 and 100");
        }
        return add("PREF", "" + pref);
      }

      public Parameters build() {
        final ImmutableMap.Builder<String, Set<String>> builder = new ImmutableMap.Builder<String, Set<String>>();
        Iterator<String> it = this.parameters.keySet().iterator();
        while (it.hasNext()) {
          String key = it.next();
          builder.put(key, new ImmutableSet.Builder<String>().addAll(parameters.get(key).iterator()).build());
        }

        return new MapParameters(builder.build());
      }

      public static class MapParameters extends Parameters {

        private MapParameters(Map<String, Set<String>> map) {
          this.map = map;
        }

        private final Map<String, Set<String>> map;


        @Override
        public Set<String> get(String key) {
          return map.get(key);
        }

        @Override
        public Iterator<String> keys() {
          return map.keySet().iterator();
        }
      }
    }
  }

  public static class Property<T extends Value> {

    private final String group;

    private final String name;

    private final Parameters parameters;

    private final T value;

    public static Property of(String group, String name, Parameters parameters, Value value) {
      return new Property(group, name, parameters, value);
    }

    public static Property of(String name, Parameters parameters, Value value) {
      return new Property(null, name, parameters, value);
    }

    public static Property of(String name, Value value) {
      return new Property(null, name, null, value);
    }


    public Property(String group, String name, Parameters parameters, T value) {
      if (group != null && !group.matches("^[a-zA-Z0-9\\-]+$")) {
        throw new IllegalArgumentException("group can only contain alpha / digit / hyphen");
      }
      if (name == null) {
        throw new IllegalArgumentException("name must not be null");
      }
      if (!name.matches("^[a-zA-Z0-9\\-]+$")) {
        throw new IllegalArgumentException("name can only contain alpha / digit / hyphen");
      }
      this.group = group;
      this.name = name;
      this.parameters = parameters;
      this.value = value;
    }

    public T getValue() {
      return value;
    }

    public String getGroup() {
      return group;
    }

    public String getName() {
      return name;
    }

    public Parameters getParameters() {
      return parameters;
    }

    public static class FN extends Property<Text> {

      public FN(String group, Parameters parameters, String value) {
        super(group, "FN", parameters, new Text(value));
      }
    }

    public static class N extends Property<StructuredValue.NType> {

      public static enum ComponentName {
        FAMILY_NAMES, GIVEN_NAMES, ADDITIONAL_NAMES, HONORIFIC_PREFIXES, HONORIFIC_SUFFIXES
      }

      public N(String group, Parameters params, List<String> family, List<String> given, List<String> additional, List<String> prefixes, List<String> suffixes) {
        super(group, "N", null, new StructuredValue.NType(
            new StructuredValue.Component<AbstractList.TextList>(ComponentName.FAMILY_NAMES.name(), AbstractList.TextList.of(family)),
            new StructuredValue.Component<AbstractList.TextList>(ComponentName.GIVEN_NAMES.name(), AbstractList.TextList.of(given)),
            new StructuredValue.Component<AbstractList.TextList>(ComponentName.ADDITIONAL_NAMES.name(), AbstractList.TextList.of(additional)),
            new StructuredValue.Component<AbstractList.TextList>(ComponentName.HONORIFIC_PREFIXES.name(), AbstractList.TextList.of(prefixes)),
            new StructuredValue.Component<AbstractList.TextList>(ComponentName.HONORIFIC_SUFFIXES.name(), AbstractList.TextList.of(suffixes))
        ));
      }

      public N(String family, String given, String additional, String prefix, String suffix) {
        this(null, null, Arrays.asList(family), Arrays.asList(given), Arrays.asList(additional), Arrays.asList(prefix), Arrays.asList(suffix));
      }


    }

  }

  private List<Property> properties;

  public Contact(List<Property> properties) {
    if (properties == null) {
      throw new IllegalArgumentException("properties must not be null");
    }
    //if (Contact.getProperty(properties, "fn") == null) throw new IllegalArgumentException("properties should at least contain FN");
    this.properties = new ImmutableList.Builder<Property>().addAll(properties).build();
  }

  public List<Property> getProperties() {
    return properties;
  }


  /**
   * Returns all properties with the provided name
   *
   * @param name case insensitive name
   * @return a list of properties having the isSame name
   */
  public List<Property> getProperties(String name) {
    if (name == null) {
      throw new IllegalArgumentException("name should not be null");
    }
    List<Property> plist = new ArrayList<Property>();
    for (Property property : properties) {
      if (property.name.equalsIgnoreCase(name)) {
        plist.add(property);
      }
    }
    return plist;
  }

  public static class Builder {
    private static final Logger LOGGER = LoggerFactory.getLogger(Builder.class);
    private String formattedName;
    private String givenName;
    private String familyName;
    private String organization;
    private List<String> organizationalUnits = new ArrayList<String>();
    private List<String> street = new ArrayList<String>();
    private List<String> locality = new ArrayList<String>();
    private List<String> region = new ArrayList<String>();
    private List<String> postalCode = new ArrayList<String>();
    private List<String> country = new ArrayList<String>();
    private List<TelephoneNumber> telephoneNumbers = new ArrayList<TelephoneNumber>();
    private List<TelephoneNumber> faxNumbers = new ArrayList<TelephoneNumber>();
    private List<String> emailAddresses = new ArrayList<String>();
    private List<URI> contactURIs = new ArrayList<URI>();
    private String[] languages = new String[0];

    public Builder setFormattedName(String formattedName) {
      this.formattedName = formattedName;
      return this;
    }

    public Builder setGivenName(String givenName) {
      this.givenName = givenName;
      return this;
    }

    public Builder setFamilyName(String familyName) {
      this.familyName = familyName;
      return this;
    }

    public Builder setOrganization(String organization) {
      this.organization = organization;
      return this;
    }

    public Builder setStreet(String street) {
      this.street.clear();
      this.street.add(street);
      return this;
    }

    public Builder setLocality(String locality) {
      this.locality.clear();
      this.locality.add(locality);
      return this;
    }

    public Builder setRegion(String region) {
      this.region.clear();
      this.region.add(region);
      return this;
    }

    public Builder setPostalCode(String postalCode) {
      this.postalCode.clear();
      this.postalCode.add(postalCode);
      return this;
    }

    public Builder setCountry(String country) {
      this.country.clear();
      this.country.add(country);
      return this;
    }

    public Builder setLanguages(String... languages) {
      this.languages = languages;
      return this;
    }

    public Builder addTelephoneNumber(TelephoneNumber telephoneNumber) {
      telephoneNumbers.add(telephoneNumber);
      return this;
    }

    public Builder addFaxNumber(TelephoneNumber faxNumber) {
      this.faxNumbers.add(faxNumber);
      return this;
    }

    public Builder addEmailAddress(String emailAddress) {
      this.emailAddresses.add(emailAddress);
      return this;
    }
    
    public Builder addContactURI(URI contactURI) {
      this.contactURIs.add(contactURI);
      return this;
    }

    public Contact build() {
      List<Property> properties = new ArrayList<Property>();
      if (!StringUtils.isEmpty(this.familyName) || !StringUtils.isEmpty(givenName)) {
        properties.add(new Property.N(this.familyName, this.givenName, null, null, null));
      }
      if (!StringUtils.isEmpty(this.formattedName)) {
        properties.add(new Property.FN(null, null, this.formattedName));
      }
      if (!this.street.isEmpty() || !this.locality.isEmpty() || !this.region.isEmpty() || !this.postalCode.isEmpty() || !this.country.isEmpty()) {
        properties.add(
            new Property(
                null,
                "ADR",
                null,
                new StructuredValue.ADRType(
                    null,
                    null,
                    this.street,
                    this.locality,
                    this.region,
                    this.postalCode,
                    this.country)));
      }
      if (!StringUtils.isEmpty(this.organization)) {
        properties.add(new Property(null, "ORG", null, StructuredValue.ORGType.of(this.organization, this.organizationalUnits.toArray(new String[this.organizationalUnits.size()]))));
      }
      for (TelephoneNumber tel : telephoneNumbers) {
        try {
          properties.add(new Property(null, "TEL", new Parameters.Builder().add("type", "voice").build(), new Tel(tel)));
        } catch (URISyntaxException e) {
          LOGGER.error("Could not add voice property", e);
        }
      }
      for (TelephoneNumber fax : faxNumbers) {
        try {
          properties.add(new Property(null, "TEL", new Parameters.Builder().add("type", "fax").build(), new Tel(fax)));
        } catch (URISyntaxException e) {
          LOGGER.error("Could not add fax property", e);
        }
      }
      for (String email : emailAddresses) {
        properties.add(new Property(null, "EMAIL", null, new Text(email)));
      }
      for (URI contactURI : contactURIs) {
        properties.add(new Property(null, "CONTACT-URI", null, new URIValue(contactURI)));
      }
      for (int i = 0; i < languages.length; i++) {
        String lang = languages[i];
        properties.add(new Property(null, "LANG", new Parameters.Builder().add("PREF", (i + 1) + "").build(), new LanguageTag(lang)));
      }
      return new Contact(properties);
    }

    public Builder addStreet(String street) {
      if (StringUtils.isEmpty(street)) {
        return this;
      }
      if (this.street == null) {
        this.street = new ArrayList<String>();
      }
      this.street.add(street);
      return this;
    }

    public Builder addOU(String organizationalUnit) {
      if (StringUtils.isEmpty(organizationalUnit)) {
        return this;
      }
      if (this.organizationalUnits == null) {
        this.organizationalUnits = new ArrayList<String>();
      }
      this.organizationalUnits.add(organizationalUnit);
      return this;
    }

    public Builder addLocality(String locality) {
      if (StringUtils.isEmpty(locality)) {
        return this;
      }
      if (this.locality == null) {
        this.locality = new ArrayList<String>();
      }
      this.locality.add(locality);
      return this;
    }

    public Builder addRegion(String region) {
      if (StringUtils.isEmpty(region)) {
        return this;
      }
      if (this.region == null) {
        this.region = new ArrayList<String>();
      }
      this.region.add(region);
      return this;
    }

    public Builder addPostalCode(String postalCode) {
      if (StringUtils.isEmpty(postalCode)) {
        return this;
      }
      if (this.postalCode == null) {
        this.postalCode = new ArrayList<String>();
      }
      this.postalCode.add(postalCode);
      return this;
    }

    public Builder addCountry(String country) {
      if (StringUtils.isEmpty(country)) {
        return this;
      }
      if (this.country == null) {
        this.country = new ArrayList<String>();
      }
      this.country.add(country);
      return this;
    }
  }
}
