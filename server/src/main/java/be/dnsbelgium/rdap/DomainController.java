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

package be.dnsbelgium.rdap;

import be.dnsbelgium.core.DomainName;
import be.dnsbelgium.core.LabelException;
import be.dnsbelgium.rdap.core.Domain;
import be.dnsbelgium.rdap.core.Error;
import be.dnsbelgium.rdap.service.DomainService;
import com.ibm.icu.text.IDNA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value = "domain")
public final class DomainController {

  private static final Logger LOGGER = LoggerFactory.getLogger(DomainController.class);

  private final String baseRedirectURL;

  private final int redirectThreshold;

  @Autowired
  public DomainController(
      @Value("#{applicationProperties['domainController.baseRedirectURL']}") String baseRedirectURL,
      @Value("#{applicationProperties['domainController.redirectThreshold']}") int redirectThreshold) {
    this.baseRedirectURL = baseRedirectURL;
    this.redirectThreshold = redirectThreshold;
  }

  @Autowired
  private DomainService domainService;

  @RequestMapping(value = "/{domainName}", method = RequestMethod.GET, produces = Controllers.CONTENT_TYPE)
  @ResponseBody
  public Domain get(@PathVariable("domainName") final String domainName) throws Error {
    LOGGER.debug("Query for domain {}", domainName);
    final DomainName dn;
    try {
      dn = DomainName.of(domainName);
      Domain result = domainService.getDomain(dn);
      if (result == null) {
        LOGGER.debug("Domain result for '{}' is null. Throwing DomainNotFoundException", domainName);
        throw new Error.DomainNotFound(dn);
      } else {
        result.addRdapConformance(Domain.DEFAULT_RDAP_CONFORMANCE);
      }
      return result;
    } catch (LabelException.IDNParseException e) {
      List<String> description = new ArrayList<String>(e.getErrors().size());
      for (IDNA.Error error : e.getErrors()) {
        description.add(error.name());
      }
      throw new be.dnsbelgium.rdap.core.Error(400, "Invalid domain name", description, e);
    } catch (be.dnsbelgium.rdap.core.Error e) {
      throw e;
    } catch (Exception e) {
      LOGGER.error("Some errors not handled", e);
      throw new be.dnsbelgium.rdap.core.Error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
    }
  }

  @ExceptionHandler(value = Error.NotAuthoritative.class)
  @ResponseBody
  protected Error handleResourceNotFoundException(Error.NotAuthoritative error, HttpServletResponse response) throws UnsupportedEncodingException {
    response.setStatus(error.getErrorCode());
    String location = baseRedirectURL + "/domain/" + URLEncoder.encode(error.getDomainName().getStringValue(), "UTF-8");
    response.addHeader(Controllers.LOCATION_HEADER, location);
    return error;
  }

  @ExceptionHandler(value = Error.class)
  @ResponseBody
  protected Error handleResourceNotFoundException(Error error, HttpServletResponse response) {
    response.setStatus(error.getErrorCode());
    return error;
  }

  public int getRedirectThreshold() {
    return redirectThreshold;
  }
}
