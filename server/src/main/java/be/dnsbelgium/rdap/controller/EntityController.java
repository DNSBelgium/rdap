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
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(value = "entity")
public class EntityController {

	private final static Logger logger = LoggerFactory.getLogger(EntityController.class);

	private final EntityService entityService;

	@Autowired
	public EntityController(EntityService entityService) {
		this.entityService = entityService;
	}

	@RequestMapping(value = "/{handle}", method = RequestMethod.GET)
	@ResponseBody
	public Entity get(@PathVariable("handle") final String handle) throws RDAPError {
		logger.debug("Query(GET) for entity with handle: {}", handle);
		Entity entity = entityService.getEntity(handle);
		if (entity == null) {
			logger.debug("Entity result for {} is null. Throwing EntityNotFound Error", handle);
			throw RDAPError.entityNotFound(handle);
		}
		return entity;
	}
	
	@RequestMapping(value = "/{handle}", method = RequestMethod.HEAD)
	public ResponseEntity<Void> head(@PathVariable("handle") final String handle) throws RDAPError {
		logger.debug("Query(HEAD) for entity with handle: {}", handle);
		Entity entity = entityService.getEntity(handle);
		if (entity == null) {
			logger.debug("Entity result for {} is null. Throwing EntityNotFound Error", handle);
			throw RDAPError.entityNotFound(handle);
		}
		return new ResponseEntity<Void>(null, new HttpHeaders(), HttpStatus.OK);
	}

	@RequestMapping(value = "/{handle}", method = { RequestMethod.DELETE, RequestMethod.PUT, RequestMethod.OPTIONS,
			RequestMethod.PATCH, RequestMethod.POST, RequestMethod.TRACE })
	@ResponseBody
	public Entity any(@PathVariable("handle") final String handle) throws RDAPError {
		throw RDAPError.methodNotAllowed();
	}


}
