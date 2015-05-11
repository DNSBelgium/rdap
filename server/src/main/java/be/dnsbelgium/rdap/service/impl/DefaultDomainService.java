package be.dnsbelgium.rdap.service.impl;

import be.dnsbelgium.core.DomainName;
import be.dnsbelgium.rdap.core.Domain;
import be.dnsbelgium.rdap.core.DomainsSearchResult;
import be.dnsbelgium.rdap.core.RDAPError;
import be.dnsbelgium.rdap.service.DomainService;

public class DefaultDomainService implements DomainService {
  @Override
  public final Domain getDomain(DomainName domainName) throws RDAPError {
    Domain toReturn = getDomainImpl(domainName);
    if (toReturn != null) {
      toReturn.addRdapConformance(Domain.DEFAULT_RDAP_CONFORMANCE);
    }
    return toReturn;
  }

  @Override
  public final DomainsSearchResult searchDomainsByName(String name) throws RDAPError {
    DomainsSearchResult toReturn = searchDomainsByNameImpl(name);
    if (toReturn != null) {
      toReturn.addRdapConformance(Domain.DEFAULT_RDAP_CONFORMANCE);
    }
    return toReturn;
  }

  @Override
  public final DomainsSearchResult searchDomainsByNsLdhName(String nsLdhName) throws RDAPError {
    DomainsSearchResult toReturn = searchDomainsByNsLdhNameImpl(nsLdhName);
    if (toReturn != null) {
      toReturn.addRdapConformance(Domain.DEFAULT_RDAP_CONFORMANCE);
    }
    return toReturn;
  }

  @Override
  public final DomainsSearchResult searchDomainsByNsIp(String nsIp) throws RDAPError {
    DomainsSearchResult toReturn = searchDomainsByNsIpImpl(nsIp);
    if (toReturn != null) {
      toReturn.addRdapConformance(Domain.DEFAULT_RDAP_CONFORMANCE);
    }
    return toReturn;
  }

  public Domain getDomainImpl(DomainName domainName) throws RDAPError {
    throw RDAPError.notImplemented();
  }

  public DomainsSearchResult searchDomainsByNameImpl(String name) throws RDAPError {
    throw RDAPError.notImplemented();
  }

  public DomainsSearchResult searchDomainsByNsLdhNameImpl(String nsLdhName) throws RDAPError {
    throw RDAPError.notImplemented();
  }

  public DomainsSearchResult searchDomainsByNsIpImpl(String nsIp) throws RDAPError {
    throw RDAPError.notImplemented();
  }
}
