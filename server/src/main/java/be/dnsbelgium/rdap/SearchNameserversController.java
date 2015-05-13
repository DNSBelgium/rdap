package be.dnsbelgium.rdap;

import be.dnsbelgium.rdap.core.NameserversSearchResult;
import be.dnsbelgium.rdap.core.RDAPError;
import be.dnsbelgium.rdap.service.NameserverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "nameservers")
public class SearchNameserversController {

  private final static Logger logger = LoggerFactory.getLogger(SearchNameserversController.class);

  @Autowired
  private NameserverService nameserverService;

  @RequestMapping(method = RequestMethod.GET, produces = Controllers.CONTENT_TYPE)
  @ResponseBody
  public NameserversSearchResult search(@RequestParam(value = "name", required = false) final String name,
                                        @RequestParam(value = "ip", required = false) final String ip) throws RDAPError {
    NameserversSearchResult result = null;
    String query = checkParams(name, ip);
    if (name != null) {
      result = nameserverService.searchByName(name);
    }
    if (ip != null) {
      result = nameserverService.searchByIp(ip);
    }
    if (result == null || result.nameserverSearchResults == null || result.nameserverSearchResults.isEmpty()) {
      throw RDAPError.noResults(query);
    }
    return result;
  }

  private String checkParams(String name, String ip) throws RDAPError {
    if (name == null && ip == null) {
      throw RDAPError.badRequest("Param missing", "One and only one of 'name' or 'ip' should be provided");
    }
    if (name != null && ip != null) {
      throw RDAPError.badRequest("Too many params", "One and only one of 'name' or 'ip' should be provided");
    }
    return name != null ? name : ip;
  }
}
