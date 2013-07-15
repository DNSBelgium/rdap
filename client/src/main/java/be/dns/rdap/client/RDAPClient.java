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

import be.dns.rdap.core.Domain;
import be.dns.rdap.jackson.CustomObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

  public static KeyStore getKeyStoreFromFile(File file, String password) throws KeyStoreException, CertificateException, NoSuchAlgorithmException {
    return getKeyStoreFromFile(file, KeyStore.getDefaultType(), password);
  }

  public static KeyStore getKeyStoreFromFile(File file, String type, String password) throws KeyStoreException, CertificateException, NoSuchAlgorithmException {
    KeyStore result = KeyStore.getInstance(type);
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(file);
      result.load(fis, password.toCharArray());
    } catch (IOException e) {
      // TODO: what to do
    } finally {
      if (fis != null) try {
        fis.close();
      } catch (IOException e) {
        // do nothing. failed to close the FileInputStream
      }
    }
    return result;
  }

  public static KeyStore getPKCS12KeyStoreFromFile(File file, String password) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
    return getKeyStoreFromFile(file, "PKCS12", password);
  }

  public static class RDAPClientException extends Exception {
    public RDAPClientException() {
      super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public RDAPClientException(String message) {
      super(message);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public RDAPClientException(String message, Throwable cause) {
      super(message, cause);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public RDAPClientException(Throwable cause) {
      super(cause);    //To change body of overridden methods use File | Settings | File Templates.
    }
  }

  public static final Logger logger = LoggerFactory.getLogger(RDAPClient.class);

  public final HttpClient httpClient;

  public final ObjectMapper mapper;

  public RDAPClient(HttpClient httpClient) {
    this.httpClient = httpClient;
    this.mapper = new CustomObjectMapper();
  }

  public Domain getDomain(String domainName) throws RDAPClientException {
    HttpGet method = null;
    try {
      method = new HttpGet(String.format("/rdap/domain/%s", URLEncoder.encode(domainName, "UTF-8")));
      method.setHeader("Accept","application/rdap+json");
      HttpResponse response = httpClient.execute(method);
      HttpEntity entity = response.getEntity();
      //System.out.println(EntityUtils.toString(entity));
      return mapper.readValue(entity.getContent(), Domain.class);
    } catch (UnsupportedEncodingException e) {
      logger.error("UTF-8 is an unsupported encoding", e);
      throw new RDAPClientException("Error in getting domain", e);
    } catch (IOException e) {
      logger.error("IO Exception", e);
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
      logger.error("UTF-8 is an unsupported encoding", e);
      throw new RDAPClientException("Error in getting domain", e);
    } catch (IOException e) {
      logger.error("IO Exception", e);
      throw new RDAPClientException("Error in getting domain", e);
    }

  }

  public static class Builder {

    public static final Logger LOGGER = LoggerFactory.getLogger(Builder.class);

    public static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";

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
      if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password))
        throw new IllegalArgumentException("username and password should both not be empty");
      this.username = username;
      this.password = password;
      return this;
    }

    public RDAPClient build() {
      int port = baseURL.getPort();
      if (port == -1 && baseURL.getProtocol().equalsIgnoreCase("http")) port = 80;
      if (port == -1 && baseURL.getProtocol().equalsIgnoreCase("https")) port = 443;

      LOGGER.debug("Initializing keystores");

      DefaultHttpClient httpClient = null;

      LOGGER.debug("Setting default headers");
      Set<Header> headers = new HashSet<Header>();
      if (!StringUtils.isEmpty(lang)) {
        LOGGER.debug("Adding language header: {}", lang);
        headers.add(new BasicHeader(ACCEPT_LANGUAGE_HEADER, lang));
      }

      logger.debug("Setting username and password");
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
