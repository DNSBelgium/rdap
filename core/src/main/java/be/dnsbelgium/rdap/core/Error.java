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

  public static class HelpNotFound extends Error {

    private static final long serialVersionUID = -2365389916154054286L;

    public HelpNotFound() {
      super(404, "Help not found");
    }
  }

  public static class AutNumNotFound extends Error {

    private static final long serialVersionUID = 3356523501894745257L;
    private final int autNum;

    public AutNumNotFound(int autNum) {
      super(404, String.format("AutNum %s not found", autNum));
      this.autNum = autNum;
    }

    public int getAutNum() {
      return autNum;
    }
  }

  public static class IPNotFound extends Error {

    private static final long serialVersionUID = -7523573051976600864L;
    private final String ipAddress;

    public IPNotFound(String ipAddress) {
      super(404, String.format("IP %s not found", ipAddress));
      this.ipAddress = ipAddress;
    }

    public String getIpAddress() {
      return ipAddress;
    }
  }

  public static class EntityNotFound extends Error {

    private static final long serialVersionUID = -5264750084274730969L;
    private final String handle;

    public EntityNotFound(String handle) {
      super(404, String.format("Entity %s not found", handle));
      this.handle = handle;
    }

    public String getHandle() {
      return handle;
    }
  }

  public static class NameserverNotFound extends Error {

    private static final long serialVersionUID = -3617347189246764940L;
    private final DomainName nameserverName;

    public NameserverNotFound(DomainName nameserverName) {
      super(404, String.format("Nameserver %s not found", nameserverName.toLDH().getStringValue()));
      this.nameserverName = nameserverName;
    }

    public DomainName getNameserverName() {
      return nameserverName;
    }

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
      super(301, String.format("Not authoritative for %s", domainName.toLDH().getStringValue()));
      this.domainName = domainName;
    }

    public DomainName getDomainName() {
      return domainName;
    }
  }

}
