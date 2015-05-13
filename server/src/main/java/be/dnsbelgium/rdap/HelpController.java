package be.dnsbelgium.rdap;

import be.dnsbelgium.rdap.core.Help;
import be.dnsbelgium.rdap.core.RDAPError;
import be.dnsbelgium.rdap.service.HelpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "help")
public final class HelpController {

  private final static Logger logger = LoggerFactory.getLogger(HelpController.class);

  @Autowired
  private HelpService helpService;

  @RequestMapping(method = RequestMethod.GET, produces = Controllers.CONTENT_TYPE)
  @ResponseBody
  public Help get() throws RDAPError {
    Help help = helpService.getHelp();
    if (help == null) {
      throw RDAPError.helpNotFound();
    }
    return help;
  }
}
