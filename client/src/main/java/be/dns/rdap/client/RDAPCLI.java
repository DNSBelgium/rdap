package be.dns.rdap.client;

/*
 * #%L
 * Client
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

import be.dns.core.DomainName;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.util.DefaultPrettyPrinter;
import org.fusesource.jansi.AnsiConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashSet;
import java.util.Locale;

/**
 * Command line RDAP client (Command Line Interface)
 */
public class RDAPCLI {

  private static final Logger LOGGER = LoggerFactory.getLogger(RDAPCLI.class);

  public static enum Type {
    DOMAIN, ENTITY, NAMESERVER, AUTNUM, IP
  }


  public static void main(String[] args) throws InterruptedException {

    AnsiConsole.systemInstall();

    LOGGER.debug("Create the command line parser");
    CommandLineParser parser = new GnuParser();

    LOGGER.debug("Create the options");
    Options options = new RDAPOptions(Locale.ENGLISH);

    try {
      LOGGER.debug("Parse the command line arguments");
      CommandLine line = parser.parse(options, args);

      if (line.hasOption("help")) {
        HelpFormatter hf = new HelpFormatter();
        hf.printHelp("rdap [ OPTIONS ]... [ QUERY ] [ TYPE ]\n", null /* header */, options, null /* footer */, true);
        System.out.println();
        return;
      }

      if (line.getArgs().length == 0) {
        throw new IllegalArgumentException("You must provide a query");
      }
      String query = line.getArgs()[0];

      Type type = (line.getArgs().length == 2) ? Type.valueOf(line.getArgs()[1].toUpperCase()) : guess(query);

      LOGGER.debug("Query: {}, Type: {}", query, type);

      try {
        URL url = new URL(line.getOptionValue(RDAPOptions.URL));
        int port = url.getPort();
        if (port == -1 && url.getProtocol().equalsIgnoreCase("http")) port = 80;
        if (port == -1 && url.getProtocol().equalsIgnoreCase("https"))
          port = 443;

        KeyStore trustStore = line.hasOption(RDAPOptions.TRUSTSTORE)
            ? RDAPClient.getKeyStoreFromFile(new File(line.getOptionValue(RDAPOptions.TRUSTSTORE)), line.getOptionValue(RDAPOptions.TRUSTSTORE_TYPE, RDAPOptions.DEFAULT_STORETYPE), line.getOptionValue(RDAPOptions.TRUSTSTORE_PASS, RDAPOptions.DEFAULT_PASS))
            : null;
        KeyStore keyStore = line.hasOption(RDAPOptions.KEYSTORE)
            ? RDAPClient.getKeyStoreFromFile(new File(line.getOptionValue(RDAPOptions.KEYSTORE)), line.getOptionValue(RDAPOptions.KEYSTORE_TYPE, RDAPOptions.DEFAULT_STORETYPE), line.getOptionValue(RDAPOptions.KEYSTORE_PASS, RDAPOptions.DEFAULT_PASS))
            : null;
        SSLSocketFactory socketFactory = new SSLSocketFactory(keyStore, line.getOptionValue(RDAPOptions.KEYPASS, RDAPOptions.DEFAULT_PASS), trustStore);


        Scheme scheme = new Scheme("https", port, socketFactory);

        SchemeRegistry registry = new SchemeRegistry();
        registry.register(scheme);
        DefaultHttpClient httpClient = new DefaultHttpClient(new BasicClientConnectionManager(registry));
        httpClient.getParams().setParameter(ClientPNames.DEFAULT_HOST,
            new HttpHost(url.getHost(), url.getPort(), url.getProtocol()));

        if (line.hasOption(RDAPOptions.USERNAME) && line.hasOption(RDAPOptions.PASSWORD)) {
          httpClient.getCredentialsProvider().setCredentials(
              new AuthScope(url.getHost(), port),
              new UsernamePasswordCredentials(line.getOptionValue(RDAPOptions.USERNAME), line.getOptionValue(RDAPOptions.PASSWORD)));
        }

        HashSet<Header> headers = new HashSet<Header>();
        headers.add(new BasicHeader("Accept-Language", line.getOptionValue(RDAPOptions.LANG, Locale.getDefault().toString())));

        httpClient.getParams().setParameter(ClientPNames.DEFAULT_HEADERS, headers);

        RDAPClient rdapClient = new RDAPClient(httpClient);
        if (line.hasOption(RDAPOptions.RAW)) {
          ObjectMapper mapper = new ObjectMapper();
          System.out.println(mapper.writer().writeValueAsString(rdapClient.getDomainAsJson(query)));
        } else if (line.hasOption(RDAPOptions.PRETTY)) {
          ObjectMapper mapper = new ObjectMapper();
          System.out.println(mapper.writer(new DefaultPrettyPrinter()).writeValueAsString(rdapClient.getDomainAsJson(query)));
        }
      } catch (CertificateException e) {
        LOGGER.error(e.getMessage(), e);
      } catch (UnrecoverableKeyException e) {
        LOGGER.error(e.getMessage(), e);
      } catch (NoSuchAlgorithmException e) {
        LOGGER.error(e.getMessage(), e);
      } catch (KeyStoreException e) {
        LOGGER.error(e.getMessage(), e);
      } catch (KeyManagementException e) {
        LOGGER.error(e.getMessage(), e);
      } catch (RDAPClient.RDAPClientException e) {
        LOGGER.error(e.getMessage(), e);
      } catch (IOException e) {
        LOGGER.error(e.getMessage(), e);
      } finally {

      }


    } catch (org.apache.commons.cli.ParseException e) {
      e.printStackTrace();
    }
  }

  public static Type guess(String query) {
    try {
      if (query.matches("^\\d+$")) {
        return Type.AUTNUM;
      }
      if (query.matches("^[\\d\\.:/]+$")) {
        return Type.IP;
      }
      if (DomainName.of(query).getLevelSize() > 1) {
        return Type.DOMAIN;
      }
      return Type.ENTITY;
    } catch (IllegalArgumentException iae) {
      return Type.ENTITY;
    }
  }

}
