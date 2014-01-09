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

/**
 * #%L
 * RDAP Core
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

import be.dnsbelgium.core.DomainName;
import com.google.common.collect.ImmutableList;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Arrays;
import java.util.List;

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
public class Error extends Exception {

  private static final long serialVersionUID = 3000647771812593816L;
  private final int errorCode;

  private final String title;

  private final List<String> description;

  @JsonCreator
  public Error(
      @JsonProperty("errorCode") int errorCode,
      @JsonProperty("title") String title,
      @JsonProperty("description") List<String> description) {
    this.errorCode = errorCode;
    this.title = title;
    this.description = description == null ? null : new ImmutableList.Builder<String>().addAll(description).build();
  }

  @JsonCreator
  public Error(
      @JsonProperty("errorCode") int errorCode,
      @JsonProperty("title") String title,
      @JsonProperty("description") String description) {
    this(errorCode, title, description == null ? null : Arrays.asList(description));
  }

  @JsonCreator
  public Error(
      @JsonProperty("errorCode") int errorCode,
      @JsonProperty("title") String title) {
    this(errorCode, title, (List<String>) null);
  }

  public Error(int errorCode, String title, List<String> description, Throwable cause) {
    this(errorCode, title, description);
    this.initCause(cause);
  }

  @JsonProperty
  public int getErrorCode() {
    return errorCode;
  }

  @JsonProperty
  public String getTitle() {
    return title;
  }

  @JsonProperty
  public List<String> getDescription() {
    return description;
  }

  public static class DomainNotFound extends Error {

    private static final long serialVersionUID = -1355753652647945804L;
    private final DomainName domainName;

    public DomainNotFound(DomainName domainName) {
      super(404, String.format("Domain %s not found", domainName.toLDH().getStringValue()));
      this.domainName = domainName;
    }

    public DomainName getDomainName() {
      return domainName;
    }
  }

  public static class NotAuthoritative extends Error {

    private static final long serialVersionUID = 7010767440479876394L;
    private final DomainName domainName;

    public NotAuthoritative(DomainName domainName) {
      super(301, String.format("Not authoritative for domain %s", domainName.toLDH().getStringValue()));
      this.domainName = domainName;
    }

    public DomainName getDomainName() {
      return domainName;
    }
  }

}
