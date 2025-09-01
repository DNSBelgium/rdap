package be.dnsbelgium.rdap.controller;

import be.dnsbelgium.rdap.core.EntitiesSearchResult;
import be.dnsbelgium.rdap.core.Nameserver;
import be.dnsbelgium.rdap.core.RDAPError;
import be.dnsbelgium.rdap.service.EntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "entities")
public class SearchEntitiesController {

	private final EntityService entityService;

	@Autowired
	public SearchEntitiesController(EntityService entityService) {
		this.entityService = entityService;
	}

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public EntitiesSearchResult search(@RequestParam(value = "fn", required = false) final String fn,
			@RequestParam(value = "handle", required = false) final String handle) throws RDAPError {
		EntitiesSearchResult result = null;
		String query = checkParams(fn, handle);
		if (fn != null) {
			result = entityService.searchByFn(fn);
		}
		if (handle != null) {
			result = entityService.searchByHandle(handle);
		}
		if (result == null || result.entitySearchResults == null || result.entitySearchResults.isEmpty()) {
			throw RDAPError.noResults(query);
		}
		return result;
	}

	@RequestMapping(method = { RequestMethod.DELETE, RequestMethod.PUT, RequestMethod.OPTIONS, RequestMethod.PATCH,
			RequestMethod.POST, RequestMethod.TRACE })
	@ResponseBody
	public Nameserver any(@RequestParam(value = "fn", required = false) final String fn,
			@RequestParam(value = "handle", required = false) final String handle) throws RDAPError {
		throw RDAPError.methodNotAllowed();
	}

	private String checkParams(String fn, String handle) throws RDAPError {
		if (fn == null && handle == null) {
			throw RDAPError.badRequest("Param missing", "One and only one of 'fn' or 'handle' should be provided");
		}
		if (fn != null && handle != null) {
			throw RDAPError.badRequest("Too many params", "One and only one of 'fn' or 'handle' should be provided");
		}
		return fn != null ? fn : handle;
	}
}
