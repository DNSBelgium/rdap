package be.dns.rdap;

/*
 * #%L
 * Server
 * %%
 * Copyright (C) 2013 DNS Belgium vzw
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import be.dns.core.DomainName;
import be.dns.rdap.core.Domain;
import be.dns.rdap.jackson.CustomObjectMapper;
import be.dns.rdap.service.DomainService;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.Resource;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = DomainControllerTest.Config.class)
public class DomainControllerTest {

  private final static int REDIRECT_THRESHOLD = 3;
  private final static String REDIRECT_URL = "https://rdap.org";

  @Configuration
  static class Config extends WebMvcConfigurationSupport {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
      converters.add(converter());
    }

    @Bean
    MappingJacksonHttpMessageConverter converter() {
      MappingJacksonHttpMessageConverter converter = new MappingJacksonHttpMessageConverter();
      converter.setObjectMapper(new CustomObjectMapper());
      return converter;
    }

    @Bean
    public DomainService domainService() {
      return Mockito.mock(DomainService.class);
    }

    @Bean
    public DomainController domainController() {
      return new DomainController(REDIRECT_URL, REDIRECT_THRESHOLD);
    }

    @Bean
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
      RequestMappingHandlerMapping handlerMapping = super.requestMappingHandlerMapping();
      handlerMapping.setUseSuffixPatternMatch(false);
      handlerMapping.setUseTrailingSlashMatch(false);
      return handlerMapping;
    }
  }

  @Autowired
  DomainController domainController;

  @Autowired
  DomainService domainService;

  @Resource
  private WebApplicationContext webApplicationContext;

  @After
  public void resetMock() {
    Mockito.reset(domainService);
  }

  @Test
  public void testNotFound() throws Exception {
    Mockito.when(domainService.getDomain(Mockito.any(DomainName.class))).thenReturn(null);
    MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    mockMvc.perform(get("/domain/example")).andExpect(status().isNotFound());
  }

  @Test
  public void testDefault() throws Exception {
    Mockito.when(domainService.getDomain(Mockito.any(DomainName.class))).thenReturn(new Domain.Builder().setLDHName("example.com").build());
    MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    mockMvc.perform(get("/domain/example")).andExpect(status().isOk()).andExpect(jsonPath("$.ldhName", "example.com").exists());
  }

  @Test
  public void testBytes() throws Exception {
    Mockito.when(domainService.getDomain(Mockito.any(DomainName.class))).thenReturn(new Domain.Builder().setLDHName("example.com").build());
    MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    mockMvc.perform(get("/domain/example")
        .accept(MediaType.parseMediaType("application/rdap+json")))
        .andExpect(header().string("Content-type", "application/rdap+json;charset=UTF-8"))
        .andExpect(status().isOk())
        .andExpect(content().string("{\"rdapConformance\":[\"rdap_level_0\"],\"ldhName\":\"example.com\"}"));
  }

  @Test
  public void testWrongMediaType() throws Exception {
    Mockito.when(domainService.getDomain(Mockito.any(DomainName.class))).thenReturn(new Domain.Builder().setLDHName("example.com").build());
    MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    mockMvc.perform(get("/domain/example")
        .accept(MediaType.TEXT_HTML))
        .andExpect(status().isNotAcceptable());
  }

  @Test
  public void testNotAuthoritative() throws Exception {
    Mockito
        .when(domainService.getDomain(Mockito.any(DomainName.class)))
        .thenThrow(new DomainController.NotAuthoritativeException(DomainName.of("example.com")));
    MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    mockMvc.perform(get("/domain/example")
        .accept(MediaType.parseMediaType("application/rdap+json")))
        .andExpect(header().string("Content-type", "application/rdap+json;charset=UTF-8"))
        .andExpect(status().isMovedPermanently())
        .andExpect(header().string("Location", REDIRECT_URL + "/domain/example.com/redirect/1"))
        .andExpect(content().string("{\"rdapConformance\":[\"rdap_level_0\"],\"ldhName\":\"example.com\"}"));
  }

  @Test
  public void testNotAuthoritativeWithPreviousRedirect() throws Exception {
    DomainController.NotAuthoritativeException notAuthoritativeException = new DomainController.NotAuthoritativeException(DomainName.of("example.com"));
    Mockito
        .when(domainService.getDomain(Mockito.any(DomainName.class)))
        .thenThrow(notAuthoritativeException);
    MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    mockMvc.perform(get("/domain/example/redirect/1")
        .accept(MediaType.parseMediaType("application/rdap+json")))
        .andExpect(header().string("Content-type", "application/rdap+json;charset=UTF-8"))
        .andExpect(status().isMovedPermanently())
        .andExpect(header().string("Location", REDIRECT_URL + "/domain/example.com/redirect/2"))
        .andExpect(content().string("{\"rdapConformance\":[\"rdap_level_0\"],\"ldhName\":\"example.com\"}"));
  }

  @Test
  public void testNotAuthoritativeTooManyRedirects() throws Exception {
    DomainController.NotAuthoritativeException notAuthoritativeException = new DomainController.NotAuthoritativeException(DomainName.of("example.com"));
    Mockito
        .when(domainService.getDomain(Mockito.any(DomainName.class)))
        .thenThrow(notAuthoritativeException);
    MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    mockMvc.perform(get("/domain/example/redirect/4")
        .accept(MediaType.parseMediaType("application/rdap+json")))
        .andExpect(header().string("Content-type", "application/rdap+json;charset=UTF-8"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("{\"rdapConformance\":[\"rdap_level_0\"],\"ldhName\":\"example\"}"));
  }

}
