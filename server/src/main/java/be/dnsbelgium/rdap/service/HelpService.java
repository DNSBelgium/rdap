package be.dnsbelgium.rdap.service;

import be.dnsbelgium.rdap.core.Help;
import be.dnsbelgium.rdap.core.RDAPError;

public interface HelpService {
  Help getHelp() throws RDAPError;
}
