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
package be.dnsbelgium.rdap.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Link {

  private final URI value;

  private final String rel;

  private final URI href;

  private final Set<String> hreflang;

  private final List<String> title;

  private final String media;

  private final String type;

  @JsonCreator
  public Link(
      @JsonProperty("value") URI value,
      @JsonProperty("rel") String rel,
      @JsonProperty("href") URI href,
      @JsonProperty("hreflang") Set<String> hreflang,
      @JsonProperty("title") List<String> title,
      @JsonProperty("media") String media,
      @JsonProperty("type") String type) {
    if (href == null) {
      throw new IllegalArgumentException("href MUST be specified");
    }

    this.value = value;
    this.rel = rel;
    this.href = href;
    this.hreflang = (hreflang == null) ? null : new ImmutableSet.Builder<String>().addAll(hreflang).build();
    this.title = (title == null) ? null : new ImmutableList.Builder<String>().addAll(title).build();
    this.media = media;
    this.type = type;
  }

  public URI getValue() {
    return value;
  }

  public String getRel() {
    return rel;
  }

  public URI getHref() {
    return href;
  }

  public Set<String> getHreflang() {
    return hreflang;
  }

  public List<String> getTitle() {
    return title;
  }

  public String getMedia() {
    return media;
  }

  public String getType() {
    return type;
  }

  public static class Builder {

    private URI value;

    private String rel;

    private URI href;

    private Set<String> hreflang;

    private List<String> title;

    private String media;

    private String type;

    public Builder(String value, String rel, String href) throws URISyntaxException {
      this.value = new URI(value);
      this.rel = rel;
      this.href = new URI(href);
    }

    public Builder addHreflang(String hreflang) {
      if (this.hreflang == null) {
        this.hreflang = new HashSet<String>();
      }
      this.hreflang.add(hreflang);
      return this;
    }

    public Builder addTitle(String title) {
      if (this.title == null) {
        this.title = new ArrayList<String>();
      }
      this.title.add(title);
      return this;
    }

    public Link build() {
      return new Link(value, rel, href, hreflang, title, media, type);
    }
  }
}
