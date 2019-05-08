package be.dnsbelgium.rdap;

import be.dnsbelgium.rdap.service.*;
import be.dnsbelgium.rdap.service.impl.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultServiceConfig {
  @Bean
  public DomainService getDomainService() {
  return new DefaultDomainService();
}

  @Bean
  public NameserverService getNameserverService() {
    return new DefaultNameserverService();
  }

  @Bean
  public EntityService getEntityService() {
    return new DefaultEntityService();
  }

  @Bean
  public IPService getIPService() {
    return new DefaultIPService();
  }

  @Bean
  public AutNumService getAutNumService() {
    return new DefaultAutNumService();
  }

  @Bean
  public HelpService getHelpService() {
    return new DefaultHelpService();
  }
}
