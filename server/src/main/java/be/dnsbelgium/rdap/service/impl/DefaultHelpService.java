package be.dnsbelgium.rdap.service.impl;

import be.dnsbelgium.rdap.core.Domain;
import be.dnsbelgium.rdap.core.Help;
import be.dnsbelgium.rdap.core.RDAPError;
import be.dnsbelgium.rdap.service.HelpService;

public class DefaultHelpService implements HelpService {
  @Override
  public final Help getHelp() throws RDAPError {
    Help toReturn = getHelpImpl();
    if (toReturn != null) {
      toReturn.addRdapConformance(Domain.DEFAULT_RDAP_CONFORMANCE);
    }
    return toReturn;
  }

  public Help getHelpImpl() throws RDAPError {
    throw RDAPError.notImplemented();
  }
}
