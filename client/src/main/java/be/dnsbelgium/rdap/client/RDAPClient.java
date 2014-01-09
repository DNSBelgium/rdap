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

import be.dnsbelgium.rdap.core.Domain;
import be.dnsbelgium.rdap.jackson.CustomObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashSet;
import java.util.Set;

/**
 * A Java client for performing RDAP queries
 */
public class RDAPClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(RDAPClient.class);

  public static KeyStore getKeyStoreFromFile(File file, String password) throws KeyStoreException {
    return getKeyStoreFromFile(file, KeyStore.getDefaultType(), password);
  }

  public static KeyStore getKeyStoreFromFile(File file, String type, String password) throws KeyStoreException {
    KeyStore result = KeyStore.getInstance(type);
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(file);
      result.load(fis, password.toCharArray());
    } catch (IOException e) {
      LOGGER.error("Could not load keystore file", e);
    } catch (CertificateException e) {
      throw new KeyStoreException(e);
    } catch (NoSuchAlgorithmException e) {
      throw new KeyStoreException(e);
    } finally {
      if (fis != null) {
        try {
          fis.close();
        } catch (IOException e) {
          LOGGER.debug("Could not close keystore file", e);
        }
      }
    }
    return result;
  }

  public static KeyStore getPKCS12KeyStoreFromFile(File file, String password) throws KeyStoreException {
    return getKeyStoreFromFile(file, "PKCS12", password);
  }

  public static class RDAPClientException extends Exception implements Serializable  {


    private static final long serialVersionUID = -7254946457661245754L;

    public RDAPClientException() {
      super();
    }

    public RDAPClientException(String message) {
      super(message);
    }

    public RDAPClientException(String message, Throwable cause) {
      super(message, cause);
    }

    public RDAPClientException(Throwable cause) {
      super(cause);
    }
  }
  private final HttpClient httpClient;

  private final ObjectMapper mapper;

  public RDAPClient(HttpClient httpClient) {
    this.httpClient = httpClient;
    this.mapper = new CustomObjectMapper();
  }

  public Domain getDomain(String domainName) throws RDAPClientException {
    HttpGet method;
    try {
      method = new HttpGet(String.format("/rdap/domain/%s", URLEncoder.encode(domainName, "UTF-8")));
      method.setHeader("Accept","application/rdap+json");
      HttpResponse response = httpClient.execute(method);
      HttpEntity entity = response.getEntity();
      return mapper.readValue(entity.getContent(), Domain.class);
    } catch (UnsupportedEncodingException e) {
      LOGGER.error("UTF-8 is an unsupported encoding", e);
      throw new RDAPClientException("Error in getting domain", e);
    } catch (IOException e) {
      LOGGER.error("IO Exception", e);
      throw new RDAPClientException("Error in getting domain", e);
    }
  }

  public JsonNode getDomainAsJson(String domainName) throws RDAPClientException {
    HttpGet method = null;
    try {
      method = new HttpGet(String.format("/rdap/domain/%s", URLEncoder.encode(domainName, "UTF-8")));
      HttpResponse response = httpClient.execute(method);
      HttpEntity entity = response.getEntity();
      return mapper.readTree(entity.getContent());
    } catch (UnsupportedEncodingException e) {
      LOGGER.error("UTF-8 is an unsupported encoding", e);
      throw new RDAPClientException("Error in getting domain", e);
    } catch (IOException e) {
      LOGGER.error("IO Exception", e);
      throw new RDAPClientException("Error in getting domain", e);
    }

  }

  public static class Builder {

    public static final Logger LOGGER = LoggerFactory.getLogger(Builder.class);

    public static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";

    private String lang = null;

    private String username = null;

    private String password = null;

    private URL baseURL = null;

    public Builder setLanguage(String lang) {
      this.lang = lang;
      return this;
    }

    public Builder setBaseURL(String url) {
      try {
        this.baseURL = new URL(url);
      } catch (MalformedURLException e) {
        throw new IllegalArgumentException("Malformed URL", e);
      }
      return this;
    }

    public Builder setUsernamePassword(String username, String password) {
      if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
        throw new IllegalArgumentException("username and password should both not be empty");
      }
      this.username = username;
      this.password = password;
      return this;
    }

    public RDAPClient build() {
      int port = baseURL.getPort();
      if (port == -1 && HTTP.equalsIgnoreCase(baseURL.getProtocol())) {
        port = 80;
      }
      if (port == -1 && HTTPS.equalsIgnoreCase(baseURL.getProtocol())) {
        port = 443;
      }

      LOGGER.debug("Initializing keystores");

      DefaultHttpClient httpClient = null;

      LOGGER.debug("Setting default headers");
      Set<Header> headers = new HashSet<Header>();
      if (!StringUtils.isEmpty(lang)) {
        LOGGER.debug("Adding language header: {}", lang);
        headers.add(new BasicHeader(ACCEPT_LANGUAGE_HEADER, lang));
      }

      LOGGER.debug("Setting username and password");
      if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
//        httpClient.getCredentialsProvider().setCredentials(
//            new AuthScope()
//        );
      }

      httpClient.getParams().setParameter(ClientPNames.DEFAULT_HEADERS, headers);

      return new RDAPClient(httpClient);
    }

  }
}
