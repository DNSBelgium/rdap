package be.dnsbelgium.rdap;

import be.dnsbelgium.rdap.core.Entity;
import be.dnsbelgium.rdap.core.RDAPError;
import be.dnsbelgium.rdap.service.EntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping(value = "entity")
public class EntityController {

  private final static Logger logger = LoggerFactory.getLogger(EntityController.class);

  private String baseRedirectURL;

  private int redirectThreshold;

  @Autowired
  public EntityController(
          @Value("#{applicationProperties['baseRedirectURL']}") String baseRedirectURL,
          @Value("#{applicationProperties['redirectThreshold']}") int redirectThreshold) {
    this.baseRedirectURL = baseRedirectURL;
    this.redirectThreshold = redirectThreshold;
  }

  @Autowired
  private EntityService entityService;

  @RequestMapping(value = "/{handle}", method = RequestMethod.GET, produces = Controllers.CONTENT_TYPE)
  @ResponseBody
  public Entity get(@PathVariable("handle") final String handle) throws RDAPError {
    logger.debug("Query for entity with handle: {}", handle);
    Entity entity = entityService.getEntity(handle);
    if (entity == null) {
      logger.debug("Entity result for {} is null. Throwing EntityNotFound Error", handle);
      throw RDAPError.entityNotFound(handle);
    }
    return entity;
  }

  public int getRedirectThreshold() {
    return redirectThreshold;
  }
}
