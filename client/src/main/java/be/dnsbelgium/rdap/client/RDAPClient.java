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

import be.dnsbelgium.rdap.jackson.CustomObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

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

  private final HttpHost host;

  private final String path;

  private final ObjectMapper mapper;

  public RDAPClient(HttpClient httpClient, String baseUrl) throws URISyntaxException {
    this.httpClient = httpClient;
    URI uri = new URI(baseUrl);
    this.path = uri.getPath().startsWith("/") ? uri.getPath() : "/" + uri.getPath();
    this.host = Utils.httpHost(baseUrl);
    this.mapper = new CustomObjectMapper();
  }

  public JsonNode getDomainAsJson(String domainName) throws RDAPClientException {
    return query("domain", domainName);
  }

  public JsonNode getEntityAsJson(String entityId) throws RDAPClientException {
    return query("entity", entityId);
  }

  public JsonNode getAutNum(String autnum) throws RDAPClientException {
    return query("autnum", autnum);
  }

  public JsonNode getIp(String ip) throws RDAPClientException {
    return query("ip", ip);
  }

  public JsonNode getNameserver(String nameserver) throws RDAPClientException {
    return query("nameserver", nameserver);
  }

  private JsonNode query(String subpath, String query) throws RDAPClientException {
    HttpGet method = null;
    try {
      method = new HttpGet(String.format("%s/%s/%s", path, subpath, URLEncoder.encode(query, "UTF-8")));
      HttpResponse response = httpClient.execute(host, method);
      HttpEntity entity = response.getEntity();
      return mapper.readTree(entity.getContent());
    } catch (UnsupportedEncodingException e) {
      throw new RDAPClientException("UTF-8 is an unsupported encoding", e);
    } catch (IOException e) {
      throw new RDAPClientException(String.format("Could not get %s information for %s", subpath, query), e);
    }

  }

}
