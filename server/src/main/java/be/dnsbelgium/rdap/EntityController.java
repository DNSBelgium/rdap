package be.dnsbelgium.rdap;

import be.dnsbelgium.rdap.core.Entity;
import be.dnsbelgium.rdap.core.Error;
import be.dnsbelgium.rdap.service.EntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(value = "entity")
public class EntityController extends AbstractController {

  private final static Logger logger = LoggerFactory.getLogger(EntityController.class);

  private final String baseRedirectURL;

  private final int redirectThreshold;

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
  public Entity get(@PathVariable("handle") final String handle) throws Error {
    logger.debug("Query for entity with handle: {}", handle);
    Entity entity;
    try {
      entity = entityService.getEntity(handle);
      if (entity == null) {
        logger.debug("Entity result for {} is null. Throwing EntityNotFound Error", handle);
        throw new Error.EntityNotFound(handle);
      } else {
        entity.addRdapConformance(Entity.DEFAULT_RDAP_CONFORMANCE);
      }
    } catch (Error e) {
      throw e;
    } catch (Exception e) {
      logger.error("Some errors not handled", e);
      throw new Error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
    }
    return entity;
  }

  public int getRedirectThreshold() {
    return redirectThreshold;
  }
}
