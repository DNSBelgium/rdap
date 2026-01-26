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
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static be.dnsbelgium.rdap.core.Common.DEFAULT_RDAP_CONFORMANCE;

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
public class RDAPError extends Exception {

	private static final long serialVersionUID = 3000647771812593816L;

  private final Set<String> rdapConformance = new LinkedHashSet<>();

	private final int errorCode;

	private final String title;

	private final List<String> description;

	@JsonCreator
	public RDAPError(@JsonProperty("errorCode") int errorCode, @JsonProperty("title") String title,
			@JsonProperty("description") List<String> description) {
		this.errorCode = errorCode;
		this.title = title;
		this.description = description == null ? null : new ImmutableList.Builder<String>().addAll(description).build();
    addRdapConformance(DEFAULT_RDAP_CONFORMANCE);
	}

	@JsonCreator
	public RDAPError(@JsonProperty("errorCode") int errorCode, @JsonProperty("title") String title,
			@JsonProperty("description") String description) {
		this(errorCode, title, description == null ? null : Arrays.asList(description));
	}

	@JsonCreator
	public RDAPError(@JsonProperty("errorCode") int errorCode, @JsonProperty("title") String title) {
		this(errorCode, title, (List<String>) null);
	}

	public RDAPError(int errorCode, String title, List<String> description, Throwable cause) {
		this(errorCode, title, description);
		this.initCause(cause);
	}

  @JsonProperty
  public Set<String> getRdapConformance() {
    return rdapConformance;
  }

  public void addRdapConformance(String conformance) {
    rdapConformance.add(conformance);
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

	public static DomainNotFound domainNotFound(DomainName domainName) {
		return new DomainNotFound(domainName);
	}

	public static AutNumNotFound autNumNotFound(int autNum) {
		return new AutNumNotFound(autNum);
	}

	public static BadRequest badRequest(String title, String description) {
		return new BadRequest(title, description);
	}

	public static BadRequest badRequest(String title, List<String> description) {
		return new BadRequest(title, description);
	}

	public static NotAuthoritative notAuthoritative(DomainName domainName) {
		return new NotAuthoritative(domainName.getStringValue());
	}

	public static RDAPError entityNotFound(String handle) {
		return new EntityNotFound(handle);
	}

	public static RDAPError helpNotFound() {
		return new HelpNotFound();
	}

	public static RDAPError ipNotFound(String ipAddress) {
		return new IPNotFound(ipAddress);
	}

	public static RDAPError nameserverNotFound(DomainName domainName) {
		return new NameserverNotFound(domainName);
	}

	public static RDAPError notImplemented() {
		return new NotImplemented();
	}

	public static RDAPError noResults(String query) {
		return new NoResults(query);
	}

	public static RDAPError methodNotAllowed() {
		return new MethodNotAllowed();
	}

	public static class NoResults extends RDAPError {

		private static final long serialVersionUID = -3752099182358813007L;

		private NoResults(String query) {
			super(HttpStatus.NOT_FOUND, String.format("No results for query %s", query));
		}
	}

	public static class BadRequest extends RDAPError {

		private static final long serialVersionUID = -7970785038966067523L;

		private BadRequest(String title, String description) {
			super(HttpStatus.BAD_REQUEST, title, description);
		}

		private BadRequest(String title, List<String> description) {
			super(HttpStatus.BAD_REQUEST, title, description);
		}
	}

	public static class HelpNotFound extends RDAPError {

		private static final long serialVersionUID = -2365389916154054286L;

		private HelpNotFound() {
			super(HttpStatus.NOT_FOUND, "Help not found");
		}
	}

	public static class NotImplemented extends RDAPError {

		private static final long serialVersionUID = 1908478239735418778L;

		private NotImplemented() {
			super(HttpStatus.NOT_IMPLEMENTED, "Not implemented");
		}
	}

	public static class AutNumNotFound extends RDAPError {

		private static final long serialVersionUID = 3356523501894745257L;
		private final int autNum;

		private AutNumNotFound(int autNum) {
			super(HttpStatus.NOT_FOUND, String.format("AutNum %s not found", autNum));
			this.autNum = autNum;
		}

		public int getAutNum() {
			return autNum;
		}
	}

	public static class IPNotFound extends RDAPError {

		private static final long serialVersionUID = -7523573051976600864L;
		private final String ipAddress;

		private IPNotFound(String ipAddress) {
			super(HttpStatus.NOT_FOUND, String.format("IP %s not found", ipAddress));
			this.ipAddress = ipAddress;
		}

		public String getIpAddress() {
			return ipAddress;
		}
	}

	public static class EntityNotFound extends RDAPError {

		private static final long serialVersionUID = -5264750084274730969L;
		private final String handle;

		private EntityNotFound(String handle) {
			super(HttpStatus.NOT_FOUND, String.format("Entity %s not found", handle));
			this.handle = handle;
		}

		public String getHandle() {
			return handle;
		}
	}

	public static class NameserverNotFound extends RDAPError {

		private static final long serialVersionUID = -3617347189246764940L;
		private final DomainName nameserverName;

		private NameserverNotFound(DomainName nameserverName) {
			super(HttpStatus.NOT_FOUND,
					String.format("Nameserver %s not found", nameserverName.toLDH().getStringValue()));
			this.nameserverName = nameserverName;
		}

		public DomainName getNameserverName() {
			return nameserverName;
		}

	}

	public static class DomainNotFound extends RDAPError {

		private static final long serialVersionUID = -1355753652647945804L;
		private final DomainName domainName;

		private DomainNotFound(DomainName domainName) {
			super(HttpStatus.NOT_FOUND, String.format("Domain %s not found", domainName.toLDH().getStringValue()));
			this.domainName = domainName;
		}

		public DomainName getDomainName() {
			return domainName;
		}
	}

	public static class NotAuthoritative extends RDAPError {

		private static final long serialVersionUID = 7010767440479876394L;
		private String domainName;

		private NotAuthoritative(String domainName) {
			super(HttpStatus.MOVED_PERMANENTLY, String.format("Not authoritative for %s", domainName));
			this.domainName = domainName;
		}

		public String getDomainName() {
			return domainName;
		}
	}

	public static class MethodNotAllowed extends RDAPError {

		private static final long serialVersionUID = 1428932048414525660L;

		private MethodNotAllowed() {
			super(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed");
		}

	}
}
