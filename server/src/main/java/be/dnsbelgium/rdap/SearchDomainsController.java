package be.dnsbelgium.rdap;

import be.dnsbelgium.rdap.core.Domain;
import be.dnsbelgium.rdap.core.DomainsSearchResult;
import be.dnsbelgium.rdap.core.Error;
import be.dnsbelgium.rdap.service.DomainService;
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
@RequestMapping(value = "domains")
public class SearchDomainsController extends AbstractController {

  private final static Logger logger = LoggerFactory.getLogger(SearchDomainsController.class);

  @Autowired
  private DomainService domainService;

  @RequestMapping(value = "/{partialDomainName}", method = RequestMethod.GET, produces = Controllers.CONTENT_TYPE)
  @ResponseBody
  public DomainsSearchResult search(@PathVariable("partialDomainName") final String partialDomainName) throws Error {
    try {
      DomainsSearchResult domains = domainService.searchDomains(partialDomainName);
      if (domains == null) {
        logger.debug("Domain search result for '{}' is null. Throwing DomainNotFound Error", partialDomainName);
        throw new Error.NotAuthoritative(partialDomainName);
      } else {
        domains.addRdapConformance(Domain.DEFAULT_RDAP_CONFORMANCE);
      }
      return domains;
    } catch (Error e) {
      throw e;
    } catch (Exception e) {
      logger.error("Some errors not handled", e);
      throw new Error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
    }
  }
}
