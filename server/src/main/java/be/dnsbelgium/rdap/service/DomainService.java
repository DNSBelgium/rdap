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
package be.dnsbelgium.rdap.service;

import be.dnsbelgium.core.DomainName;
import be.dnsbelgium.rdap.core.Domain;
import be.dnsbelgium.rdap.core.DomainsSearchResult;
import be.dnsbelgium.rdap.core.RDAPError;

public interface DomainService {

  Domain getDomain(DomainName domainName) throws RDAPError;

  DomainsSearchResult searchDomainsByName(String name) throws RDAPError;

  DomainsSearchResult searchDomainsByNsLdhName(String nsLdhName) throws RDAPError;

  DomainsSearchResult searchDomainsByNsIp(String nsLdhName) throws RDAPError;
}
