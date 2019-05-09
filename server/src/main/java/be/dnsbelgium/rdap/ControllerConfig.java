package be.dnsbelgium.rdap;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "be.dnsbelgium.rdap.controller")
public class ControllerConfig {
}
