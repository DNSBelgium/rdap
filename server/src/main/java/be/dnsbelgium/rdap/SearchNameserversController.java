package be.dnsbelgium.rdap;

import be.dnsbelgium.rdap.core.*;
import be.dnsbelgium.rdap.core.RDAPError;
import be.dnsbelgium.rdap.service.NameserverService;
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
@RequestMapping(value = "nameservers")
public class SearchNameserversController extends AbstractController {

  private final static Logger logger = LoggerFactory.getLogger(SearchNameserversController.class);

  @Autowired
  private NameserverService nameserverService;

  @RequestMapping(value = "/{partialNameserverName}", method = RequestMethod.GET, produces = Controllers.CONTENT_TYPE)
  @ResponseBody
  public NameserversSearchResult search(@PathVariable("partialNameserverName") String partialNameserverName) throws RDAPError {
    try {
      NameserversSearchResult result = nameserverService.searchNameservers(partialNameserverName);
      if (result == null) {
        logger.debug("Nameserver search result for '{}' is null. Throwing DomainNotFound Error", partialNameserverName);
        throw new RDAPError.NotAuthoritative(partialNameserverName);
      } else {
        result.addRdapConformance(Domain.DEFAULT_RDAP_CONFORMANCE);
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
