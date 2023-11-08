package be.dnsbelgium.rdap.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

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

import jakarta.servlet.http.HttpServletResponse;
import com.ibm.icu.text.IDNA;

import be.dnsbelgium.core.DomainName;
import be.dnsbelgium.core.LabelException;
import be.dnsbelgium.rdap.core.Nameserver;
import be.dnsbelgium.rdap.core.RDAPError;
import be.dnsbelgium.rdap.service.NameserverService;

@Controller
@RequestMapping(value = "nameserver")
public class NameserverController {

	private final static Logger logger = LoggerFactory.getLogger(NameserverController.class);

	private final String baseRedirectURL;

	private final NameserverService nameserverService;

	@Autowired
	public NameserverController(@Value("${baseRedirectURL}") String baseRedirectURL, NameserverService nameserverService) {
		this.baseRedirectURL = baseRedirectURL;
		this.nameserverService = nameserverService;
	}

	@RequestMapping(value = "/{nameserverName}", method = RequestMethod.GET, produces = Controllers.CONTENT_TYPE)
	@ResponseBody
	public Nameserver get(@PathVariable("nameserverName") final String nameserverName) throws RDAPError {
		logger.debug("Query(GET) for nameserver {}", nameserverName);
		final DomainName domainName;
		try {
			domainName = DomainName.of(nameserverName);
			Nameserver nameserver = nameserverService.getNameserver(domainName);
			if (nameserver == null) {
				logger.debug("Query(GET) result for {} is null. Throwing NameserverNotFound Error", nameserverName);
				throw RDAPError.nameserverNotFound(domainName);
			}
			return nameserver;
		} catch (LabelException.IDNParseException e) {
			List<String> description = new ArrayList<String>(e.getErrors().size());
			for (IDNA.Error error : e.getErrors()) {
				description.add(error.name());
			}
			throw new RDAPError(400, "Invalid nameserver name", description, e);
		} catch (RDAPError e) {
			throw e;
		} catch (Exception e) {
			logger.error("Some errors not handled", e);
			throw new RDAPError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
		}
	}

	@RequestMapping(value = "/{nameserverName}", method = RequestMethod.HEAD, produces = Controllers.CONTENT_TYPE)
	public ResponseEntity<Void> head(@PathVariable("nameserverName") final String nameserverName) throws RDAPError {
		logger.debug("Query(HEAD) for nameserver {}", nameserverName);
		final DomainName domainName;
		try {
			domainName = DomainName.of(nameserverName);
			Nameserver nameserver = nameserverService.getNameserver(domainName);
			if (nameserver == null) {
				logger.debug("Query(HEAD) result for {} is null. Throwing NameserverNotFound Error", nameserverName);
				throw RDAPError.nameserverNotFound(domainName);
			}
			return new ResponseEntity<Void>(null, new HttpHeaders(), HttpStatus.OK);
		} catch (LabelException.IDNParseException e) {
			List<String> description = new ArrayList<String>(e.getErrors().size());
			for (IDNA.Error error : e.getErrors()) {
				description.add(error.name());
			}
			throw new RDAPError(400, "Invalid nameserver name", description, e);
		} catch (RDAPError e) {
			throw e;
		} catch (Exception e) {
			logger.error("Some errors not handled", e);
			throw new RDAPError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
		}
	}
	
	@ExceptionHandler(value = RDAPError.NotAuthoritative.class)
	@ResponseBody
	protected RDAPError handleResourceNotFoundException(RDAPError.NotAuthoritative error, HttpServletResponse response)
			throws UnsupportedEncodingException {
		response.setStatus(error.getErrorCode());
		String location = baseRedirectURL + "/nameserver/" + URLEncoder.encode(error.getDomainName(), "UTF-8");
		response.addHeader(Controllers.LOCATION_HEADER, location);
		return error;
	}
	
	
	@RequestMapping(value = "/{nameserverName}", method = { RequestMethod.DELETE, RequestMethod.PUT,
			RequestMethod.OPTIONS, RequestMethod.PATCH, RequestMethod.POST,
			RequestMethod.TRACE }, produces = Controllers.CONTENT_TYPE)
	@ResponseBody
	public Nameserver any(@PathVariable("nameserverName") final String nameserverName) throws RDAPError {
		throw RDAPError.methodNotAllowed();
	}

}
