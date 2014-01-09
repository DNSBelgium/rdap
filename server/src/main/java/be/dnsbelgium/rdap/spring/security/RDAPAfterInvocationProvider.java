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

import be.dnsbelgium.rdap.core.Domain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AfterInvocationProvider;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import java.util.Collection;

public class RDAPAfterInvocationProvider implements AfterInvocationProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(RDAPAfterInvocationProvider.class);

  @Override
  public Object decide(final Authentication authentication, final Object object, final Collection<ConfigAttribute> attributes, final Object returnedObject) {
    if (returnedObject instanceof Domain) {
      return new Domain.Builder().setLDHName("example.be").build();
    }
    return returnedObject;
  }

  @Override
  public boolean supports(ConfigAttribute attribute) {
    return true;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return true;
  }
}
