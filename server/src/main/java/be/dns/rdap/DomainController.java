
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
import be.dns.rdap.core.Domain;
import be.dns.rdap.service.DomainService;
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
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

  /**
   * Parent of all exceptions thrown by the DomainService.
   */
  public abstract static class DomainException extends RuntimeException {

    /**
     * The subject of the DomainException.
     */
    private final DomainName domainName;

    public DomainException(DomainName domainName) {
      this.domainName = domainName;
    }

    public DomainName getDomainName() {
      return domainName;
    }
  }

  /**
   * Thrown when a Domain is not found.
   */
  public static class DomainNotFoundException extends DomainException {
    public DomainNotFoundException(DomainName domainName) {
      super(domainName);
    }
  }

  /**
   * Thrown when this server is not authoritative for the
   * domain being looked up.
   * <p/>
   * e.g. when looking up a .com domain at the .be registry
   */
  public static class NotAuthoritativeException extends DomainException {
    public NotAuthoritativeException(DomainName domainName) {
      super(domainName);
    }
  }

  @Autowired
  private DomainService domainService;

  @RequestMapping(value = "/{domainName}", method = RequestMethod.GET, produces = Controllers.CONTENT_TYPE)
  @ResponseBody
  public Domain get(@PathVariable("domainName") final String domainName) {
    LOGGER.debug("Query for domain {}", domainName);
    DomainName dn = DomainName.of(domainName);
    Domain result = domainService.getDomain(dn);
    if (result == null) {
      LOGGER.debug("Domain result for '{}' is null. Throwing DomainNotFoundException", domainName);
      throw new DomainNotFoundException(dn);
    }
    return result;
  }

  @RequestMapping(value = "/{domainName}/redirect/{redirect}", method = RequestMethod.GET, produces = Controllers.CONTENT_TYPE)
  @ResponseBody
  public Domain get(@PathVariable("domainName") final String domainName, @PathVariable final int redirect) {
    if (redirect > redirectThreshold) {
      LOGGER.debug("Exceeded threshold for domain name {}. Current: {} Max: {}", domainName, redirect, redirectThreshold);
      throw new DomainNotFoundException(DomainName.of(domainName));
    }
    return get(domainName);
  }

  @ExceptionHandler(value = NotAuthoritativeException.class)
  @ResponseStatus(value = HttpStatus.MOVED_PERMANENTLY)
  @ResponseBody
  protected Domain handleNotAuthoritativeException(NotAuthoritativeException ex, HttpServletRequest request, HttpServletResponse response) {
    String location = baseRedirectURL + "/domain/" + ex.getDomainName().getStringValue() + "/redirect/";
    String requestURI = request.getRequestURI();
    Pattern pattern = Pattern.compile(".*/redirect/(\\d+).*", Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(requestURI);
    if (matcher.matches()) {
      long number = Long.parseLong(matcher.group(1));
      location += number + 1;
    } else {
      location += 1;
    }
    response.addHeader(LOCATION_HEADER, location);
    // TODO: handler method in DomainService to create a domain object to return
    return new Domain.Builder().setLDHName(ex.getDomainName().toLDH().getStringValue()).build();
  }

  @ExceptionHandler(value = DomainNotFoundException.class)
  @ResponseStatus(value = HttpStatus.NOT_FOUND)
  @ResponseBody
  protected Domain handleResourceNotFoundException(DomainNotFoundException ex, HttpServletResponse response) {
    // TODO: handler method in DomainService to create a domain object to return
    return new Domain.Builder().setLDHName(ex.getDomainName().toLDH().getStringValue()).build();
  }

}
