package be.dnsbelgium.rdap;

import be.dnsbelgium.rdap.core.*;
import be.dnsbelgium.rdap.core.RDAPError;
import be.dnsbelgium.rdap.service.AutNumService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(value = "autnum")
public final class AutNumController extends AbstractController {

  private final Logger logger = LoggerFactory.getLogger(AutNumController.class);

  @Autowired
  private AutNumService autNumService;

  @RequestMapping(value = "/{autnum}", method = RequestMethod.GET, produces = Controllers.CONTENT_TYPE)
  @ResponseBody
  public AutNum get(@PathVariable("autnum") int autNum) throws RDAPError {
    AutNum result;
    try {
      result = autNumService.getAutNum(autNum);
      if (result == null) {
        logger.debug("AutNum result for {} is null. Throwing AutNumNotFound Error");
        throw new RDAPError.AutNumNotFound(autNum);
      } else {
        result.addRdapConformance(AutNum.DEFAULT_RDAP_CONFORMANCE);
      }
    } catch (RDAPError e) {
      throw e;
    } catch (Exception e) {
      logger.error("Some errors not handled", e);
      throw new RDAPError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
    }
    return result;
  }
}
