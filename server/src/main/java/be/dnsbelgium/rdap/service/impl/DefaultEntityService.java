package be.dnsbelgium.rdap.service.impl;

import be.dnsbelgium.rdap.core.EntitiesSearchResult;
import be.dnsbelgium.rdap.core.Entity;
import be.dnsbelgium.rdap.core.RDAPError;
import be.dnsbelgium.rdap.service.EntityService;

public class DefaultEntityService implements EntityService {

  @Override
  public final Entity getEntity(String handle) throws RDAPError {
    Entity toReturn = getEntityImpl(handle);
    if (toReturn != null) {
      toReturn.addRdapConformance(Entity.DEFAULT_RDAP_CONFORMANCE);
    }
    return toReturn;
  }

  @Override
  public EntitiesSearchResult searchByFn(String fn) throws RDAPError {
    EntitiesSearchResult toReturn = searchByFnImpl(fn);
    if (toReturn != null) {
      toReturn.addRdapConformance(Entity.DEFAULT_RDAP_CONFORMANCE);
    }
    return toReturn;
  }

  @Override
  public final EntitiesSearchResult searchByHandle(String handle) throws RDAPError {
    EntitiesSearchResult toReturn = searchByHandleImpl(handle);
    if (toReturn != null) {
      toReturn.addRdapConformance(Entity.DEFAULT_RDAP_CONFORMANCE);
    }
    return toReturn;
  }

  public Entity getEntityImpl(String handle) throws RDAPError {
    throw RDAPError.notImplemented();
  }

  public EntitiesSearchResult searchByFnImpl(String fn) throws RDAPError {
    throw RDAPError.notImplemented();
  }

  public EntitiesSearchResult searchByHandleImpl(String handle) throws RDAPError {
    throw RDAPError.notImplemented();
  }
}
