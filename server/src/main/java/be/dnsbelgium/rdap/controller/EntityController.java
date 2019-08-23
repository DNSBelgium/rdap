package be.dnsbelgium.rdap.controller;

import be.dnsbelgium.rdap.core.Entity;
import be.dnsbelgium.rdap.core.RDAPError;
import be.dnsbelgium.rdap.service.EntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(value = "entity")
public class EntityController {

	private final static Logger logger = LoggerFactory.getLogger(EntityController.class);

	private final EntityService entityService;

	@Autowired
	public EntityController(EntityService entityService) {
		this.entityService = entityService;
	}

	@RequestMapping(value = {"/{handle}", "{handle}/**"}, method = RequestMethod.GET, produces = Controllers.CONTENT_TYPE)
	@ResponseBody
	public Entity get(@PathVariable("handle") final String handle, HttpServletRequest request) throws RDAPError {
		// EntityName can contain slashes in their name
		final String entityName = getBestMatchingEntityName(handle, request);

		logger.debug("Query(GET) for entity with handle: {}", entityName);
		Entity entity = entityService.getEntity(entityName);
		if (entity == null) {
			logger.debug("Entity result for {} is null. Throwing EntityNotFound Error", entityName);
			throw RDAPError.entityNotFound(entityName);
		}
		return entity;
	}

	@RequestMapping(value = {"/{handle}", "{handle}/**"}, method = RequestMethod.HEAD, produces = Controllers.CONTENT_TYPE)
	public ResponseEntity<Void> head(@PathVariable("handle") final String handle, HttpServletRequest request) throws RDAPError {
		// EntityName can contain slashes in their name
		final String entityName = getBestMatchingEntityName(handle, request);

		logger.debug("Query(HEAD) for entity with handle: {}", entityName);
		Entity entity = entityService.getEntity(entityName);
		if (entity == null) {
			logger.debug("Entity result for {} is null. Throwing EntityNotFound Error", entityName);
			throw RDAPError.entityNotFound(entityName);
		}
		return new ResponseEntity<Void>(null, new HttpHeaders(), HttpStatus.OK);
	}

	@RequestMapping(value = {"/{handle}", "{handle}/**"}, method = { RequestMethod.DELETE, RequestMethod.PUT, RequestMethod.OPTIONS,
			RequestMethod.PATCH, RequestMethod.POST, RequestMethod.TRACE }, produces = Controllers.CONTENT_TYPE)
	@ResponseBody
	public Entity any(@PathVariable("handle") final String handle, HttpServletRequest request) throws RDAPError {
		// EntityName can contain slashes in their name
		final String entityName = getBestMatchingEntityName(handle, request);

		throw RDAPError.methodNotAllowed();
	}

	private String getBestMatchingEntityName(@PathVariable("handle") String handle, HttpServletRequest request) {
		final String path = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
		final String bestMatchingPattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString();
		final String arguments = new AntPathMatcher().extractPathWithinPattern(bestMatchingPattern, path);

		return arguments.isEmpty() ? handle : handle + '/' + arguments;
	}

}
