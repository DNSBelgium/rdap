package be.dnsbelgium.rdap.service.impl;

import be.dnsbelgium.core.DomainName;
import be.dnsbelgium.rdap.core.Nameserver;
import be.dnsbelgium.rdap.core.NameserversSearchResult;
import be.dnsbelgium.rdap.core.RDAPError;
import be.dnsbelgium.rdap.service.NameserverService;

public class DefaultNameserverService implements NameserverService {

  @Override
  public final Nameserver getNameserver(DomainName domainName) throws RDAPError {
    Nameserver toReturn = getNameserverImpl(domainName);
    if (toReturn != null) {
      toReturn.addRdapConformance(Nameserver.DEFAULT_RDAP_CONFORMANCE);
    }
    return toReturn;
  }

  @Override
  public final NameserversSearchResult searchByName(String name) throws RDAPError {
    NameserversSearchResult toReturn = searchByNameImpl(name);
    if (toReturn != null) {
      toReturn.addRdapConformance(Nameserver.DEFAULT_RDAP_CONFORMANCE);
    }
    return toReturn;
  }

  @Override
  public NameserversSearchResult searchByIp(String ip) throws RDAPError {
    NameserversSearchResult toReturn = searchByIpImpl(ip);
    if (toReturn != null) {
      toReturn.addRdapConformance(Nameserver.DEFAULT_RDAP_CONFORMANCE);
    }
    return toReturn;
  }

  public Nameserver getNameserverImpl(DomainName domainName) throws RDAPError {
    throw RDAPError.notImplemented();
  }

  public NameserversSearchResult searchByNameImpl(String name) throws RDAPError {
    throw RDAPError.notImplemented();
  }

  public NameserversSearchResult searchByIpImpl(String ip) throws RDAPError {
    throw RDAPError.notImplemented();
  }
}

