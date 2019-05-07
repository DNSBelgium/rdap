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

package be.dnsbelgium.rdap.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import be.dnsbelgium.core.DomainName;
import be.dnsbelgium.rdap.core.Domain;
import be.dnsbelgium.rdap.core.RDAPError;
import be.dnsbelgium.rdap.service.DomainService;

@Controller
@RequestMapping(value = "domain")
public final class DomainController {

	private final Logger logger = LoggerFactory.getLogger(DomainController.class);

	private final String baseRedirectURL;

	private final DomainService domainService;

	@Autowired
	public DomainController(@Value("${baseRedirectURL}") String baseRedirectURL, DomainService domainService) {
		this.baseRedirectURL = baseRedirectURL;
		this.domainService = domainService;
	}

	@RequestMapping(value = "/{domainName}", method = RequestMethod.GET, produces = Controllers.CONTENT_TYPE)
	@ResponseBody
	public Domain get(@PathVariable("domainName") final String domainName) throws RDAPError {
		logger.debug("Query(GET) for domain {}", domainName);
		Domain result = getDomain(domainName);
		return result;
	}

	private Domain getDomain(@PathVariable("domainName") String domainName) throws RDAPError {
		final DomainName dn;
		dn = DomainName.of(domainName);
		Domain result = domainService.getDomain(dn);
		if (result == null) {
			logger.debug("Domain result for '{}' is null. Throwing DomainNotFound Error", domainName);
			throw RDAPError.domainNotFound(dn);
		}
		return result;
	}

	@RequestMapping(value = "/{domainName}", method = RequestMethod.HEAD, produces = Controllers.CONTENT_TYPE)
	public ResponseEntity<Void> head(@PathVariable("domainName") final String domainName) throws RDAPError {
		logger.debug("Query(HEAD) for domain {}", domainName);
		Domain result = getDomain(domainName);
		return new ResponseEntity<Void>(null, new HttpHeaders(), HttpStatus.OK);
	}

	@ExceptionHandler(value = RDAPError.NotAuthoritative.class)
	@ResponseBody
	protected RDAPError handleResourceNotFoundException(RDAPError.NotAuthoritative error, HttpServletResponse response)
			throws UnsupportedEncodingException {
		response.setStatus(error.getErrorCode());
		String location = baseRedirectURL + "/domain/" + URLEncoder.encode(error.getDomainName(), "UTF-8");
		response.addHeader(Controllers.LOCATION_HEADER, location);
		return error;
	}

	@RequestMapping(value = "/{domainName}", method = { RequestMethod.DELETE, RequestMethod.PUT, RequestMethod.OPTIONS,
			RequestMethod.PATCH, RequestMethod.POST, RequestMethod.TRACE }, produces = Controllers.CONTENT_TYPE)
	@ResponseBody
	public Domain any(@PathVariable("domainName") final String domainName) throws RDAPError {
		throw RDAPError.methodNotAllowed();
	}
}
