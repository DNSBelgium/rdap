package be.dnsbelgium.rdap.controller;

import be.dnsbelgium.rdap.core.AutNum;
import be.dnsbelgium.rdap.core.RDAPError;
import be.dnsbelgium.rdap.service.AutNumService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "autnum")
public class AutNumController {

  private final Logger logger = LoggerFactory.getLogger(AutNumController.class);

  private final AutNumService autNumService;

  @Autowired
  public AutNumController(AutNumService autNumService) {
    this.autNumService = autNumService;
  }

  @RequestMapping(value = "/{autnum}", method = RequestMethod.GET, produces = Controllers.CONTENT_TYPE)
  @ResponseBody
  public AutNum get(@PathVariable("autnum") int autNum) throws RDAPError {
    AutNum result = autNumService.getAutNum(autNum);
    if (result == null) {
      logger.debug("AutNum result for {} is null. Throwing AutNumNotFound Error");
      throw RDAPError.autNumNotFound(autNum);
    }
    return result;
  }
}
