package be.dns.rdap.spring.security;

/*
 * #%L
 * Server
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
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
public class RDAPErrorException extends AccessDeniedException {

  private final int errorCode;

  private final String title;

  private final List<String> description;

  public RDAPErrorException(int errorCode, String title, List<String> description) {
    super(title);
    this.errorCode = errorCode;
    this.title = title;
    if (description == null) {
      this.description = null;
    } else {
      this.description = new ImmutableList.Builder<String>().addAll(description).build();
    }
  }

  public RDAPErrorException(int errorCode, String title) {
    this(errorCode, title, null);
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
