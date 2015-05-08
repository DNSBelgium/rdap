package be.dnsbelgium.rdap.service.impl;

import be.dnsbelgium.core.CIDR;
import be.dnsbelgium.rdap.core.IPNetwork;
import be.dnsbelgium.rdap.core.RDAPError;
import be.dnsbelgium.rdap.service.IPService;

public class DefaultIPService implements IPService {
  @Override
  public final IPNetwork getIPNetwork(CIDR cidr) throws RDAPError {
    IPNetwork toReturn = getIPNetworkImpl(cidr);
    if (toReturn != null) {
      toReturn.addRdapConformance(IPNetwork.DEFAULT_RDAP_CONFORMANCE);
    }
    return toReturn;
  }

  public IPNetwork getIPNetworkImpl(CIDR cidr) throws RDAPError {
    throw RDAPError.notImplemented();
  }
}
