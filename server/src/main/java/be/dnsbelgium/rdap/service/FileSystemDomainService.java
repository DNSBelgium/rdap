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
import be.dnsbelgium.rdap.jackson.CustomObjectMapper;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.File;
import java.io.IOException;

public class FileSystemDomainService implements DomainService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemDomainService.class);

  private File directory;

  private final ObjectMapper mapper = new CustomObjectMapper();

  @Autowired
  public FileSystemDomainService(@Value("#{applicationProperties['domains.path']}") String directory) {
    this.directory = new File(directory);
    if (!this.directory.isDirectory()) {
      throw new IllegalArgumentException(String.format("'%s' is not a directory", directory));
    }
  }

  @Override
  public Domain getDomain(DomainName domainName) throws RDAPError {
    LOGGER.debug("Current locale: {}", LocaleContextHolder.getLocale());
    File domainNameFile = new File(directory, domainName.getStringValue());
    try {
      if (!domainNameFile.isFile() || !domainNameFile.getCanonicalFile().getParent().equals(directory.getCanonicalPath())) {
        return null;
      }
      return mapper.readValue(domainNameFile, Domain.class);
    } catch (IOException e) {
      LOGGER.debug("Error in parsing JSON file", e);
    } catch (Exception e) {
      LOGGER.error("Unhandled error in parsing JSON file", e);
    }
    throw new RDAPError.DomainNotFound(domainName);
  }

  @Override
  public DomainsSearchResult searchDomainsByName(String name) throws RDAPError {
    throw new RDAPError.NotImplemented();
  }

  @Override
  public DomainsSearchResult searchDomainsByNsLdhName(String nsLdhName) throws RDAPError {
    throw new RDAPError.NotImplemented();
  }

  @Override
  public DomainsSearchResult searchDomainsByNsIp(String nsLdhName) throws RDAPError {
    throw new RDAPError.NotImplemented();
  }
}
