package ${package};

/*
 * #%L
 * Archetype
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

import be.dns.core.DomainName;
import be.dns.rdap.DomainController;
import be.dns.rdap.core.Notice;
import be.dns.rdap.core.Status;
import be.dns.rdap.service.DomainService;
import be.dns.rdap.core.Domain;
import com.google.common.collect.Lists;

import javax.annotation.Resource;

public class MyDomainService implements DomainService {

  @Override
  public Domain getDomain(DomainName domainName) {

    if (domainName.getLevelSize() > 2) {
      throw new DomainController.DomainNotFoundException(domainName);
    }

    return new Domain.Builder()
        .setLDHName(domainName.toLDH().getStringValue())
        .addStatus(new Status.BasicStatus("active"))
        .build();

  }

}
