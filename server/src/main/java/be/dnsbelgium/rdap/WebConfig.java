/**
 * Copyright 2014 DNS Belgium vzw
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.dnsbelgium.rdap;

import be.dnsbelgium.rdap.controller.Controllers;
import be.dnsbelgium.rdap.jackson.CustomObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;

@Configuration
public class WebConfig extends WebMvcConfigurationSupport {

  @Override
  public void configureMessageConverters(final List<HttpMessageConverter<?>> converters) {
    converters.add(converter());
  }

  @Override
  protected void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    super.configureContentNegotiation(configurer);
    // make sure spring does not interpret extensions of domain name (.com)
    configurer.favorPathExtension(false);
    // make sure spring returns "application/rdap+json;charset=UTF-8", would be "application/json" without this
    // see https://github.com/spring-projects/spring-framework/issues/21927
    configurer.defaultContentType(MediaType.valueOf(Controllers.CONTENT_TYPE));
  }

  @Bean
  MappingJackson2HttpMessageConverter converter() {
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
    converter.setObjectMapper(getObjectMapper());
    return converter;
  }

  @Bean
  public ObjectMapper getObjectMapper() {
    return new CustomObjectMapper();
  }

  @Bean
  public RequestMappingHandlerMapping requestMappingHandlerMapping() {
    RequestMappingHandlerMapping handlerMapping = super.requestMappingHandlerMapping();
    handlerMapping.setUseSuffixPatternMatch(false);
    handlerMapping.setUseTrailingSlashMatch(false);
    return handlerMapping;
  }
}
