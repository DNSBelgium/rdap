package be.dnsbelgium.rdap.client;

import org.apache.http.HttpHost;

import java.net.URI;
import java.net.URISyntaxException;

public class Utils {
  private Utils() {
  }

  public static HttpHost httpHost(String url) {
    URI uri = null;
    try {
      uri = new URI(url);
    } catch (URISyntaxException e) {
      return null;
    }
    HttpHost host;
    int port = uri.getPort();
    if (port == -1) {
      port = uri.getScheme().equalsIgnoreCase("https") ? 443 : 80;
    }
    host = new HttpHost(uri.getHost(), port, uri.getScheme());
    return host;
  }
}
