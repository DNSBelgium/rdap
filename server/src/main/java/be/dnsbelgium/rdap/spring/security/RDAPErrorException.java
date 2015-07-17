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
package be.dnsbelgium.rdap.spring.security;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
public class RDAPErrorException extends AccessDeniedException {

  private static final long serialVersionUID = 5433510515283772536L;

  private final int errorCode;

  private final String title;

  private final List<String> description;

  public RDAPErrorException(int errorCode, String title, String... description) {
    super(title);
    this.errorCode = errorCode;
    this.title = title;
    if (description == null) {
      this.description = null;
    } else {
      this.description = new ImmutableList.Builder<String>().add(description).build();
    }
  }

  public RDAPErrorException(int errorCode, String title) {
    this(errorCode, title, (String[]) null);
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
}
