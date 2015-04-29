package be.dnsbelgium.rdap;

import be.dnsbelgium.rdap.core.*;
import be.dnsbelgium.rdap.core.RDAPError;
import be.dnsbelgium.rdap.service.HelpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "help")
public final class HelpController extends AbstractController {

  private final static Logger logger = LoggerFactory.getLogger(HelpController.class);

  @Autowired
  private HelpService helpService;

  @RequestMapping(method = RequestMethod.GET, produces = Controllers.CONTENT_TYPE)
  @ResponseBody
  public Help get() throws RDAPError {
    try {
      Help help = helpService.getHelp();
      if (help == null) {
        throw new RDAPError.HelpNotFound();
      } else {
        help.addRdapConformance(Domain.DEFAULT_RDAP_CONFORMANCE);
      }
      return help;
    } catch (Exception e) {
      logger.error("Some errors not handled", e);
      throw new RDAPError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
    }
  }
}
