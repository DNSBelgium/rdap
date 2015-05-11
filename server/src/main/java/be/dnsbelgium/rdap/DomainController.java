/**
 * Copyright 2014 DNS Belgium vzw
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package be.dnsbelgium.rdap;

import be.dnsbelgium.core.DomainName;
import be.dnsbelgium.rdap.core.Domain;
import be.dnsbelgium.rdap.core.RDAPError;
import be.dnsbelgium.rdap.service.DomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Controller
@RequestMapping(value = "domain")
public final class DomainController {

  private final Logger logger = LoggerFactory.getLogger(DomainController.class);

  private final String baseRedirectURL;

  private final int redirectThreshold;

  @Autowired
  public DomainController(
          @Value("#{applicationProperties['baseRedirectURL']}") String baseRedirectURL,
          @Value("#{applicationProperties['redirectThreshold']}") int redirectThreshold) {
    this.baseRedirectURL = baseRedirectURL;
    this.redirectThreshold = redirectThreshold;
  }

  @Autowired
  private DomainService domainService;

  @RequestMapping(value = "/{domainName}", method = RequestMethod.GET, produces = Controllers.CONTENT_TYPE)
  @ResponseBody
  public Domain get(@PathVariable("domainName") final String domainName) throws RDAPError {
    logger.debug("Query for domain {}", domainName);
    final DomainName dn;
    dn = DomainName.of(domainName);
    Domain result = domainService.getDomain(dn);
    if (result == null) {
      logger.debug("Domain result for '{}' is null. Throwing DomainNotFound Error", domainName);
      throw RDAPError.domainNotFound(dn);
    }
    return result;
  }

  @ExceptionHandler(value = RDAPError.NotAuthoritative.class)
  @ResponseBody
  protected RDAPError handleResourceNotFoundException(RDAPError.NotAuthoritative error, HttpServletResponse response) throws UnsupportedEncodingException {
    response.setStatus(error.getErrorCode());
    String location = baseRedirectURL + "/domain/" + URLEncoder.encode(error.getDomainName(), "UTF-8");
    response.addHeader(Controllers.LOCATION_HEADER, location);
    return error;
  }

  public int getRedirectThreshold() {
    return redirectThreshold;
  }
}
