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
import be.dnsbelgium.rdap.controller.DomainController;
import be.dnsbelgium.rdap.core.*;
import be.dnsbelgium.rdap.core.RDAPError;
import be.dnsbelgium.rdap.service.DomainService;
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
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.ArrayList;
import java.util.List;

import static be.dnsbelgium.rdap.RdapMediaType.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = DomainControllerTest.Config.class)
public class DomainControllerTest extends AbstractControllerTest {

  private static final String DOMAIN = "example.com";
  private static final DomainName DOMAIN_NAME = DomainName.of(DOMAIN);
  private static final String DOMAIN_PATH = "/domain/" + DOMAIN;
  private static final  String REDIRECT_URL = "https://rdap.org";

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
    when(domainService.getDomain(DOMAIN_NAME)).thenReturn(null);
    mockMvc.perform(get(DOMAIN_PATH).accept(APPLICATION_RDAP_JSON)).andExpect(status().isNotFound());
  }

  @Test
  public void testAcceptRdapJson() throws Exception {
    verifyGetDomainOk(APPLICATION_RDAP_JSON);
  }

  @Test
  public void testAcceptRdapJsonUtf8() throws Exception {
    verifyGetDomainOk(APPLICATION_RDAP_JSON_UTF8);
  }

  @Test
  public void testAcceptJson() throws Exception {
    verifyGetDomainOk(APPLICATION_JSON);
  }

  @Test
  public void testAcceptJsonUtf8() throws Exception {
    verifyGetDomainOk(APPLICATION_JSON_UTF8);
  }

  @Test
  public void testAcceptOtherAcceptHeaders() throws Exception {
    verifyGetDomainOk(TEXT_HTML);
  }

  @Test
  public void testNotAuthoritative() throws Exception {
    when(domainService.getDomain(DOMAIN_NAME)).thenThrow(RDAPError.notAuthoritative(DOMAIN_NAME));
    performGetDomain(APPLICATION_RDAP_JSON, status().isMovedPermanently(),
        redirectedUrl(REDIRECT_URL + DOMAIN_PATH));
  }

  @Test
  public void testDefaultGet() throws Exception {
    verifyGetDomainOk(jsonPath("$.ldhName", DOMAIN).exists());
  }

  @Test
  public void testDefaultGetWithTrailingSlash() throws Exception {
    mockMvc.perform(get(DOMAIN_PATH + "/").accept(APPLICATION_RDAP_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testDefaultHead() throws Exception {
    initDomain();
    mockMvc.perform(head(DOMAIN_PATH).accept(APPLICATION_RDAP_JSON))
        .andExpect(status().isOk());
  }

	@Test
	public void testMethodNotAllowed() throws Exception {
		mockMvc.perform(put(DOMAIN_PATH).accept(APPLICATION_RDAP_JSON))
				.andExpect(status().isMethodNotAllowed());
	}
	
  @Test
  public void testNoGlue() throws Exception{
    DomainName nameserverName = DomainName.of("ns.example.other");
    Nameserver nameserver = new Nameserver(null, null, null, null, null, null, null, null, nameserverName, nameserverName, null);
    List<Nameserver> nameserverList = new ArrayList<>();
    nameserverList.add(nameserver);
    Domain domain = new Domain(null, null, null, null, null, null, null, null, DOMAIN_NAME, DOMAIN_NAME, null, nameserverList, null, null, null, null);
    domain.addRdapConformance(Domain.DEFAULT_RDAP_CONFORMANCE);

    verifyGetDomainOkForDomain(domain, content().string("{\"rdapConformance\":[\"rdap_level_0\"],\"objectClassName\":\"domain\"," +
        "\"ldhName\":\"example.com\",\"unicodeName\":\"example.com\"," +
        "\"nameservers\":[{\"objectClassName\":\"nameserver\",\"ldhName\":\"ns.example.other\",\"unicodeName\":\"ns.example.other\"}]}"));
  }

  @Test
  public void testMaximalDomain() throws Exception {
    String expectedJson = createExpectedJson("DomainControllerTest.maximalDomain.json");

    Domain domain = new Domain(someLinks(), someNotices(), someRemarks(), "en", someEvents(), someStatuses(), DomainName.of("whois.example.com"),
            "Handle", DOMAIN_NAME, DOMAIN_NAME, someVariants(), someNameservers(), aSecureDNS(), someEntities(), somePublicIds(), anIPNetwork());
    domain.addRdapConformance(Domain.DEFAULT_RDAP_CONFORMANCE);

    verifyGetDomainOkForDomain(domain, content().json(expectedJson));
  }

  @Test
  public void testBytes() throws Exception {
    verifyGetDomainOk(content().string("{\"rdapConformance\":[\"rdap_level_0\"],\"objectClassName\":\"domain\",\"ldhName\":\"example.com\",\"unicodeName\":\"example.com\"}"));
  }

  @Test
  public void testIDNParseException() throws Exception {
    performGet("/domain/-\u2620-.be", APPLICATION_JSON, status().isBadRequest(),
        content().string("{\"errorCode\":400,\"title\":\"Invalid domain name\",\"description\":[\"LEADING_HYPHEN\",\"TRAILING_HYPHEN\",\"DISALLOWED\"]}"));
  }

  private void verifyGetDomainOk(ResultMatcher... additionalMatchers) throws Exception {
    verifyGetDomainOk(APPLICATION_RDAP_JSON, additionalMatchers);
  }

  private void verifyGetDomainOk(MediaType acceptHeader, ResultMatcher... additionalMatchers) throws Exception {
    initDomain();
    performGetDomain(acceptHeader, status().isOk(), additionalMatchers);
  }

  private void verifyGetDomainOkForDomain(Domain domain, ResultMatcher... additionalMatchers) throws Exception {
    when(domainService.getDomain(DOMAIN_NAME)).thenReturn(domain);
    performGetDomain(APPLICATION_RDAP_JSON, status().isOk(), additionalMatchers);
  }

  private void performGetDomain(MediaType acceptHeader, ResultMatcher statusMatcher, ResultMatcher... additionalMatchers) throws Exception {
    performGet(DOMAIN_PATH, acceptHeader, statusMatcher, additionalMatchers);
  }

  private void performGet(String urlTemplate, MediaType acceptHeader, ResultMatcher statusMatcher, ResultMatcher... additionalMatchers) throws Exception {
    mockMvc.perform(get(urlTemplate).accept(acceptHeader))
        .andExpect(header().string("Content-type", RdapMediaType.APPLICATION_RDAP_JSON_UTF8_VALUE))
        .andExpect(statusMatcher)
        .andExpect(content().contentTypeCompatibleWith(APPLICATION_RDAP_JSON))
        .andExpectAll(additionalMatchers);
  }

  private void initDomain() throws RDAPError {
    Domain domain = new Domain(null, null, null, null, null, null, null, null, DOMAIN_NAME, DOMAIN_NAME, null, null, null, null, null, null);
    domain.addRdapConformance(Domain.DEFAULT_RDAP_CONFORMANCE);
    when(domainService.getDomain(DOMAIN_NAME)).thenReturn(domain);
  }

}
