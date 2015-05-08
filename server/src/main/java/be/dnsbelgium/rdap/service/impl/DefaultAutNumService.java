package be.dnsbelgium.rdap.service.impl;

import be.dnsbelgium.rdap.core.AutNum;
import be.dnsbelgium.rdap.core.RDAPError;
import be.dnsbelgium.rdap.service.AutNumService;

public class DefaultAutNumService implements AutNumService {
  @Override
  public final AutNum getAutNum(int autNum) throws RDAPError {
    AutNum toReturn = getAutNumImpl(autNum);
    if (toReturn != null) {
      toReturn.addRdapConformance(AutNum.DEFAULT_RDAP_CONFORMANCE);
    }
    return toReturn;
  }

  public AutNum getAutNumImpl(int autNum) throws RDAPError {
    throw RDAPError.notImplemented();
  }
}
