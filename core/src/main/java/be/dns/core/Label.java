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

import com.google.common.collect.ImmutableSet;
import com.ibm.icu.text.IDNA;
import com.ibm.icu.text.UnicodeSet;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

public abstract class Label {

  public static final Logger LOGGER = LoggerFactory.getLogger(Label.class);

  public static final int IDNA_OPTIONS;

  private static final IDNA IDNA;

  static {
    int options;
    Properties properties = new Properties();
    try {
      properties.load(TelephoneNumber.class.getResourceAsStream("icu4j.properties"));
      options = Integer.parseInt(properties.getProperty("idna.options"));
    } catch (IOException e) {
      LOGGER.debug("IOException. Defaulting to zero", e);
      options = 0;
    }
    IDNA_OPTIONS = options;
    IDNA = com.ibm.icu.text.IDNA.getUTS46Instance(IDNA_OPTIONS);
  }

  private static final String HYPHEN = "-";

  private final String value;

  public Label(String value) {
    this.value = value;
  }

  public static abstract class ASCIILabel extends Label {

    public static UnicodeSet ASCII_SET = new UnicodeSet().set(32, 127);

    private ASCIILabel(String value) {
      super(value);
      if (!ASCII_SET.containsAll(value)) throw new IllegalArgumentException();
    }

    public static abstract class LDHLabel extends ASCIILabel {

      public static UnicodeSet LDH_SET = new UnicodeSet().applyPattern("[A-Za-z0-9\\-]");

      public static class ReservedLDHLabel extends LDHLabel {

        private ReservedLDHLabel(String value) {
          super(value);
          if (!LDH_SET.containsAll(value)) throw new IllegalArgumentException();
        }

        public static class ALabel extends ReservedLDHLabel {

          private ALabel(String value) {
            super(value);
          }

          @Override
          public Label toUnicode() throws LabelException {
            IDNA.Info info = new IDNA.Info();
            StringBuilder result = new StringBuilder();
            IDNA.labelToUnicode(this.getStringValue(), result, info);
            if (info.hasErrors()) {
              throw new LabelException.IDNParseException(info.getErrors());
            }
            return Label.of(result.toString());

          }
        }

        public static class FakeALabel extends ReservedLDHLabel {

          private Set<IDNA.Error> errors;

          private FakeALabel(String value, Set<IDNA.Error> errors) {
            super(value);
            this.errors = new ImmutableSet.Builder<IDNA.Error>().addAll(errors).build();
          }

          public Set<IDNA.Error> getErrors() {
            return errors;
          }
        }

      }

      public static class NonReservedLDHLabel extends LDHLabel {

        private NonReservedLDHLabel(String value) {
          super(value);
        }

      }

      private LDHLabel(String value) {
        super(value);
      }

    }

    public static class NONLDHLabel extends ASCIILabel {

      private NONLDHLabel(String value) {
        super(value);
      }

    }

  }

  public static abstract class NonASCIILabel extends Label {

    private NonASCIILabel(String value) {
      super(value);
    }


    public static class ULabel extends NonASCIILabel {

      private ULabel(String value) {
        super(value);
      }

      @Override
      public Label toLDH() throws LabelException.IDNParseException {
        IDNA.Info info = new IDNA.Info();
        StringBuilder result = new StringBuilder();
        IDNA.labelToASCII(getStringValue(), result, info);
        if (info.hasErrors()) {
          throw new LabelException.IDNParseException(info.getErrors());
        }
        return Label.of(result.toString());
      }
    }

  }


  public static class RootLabel extends Label {

    private static final RootLabel instance = new RootLabel();

    private RootLabel() {
      super("");
    }

    public static RootLabel getInstance() {
      return instance;
    }

  }

  public static Label of(final String label) throws LabelException {
    if (StringUtils.isEmpty(label)) return RootLabel.getInstance();
    if (!ASCIILabel.ASCII_SET.containsAll(label)) {
      IDNA.Info info = new IDNA.Info();
      StringBuilder result = new StringBuilder();
      IDNA.labelToUnicode(label, result, info);
      if (info.hasErrors()) {
        throw new LabelException.IDNParseException(info.getErrors());
      }
      return new NonASCIILabel.ULabel(result.toString());
    }
    // label contains ascii characters
    if (!ASCIILabel.LDHLabel.LDH_SET.containsAll(label) || label.startsWith(HYPHEN) || label.endsWith(HYPHEN)) {
      return new ASCIILabel.NONLDHLabel(label);
    }
    // label contains LDH characters and doesn't start or end with with hyphen
    if (!label.matches("^..--.*")) {
      return new ASCIILabel.LDHLabel.NonReservedLDHLabel(label);
    }
    // label is reserved
    if (label.startsWith("xn")) {
      Label aLabel = new ASCIILabel.LDHLabel.ReservedLDHLabel.ALabel(label);
      try {
        aLabel.toUnicode();
        return aLabel;
      } catch (LabelException.IDNParseException e) {
        return new ASCIILabel.LDHLabel.ReservedLDHLabel.FakeALabel(label, e.getErrors());
      }
    }
    return new ASCIILabel.LDHLabel.ReservedLDHLabel(label);
  }

  public String getStringValue() {
    return value;
  }

  public Label toLDH() {
    return this;
  }

  public Label toUnicode() {
    return this;
  }

}
