package be.dns.rdap.core;

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

import com.google.common.collect.ImmutableList;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

/**
 * A Notice or Remark
 */
public final class Notice {

  private final String title;

  private final List<String> description;

  private final List<Link> links;

  @JsonCreator
  public Notice(
      @JsonProperty("title") String title,
      @JsonProperty("description") List<String> description,
      @JsonProperty("links") List<Link> links) {
    this.title = title;
    this.description = (description == null) ? null : new ImmutableList.Builder<String>().addAll(description).build();
    this.links = (links == null) ? null : new ImmutableList.Builder<Link>().addAll(links).build();
  }

  public String getTitle() {
    return title;
  }

  public List<String> getDescription() {
    return description;
  }

  public List<Link> getLinks() {
    return links;
  }
}
