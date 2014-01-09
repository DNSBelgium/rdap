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
package be.dnsbelgium.rdap.client;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import java.util.Locale;

public class RDAPOptions extends Options {

  public static final String URL = "url";
  public static final String TRUSTSTORE_PASS = "truststorepass";
  public static final String DEFAULT_PASS = "changeit";
  public static final String DEFAULT_STORETYPE = "JKS";
  public static final String KEYSTORE_PASS = "keystorepass";
  public static final String KEYSTORE = "keystore";
  public static final String KEYPASS = "keypass";
  public static final String USERNAME = "username";
  public static final String PASSWORD = "password";
  public static final String KEYSTORE_TYPE = "keystoretype";
  public static final String TRUSTSTORE_TYPE = "truststoretype";
  public static final String RAW = "raw";
  public static final String PRETTY = "pretty";
  public static final String LANG = "lang";
  public static final String TRUSTSTORE = "truststore";
  private static final long serialVersionUID = 3032718269550334306L;

  public RDAPOptions(Locale locale) {
    super();
    addOption(null, "version", false, "display version, authors and licensing information.");
    addOption(null, "help", false, "display a short help text.");
    addOption(OptionBuilder.withLongOpt("config")
        .withDescription("uses FILE as a configuration file instead of the default.")
        .hasArg()
        .withArgName("FILE")
        .create("c"));
    addOption(OptionBuilder.withLongOpt(URL)
        .withDescription("overrides any hosts in the configuration file and queries HOST directly.")
        .hasArg()
        .withArgName("URL")
        .create("u"));
    addOption(OptionBuilder.withLongOpt(USERNAME)
        .withDescription("Specify a username to be used with Basic Authentication.")
        .hasArg()
        .withArgName("USERNAME")
        .create());
    addOption(OptionBuilder.withLongOpt(KEYSTORE)
        .withDescription("Tells rdap to use the specified keystore file when getting info with RDAP")
        .hasArg()
        .withArgName("FILE")
        .create());
    addOption(OptionBuilder.withLongOpt(KEYSTORE_TYPE)
        .withDescription(String.format("Type of the keystore. Either JKS or PKCS12, default is %s", DEFAULT_STORETYPE))
        .hasArg()
        .withArgName("TYPE")
        .create());
    addOption(OptionBuilder.withLongOpt(KEYSTORE_PASS)
        .withDescription(String.format("Tells rdap to use the specified keystore file when getting info with RDAP. Default value is changeit", DEFAULT_PASS))
        .hasArg()
        .withArgName("PASSWORD")
        .create());
    addOption(OptionBuilder.withLongOpt(KEYPASS)
        .withDescription(String.format("Tells rdap to use the specified key password when getting info with RDAP. Default value is %s", DEFAULT_PASS))
        .hasArg()
        .withArgName("PASSWORD")
        .create());
    addOption(OptionBuilder.withLongOpt(TRUSTSTORE)
        .withDescription("Tells curl to use the specified certificate file to verify the peer. The file may contain multiple CA certificates. The certificate(s) must be\n" +
            " in PEM format.")
        .hasArg()
        .withArgName("FILE")
        .create());
    addOption(OptionBuilder.withLongOpt(TRUSTSTORE_TYPE)
        .withDescription("Type of the truststore. Either JKS or PKCS12")
        .hasArg()
        .withArgName("TYPE")
        .create());
    addOption(OptionBuilder.withLongOpt(TRUSTSTORE_PASS)
        .withDescription(String.format("Tells rdap to use the specified keystore file when getting info with RDAP. Default value is %s", DEFAULT_PASS))
        .hasArg()
        .withArgName("PASSWORD")
        .create());
    addOption(OptionBuilder.withLongOpt("insecure")
        .withDescription("This option explicitly allows RDAP to perform \"insecure\" SSL connections")
        .create("i"));
    addOption(OptionBuilder.withLongOpt(RAW)
        .withDescription("Causes rdap to emit raw (not pretty-printed) JSON rather than text output.")
        .create());
    addOption(OptionBuilder.withLongOpt(PRETTY)
        .withDescription("Causes rdap to emit pretty-printed JSON rather than text output.")
        .create());
    addOption(OptionBuilder.withLongOpt("location")
        .withDescription("If  the server reports that the requested page has moved to a different location (indicated with a Location: header and a 3XX response\n" +
            " code), this option will make curl redo the request on the new place")
        .create("l"));
    addOption(OptionBuilder.withLongOpt(PASSWORD)
        .withDescription("Specify a password to be used with Basic Authentication.")
        .hasArg()
        .withArgName("PASSWORD")
        .create());
    addOption(OptionBuilder.withLongOpt(LANG)
        .withDescription("Specify a language. This is sent to the server using the Accept-Language header. If unset, the language will be taken from your $LANG environment variable (or en if that is not defined).")
        .hasArg()
        .withArgName("LANG")
        .create());
  }
}
