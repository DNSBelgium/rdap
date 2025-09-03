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

import be.dnsbelgium.rdap.jackson.CustomObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.util.UrlPathHelper;

import java.util.List;

@Configuration
public class WebConfig extends WebMvcConfigurationSupport {

  @Override
  protected void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/robots.txt").addResourceLocations("classpath:/static/");
  }

  @Override
  public void configureMessageConverters(final List<HttpMessageConverter<?>> converters) {
    converters.add(converter());
  }

  @Override
  protected void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    super.configureContentNegotiation(configurer);

    /*
     * The RDAP RFC (RFC 7480) includes the following requirement regarding the Content-Type header of responses:
     *
     *    | 4.2.  Accept Header
     *    | To indicate to servers that an RDAP response is desired, clients
     *    | include an Accept header field with an RDAP-specific JSON media type,
     *    | the generic JSON media type, or both.  Servers receiving an RDAP
     *    | request return an entity with a Content-Type header containing the
     *    | RDAP-specific JSON media type.
     *    | This specification does not define the responses a server returns to
     *    | a request with any other media types in the Accept header field, or
     *    | with no Accept header field.  One possibility would be to return a
     *    | response in a media type suitable for rendering in a web browser.
     *
     * So for an Accept header of "application/rdap+json" or "application/json", we need to
     * always return Content-Type "application/rdap+json"; for any other Accept header, we can choose.
     *
     * Since Spring will automatically respond with "application/json" if the Accept header is "application/json",
     * and will only automatically respond with "application/rdap+json" if the Accept header is exactly that,
     * we need to ignore the Accept header altogether. We then always respond with Content-Type "application/rdap+json",
     * since this is always valid for any kind of request, according to the RFC.
     */
    configurer
        .ignoreAcceptHeader(true)
        .defaultContentType(RdapMediaType.APPLICATION_RDAP_JSON_UTF8);
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

  @Override
  public void configurePathMatch(PathMatchConfigurer configurer) {
    UrlPathHelper urlPathHelper = new UrlPathHelper();
    urlPathHelper.setUrlDecode(false);
    configurer.setUrlPathHelper(urlPathHelper);
  }

}
