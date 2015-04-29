package be.dnsbelgium.rdap;

import be.dnsbelgium.rdap.core.Domain;
import be.dnsbelgium.rdap.core.DomainsSearchResult;
import be.dnsbelgium.rdap.core.RDAPError;
import be.dnsbelgium.rdap.service.DomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(value = "domains")
public class SearchDomainsController extends AbstractController {

  private final static Logger logger = LoggerFactory.getLogger(SearchDomainsController.class);

  @Autowired
  private DomainService domainService;

  @RequestMapping(method = RequestMethod.GET, produces = Controllers.CONTENT_TYPE)
  @ResponseBody
  public DomainsSearchResult search(
          @RequestParam(value = "name", required = false) final String name,
          @RequestParam(value = "nsLdhName", required = false) final String nsLdhName,
          @RequestParam(value = "nsIp", required = false) final String nsIp) throws RDAPError {
    try {
      checkParams(name, nsLdhName, nsIp);
      if (name != null) {
        return handleByNameSearch(name);
      }
      if (nsLdhName != null) {
        return handleByNsLdhNameSearch(nsLdhName);
      }
      return handleByNsIpSearch(nsIp);
    } catch (RDAPError e) {
      throw e;
    } catch (Exception e) {
      logger.error("Some errors not handled", e);
      throw new RDAPError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
    }
  }

  private DomainsSearchResult handleByNsIpSearch(String nsIp) throws RDAPError {
    DomainsSearchResult domains = domainService.searchDomainsByNsIp(nsIp);
    if (domains == null) {
      logger.debug("Domain search (by nameserver ip) result is null. Throwing NotAuthoritative Error");
      throw new RDAPError.NotAuthoritative(nsIp);
    } else {
      domains.addRdapConformance(Domain.DEFAULT_RDAP_CONFORMANCE);
    }
    return domains;
  }

  private DomainsSearchResult handleByNsLdhNameSearch(String nsLdhName) throws RDAPError {
    DomainsSearchResult domains = domainService.searchDomainsByNsLdhName(nsLdhName);
    if (domains == null) {
      logger.debug("Domain search (by nameserver name) result is null. Throwing NotAuthoritative Error");
      throw new RDAPError.NotAuthoritative(nsLdhName);//TODO check this: should be None found?
    } else {
      domains.addRdapConformance(Domain.DEFAULT_RDAP_CONFORMANCE);
    }
    return domains;
  }

  private DomainsSearchResult handleByNameSearch(String name) throws RDAPError {
    DomainsSearchResult domains = domainService.searchDomainsByName(name);
    if (domains == null) {
      logger.debug("Domain search result for '{}' is null. Throwing NotAuthoritative Error", name);
      throw new RDAPError.NotAuthoritative(name);
    } else {
      domains.addRdapConformance(Domain.DEFAULT_RDAP_CONFORMANCE);
    }
    return domains;
  }

  private void checkParams(String name, String nsLdhName, String nsIp) throws RDAPError {
    if (name == null && nsLdhName == null && nsIp == null) {
      throw new RDAPError.BadRequest();
    }
    int paramCount = 0;
    if (name != null) {
      paramCount++;
    }
    if (nsLdhName != null) {
      paramCount++;
    }
    if (nsIp != null) {
      paramCount++;
    }
    if (paramCount > 1) {
      throw new RDAPError.BadRequest();
    }
  }
}
