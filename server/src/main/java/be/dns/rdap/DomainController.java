
package be.dns.rdap;

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

import be.dns.core.DomainName;
import be.dns.core.LabelException;
import be.dns.rdap.core.Domain;
import be.dns.rdap.core.Error;
import be.dns.rdap.service.DomainService;
import com.ibm.icu.text.IDNA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value = "domain")
public final class DomainController {

  private static final Logger LOGGER = LoggerFactory.getLogger(DomainController.class);

  private static final String LOCATION_HEADER = "Location";

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
      }
      return result;
    } catch (LabelException.IDNParseException e) {
      List<String> description = new ArrayList<String>(e.getErrors().size());
      for (IDNA.Error error : e.getErrors()) description.add(error.name());
      throw new be.dns.rdap.core.Error(400, "Invalid domain name", description);
    } catch (Error e) {
      throw e;
    } catch (Throwable t) {
      LOGGER.error("Some errors not handled", t);
      throw new Error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
    }
  }

  @ExceptionHandler(value = Error.NotAuthoritative.class)
  @ResponseBody
  protected Error handleResourceNotFoundException(Error.NotAuthoritative error, HttpServletResponse response) throws UnsupportedEncodingException {
    response.setStatus(error.getErrorCode());
    String location = baseRedirectURL + "/domain/" + URLEncoder.encode(error.getDomainName().getStringValue(), "UTF-8");
    response.addHeader(LOCATION_HEADER, location);
    return error;
  }

  @ExceptionHandler(value = Error.class)
  @ResponseBody
  protected Error handleResourceNotFoundException(Error error, HttpServletResponse response) {
    response.setStatus(error.getErrorCode());
    return error;
  }



}
