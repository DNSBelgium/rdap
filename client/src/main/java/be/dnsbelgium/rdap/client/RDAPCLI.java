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

import be.dnsbelgium.core.DomainName;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.cli.*;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.ssl.*;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

/**
 * Command line RDAP client (Command Line Interface)
 */
public final class RDAPCLI {

  private RDAPCLI() {

  }

  private static final Logger LOGGER = LoggerFactory.getLogger(RDAPCLI.class);

  public static enum Type {
    DOMAIN, ENTITY, NAMESERVER, AUTNUM, IP
  }


  public static void main(String[] args) {

    LOGGER.debug("Create the command line parser");
    CommandLineParser parser = new GnuParser();

    LOGGER.debug("Create the options");
    Options options = new RDAPOptions(Locale.ENGLISH);

    try {
      LOGGER.debug("Parse the command line arguments");
      CommandLine line = parser.parse(options, args);

      if (line.hasOption("help")) {
        printHelp(options);
        return;
      }

      if (line.getArgs().length == 0) {
        throw new IllegalArgumentException("You must provide a query");
      }
      String query = line.getArgs()[0];

      Type type = (line.getArgs().length == 2) ? Type.valueOf(line.getArgs()[1].toUpperCase()) : guessQueryType(query);

      LOGGER.debug("Query: {}, Type: {}", query, type);

      try {
        SSLContextBuilder sslContextBuilder = SSLContexts.custom();
        if (line.hasOption(RDAPOptions.TRUSTSTORE)) {
          sslContextBuilder.loadTrustMaterial(RDAPClient.getKeyStoreFromFile(new File(line.getOptionValue(RDAPOptions.TRUSTSTORE)), line.getOptionValue(RDAPOptions.TRUSTSTORE_TYPE, RDAPOptions.DEFAULT_STORETYPE), line.getOptionValue(RDAPOptions.TRUSTSTORE_PASS, RDAPOptions.DEFAULT_PASS)));
        }
        if (line.hasOption(RDAPOptions.KEYSTORE)) {
          sslContextBuilder.loadKeyMaterial(RDAPClient.getKeyStoreFromFile(new File(line.getOptionValue(RDAPOptions.KEYSTORE)), line.getOptionValue(RDAPOptions.KEYSTORE_TYPE, RDAPOptions.DEFAULT_STORETYPE), line.getOptionValue(RDAPOptions.KEYSTORE_PASS, RDAPOptions.DEFAULT_PASS)), line.getOptionValue(RDAPOptions.KEYSTORE_PASS, RDAPOptions.DEFAULT_PASS).toCharArray());
        }
        SSLContext sslContext = sslContextBuilder.build();

        final String url = line.getOptionValue(RDAPOptions.URL);
        final HttpHost host = Utils.httpHost(url);


        HashSet<Header> headers = new HashSet<Header>();
        headers.add(new BasicHeader("Accept-Language", line.getOptionValue(RDAPOptions.LANG, Locale.getDefault().toString())));
        HttpClientBuilder httpClientBuilder = HttpClients.custom()
            .setDefaultHeaders(headers)
            .setSSLSocketFactory(
                new SSLConnectionSocketFactory(
                    sslContext,
                    (line.hasOption(RDAPOptions.INSECURE) ? new AllowAllHostnameVerifier() :new BrowserCompatHostnameVerifier())
                )
            );

        if (line.hasOption(RDAPOptions.USERNAME) && line.hasOption(RDAPOptions.PASSWORD)) {
          BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
          credentialsProvider.setCredentials(
              new AuthScope(host.getHostName(), host.getPort()),
              new UsernamePasswordCredentials(line.getOptionValue(RDAPOptions.USERNAME), line.getOptionValue(RDAPOptions.PASSWORD)));
          httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        }

        RDAPClient rdapClient = new RDAPClient(httpClientBuilder.build(), url);
        ObjectMapper mapper = new ObjectMapper();

        JsonNode json = null;
        switch (type) {
          case DOMAIN:
            json = rdapClient.getDomainAsJson(query);
            break;
          case ENTITY:
            json = rdapClient.getEntityAsJson(query);
            break;
          case AUTNUM:
            json = rdapClient.getAutNum(query);
            break;
          case IP:
            json = rdapClient.getIp(query);
            break;
          case NAMESERVER:
            json = rdapClient.getNameserver(query);
            break;
        }
        PrintWriter out = new PrintWriter(System.out, true);
        if (line.hasOption(RDAPOptions.RAW)) {
          mapper.writer().writeValue(out, json);
        } else if (line.hasOption(RDAPOptions.PRETTY)) {
          mapper.writer(new DefaultPrettyPrinter()).writeValue(out, json);
        } else if (line.hasOption(RDAPOptions.YAML)) {
          DumperOptions dumperOptions = new DumperOptions();
          dumperOptions.setPrettyFlow(true);
          dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
          dumperOptions.setSplitLines(true);
          Yaml yaml = new Yaml(dumperOptions);
          Map data = mapper.convertValue(json, Map.class);
          yaml.dump(data, out);
        } else {
          mapper.writer(new MinimalPrettyPrinter()).writeValue(out, json);
        }
        out.flush();
      } catch (Exception e) {
        LOGGER.error(e.getMessage(), e);
        System.exit(-1);
      }
    } catch (org.apache.commons.cli.ParseException e) {
      printHelp(options);
      System.exit(-1);
    }
  }

  private static void printHelp(Options options) {
    HelpFormatter hf = new HelpFormatter();
    hf.printHelp("rdap [ OPTIONS ]... [ QUERY ] [ TYPE ]\n", null /* header */, options, "\n" /* footer */, true);
  }

  public static Type guessQueryType(String query) {
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
      LOGGER.debug("Not a domain name, defaulting to entity", iae);
      return Type.ENTITY;
    }
  }

}
