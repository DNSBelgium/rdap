package be.dnsbelgium.rdap;

import be.dnsbelgium.rdap.core.*;
import be.dnsbelgium.rdap.core.RDAPError;
import be.dnsbelgium.rdap.service.EntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "entities")
public class SearchEntitiesController extends AbstractController {

  private final static Logger logger = LoggerFactory.getLogger(SearchEntitiesController.class);

  @Autowired
  private EntityService entityService;

  @RequestMapping(value = "/{partialHandle}", method = RequestMethod.GET, produces = Controllers.CONTENT_TYPE)
  @ResponseBody
  public EntitiesSearchResult search(@PathVariable("partialHandle") final String partialHandle) throws RDAPError {
    try {
      EntitiesSearchResult result = entityService.search(partialHandle);
      if (result == null) {
        logger.debug("Entity search result for '{}' is null. Throwing NotAuthoritative Error", partialHandle);
        throw new RDAPError.NotAuthoritative(partialHandle);
      } else {
        result.addRdapConformance(Entity.DEFAULT_RDAP_CONFORMANCE);
      }
      return result;
    } catch(RDAPError e) {
      throw e;
    } catch(Exception e) {
      logger.error("Some errors not handled", e);
      throw new RDAPError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
    }
  }
}
