/**
 * Copyright 2014 DNS Belgium vzw
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.dnsbelgium.rdap;

import be.dnsbelgium.core.DomainName;
import be.dnsbelgium.rdap.controller.Controllers;
import be.dnsbelgium.rdap.controller.DomainController;
import be.dnsbelgium.rdap.core.*;
import be.dnsbelgium.rdap.core.RDAPError;
import be.dnsbelgium.rdap.service.DomainService;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = DomainControllerTest.Config.class)
public class DomainControllerTest extends AbstractControllerTest {

  private final static String REDIRECT_URL = "https://rdap.org";

  @Configuration
  static class Config extends AbstractControllerTest.Config {

    @Bean
    public DomainService domainService() {
      return Mockito.mock(DomainService.class);
    }

    @Bean
    public DomainController domainController() {
      return new DomainController(REDIRECT_URL, domainService());
    }
  }

  @Autowired
  DomainController domainController;

  @Autowired
  DomainService domainService;

  @After
  public void resetMock() {
    Mockito.reset(domainService);
  }

  @Test
  public void testNotFound() throws Exception {
    when(domainService.getDomain(Mockito.any(DomainName.class))).thenReturn(null);
    mockMvc.perform(get("/domain/example.com").accept(MediaType.parseMediaType("application/rdap+json"))).andExpect(status().isNotFound());
  }

  @Test
  public void testAcceptRdapJson() throws Exception {
    DomainName domainName = DomainName.of("example.com");
    Domain domain = new Domain(null, null, null, null, null, null, null, null, domainName, domainName, null, null, null, null, null, null);
    when(domainService.getDomain(Mockito.any(DomainName.class))).thenReturn(domain);
    mockMvc.perform(get("/domain/example.com").accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isNotAcceptable());
  }

  @Test
  public void testNotAuthoritative() throws Exception {
    when(domainService.getDomain(Mockito.any(DomainName.class))).thenThrow(RDAPError.notAuthoritative(DomainName.of("example.com")));
    mockMvc.perform(get("/domain/example.com")).andExpect(status().isMovedPermanently()).andExpect(redirectedUrl(REDIRECT_URL + "/domain/example.com"));
  }

  @Test
  public void testDefaultGet() throws Exception {
    DomainName domainName = DomainName.of("example.com");
    Domain domain = new Domain(null, null, null, null, null, null, null, null, domainName, domainName, null, null, null, null, null, null);
    when(domainService.getDomain(Mockito.any(DomainName.class))).thenReturn(domain);
    mockMvc.perform(get("/domain/example.com").accept(MediaType.parseMediaType("application/rdap+json"))).andExpect(status().isOk()).andExpect(jsonPath("$.ldhName", "example.com").exists());
  }

  @Test
  public void testDefaultGetWithTrailingSlash() throws Exception {
    DomainName domainName = DomainName.of("example.com");
    Domain domain = new Domain(null, null, null, null, null, null, null, null, domainName, domainName, null, null, null, null, null, null);
    when(domainService.getDomain(Mockito.any(DomainName.class))).thenReturn(domain);
    mockMvc.perform(get("/domain/example.com/").accept(MediaType.parseMediaType("application/rdap+json"))).andExpect(status().isNotFound());
  }

  @Test
  public void testDefaultHead() throws Exception {
    DomainName domainName = DomainName.of("example.com");
    Domain domain = new Domain(null, null, null, null, null, null, null, null, domainName, domainName, null, null, null, null, null, null);
    when(domainService.getDomain(Mockito.any(DomainName.class))).thenReturn(domain);
    mockMvc.perform(head("/domain/example.com").accept(MediaType.parseMediaType("application/rdap+json"))).andExpect(status().isOk());
  }

	@Test
	public void testMethodNotAllowed() throws Exception {
		mockMvc.perform(put("/domain/example.com").accept(MediaType.parseMediaType("application/rdap+json")))
				.andExpect(status().isMethodNotAllowed());
	}
	
  @Test
  public void testNoGlue() throws Exception{
    DomainName domainName = DomainName.of("example.com");
    DomainName nameserverName = DomainName.of("ns.example.other");
    Nameserver nameserver = new Nameserver(null, null, null, null, null, null, null, null, nameserverName, nameserverName, null);
    List<Nameserver> nameserverList = new ArrayList<Nameserver>();
    nameserverList.add(nameserver);
    Domain domain = new Domain(null, null, null, null, null, null, null, null, domainName, domainName, null, nameserverList, null, null, null, null);
    domain.addRdapConformance(Domain.DEFAULT_RDAP_CONFORMANCE);
    when(domainService.getDomain(Mockito.any(DomainName.class))).
            thenReturn(domain);
    mockMvc.perform(get("/domain/example.com")
            .accept(MediaType.parseMediaType("application/rdap+json")))
            .andExpect(header().string("Content-type", "application/rdap+json;charset=UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"rdapConformance\":[\"rdap_level_0\"],\"objectClassName\":\"domain\"," +
                    "\"ldhName\":\"example.com\",\"unicodeName\":\"example.com\"," +
                    "\"nameservers\":[{\"objectClassName\":\"nameserver\",\"ldhName\":\"ns.example.other\",\"unicodeName\":\"ns.example.other\"}]}"));
  }

  @Test
  public void testMaximalDomain() throws Exception {
    String expectedJson = createExpectedJson("DomainControllerTest.maximalDomain.json");
    DomainName domainName = DomainName.of("example.com");

    Domain domain = new Domain(someLinks(), someNotices(), someRemarks(), "en", someEvents(), someStatuses(), DomainName.of("whois.example.com"),
            "Handle", domainName, domainName, someVariants(), someNameservers(), aSecureDNS(), someEntities(), somePublicIds(), anIPNetwork());
    domain.addRdapConformance(Domain.DEFAULT_RDAP_CONFORMANCE);
    when(domainService.getDomain(Mockito.any(DomainName.class))).thenReturn(domain);
    mockMvc.perform(get("/domain/example.com")
            .accept(MediaType.parseMediaType("application/rdap+json")))
            .andExpect(header().string("Content-type", "application/rdap+json;charset=UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
  }

  @Test
  public void testBytes() throws Exception {
    DomainName domainName = DomainName.of("example.com");
    Domain domain = new Domain(null, null, null, null, null, null, null, null, domainName, domainName, null, null, null, null, null, null);
    domain.addRdapConformance(Domain.DEFAULT_RDAP_CONFORMANCE);
    when(domainService.getDomain(Mockito.any(DomainName.class))).thenReturn(domain);
    mockMvc.perform(get("/domain/example.com")
        .accept(MediaType.parseMediaType("application/rdap+json")))
        .andExpect(header().string("Content-type", "application/rdap+json;charset=UTF-8"))
        .andExpect(status().isOk())
        .andExpect(content().string("{\"rdapConformance\":[\"rdap_level_0\"],\"objectClassName\":\"domain\",\"ldhName\":\"example.com\",\"unicodeName\":\"example.com\"}"));
  }

  @Test
  public void testWrongMediaType() throws Exception {
    mockMvc.perform(get("/domain/example.com")
            .accept(MediaType.TEXT_HTML))
        .andExpect(status().isNotAcceptable());
  }

  @Test
  public void testIDNParseException() throws Exception {
    mockMvc.perform(get("/domain/-\u2620-.be")
        .accept(MediaType.parseMediaType("application/rdap+json")))
        .andExpect(header().string("Content-type", Controllers.CONTENT_TYPE))
        .andExpect(content().string("{\"errorCode\":400,\"title\":\"Invalid domain name\",\"description\":[\"LEADING_HYPHEN\",\"TRAILING_HYPHEN\",\"DISALLOWED\"]}"))
            .andExpect(status().isBadRequest());
  }
  
  
  
}
