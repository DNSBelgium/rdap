package be.dnsbelgium.rdap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySource(value= {"classpath:application.properties"})
public class ApplicationPropertiesConfig {
}
