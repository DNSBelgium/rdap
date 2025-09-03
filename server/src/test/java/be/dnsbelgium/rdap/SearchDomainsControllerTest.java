package be.dnsbelgium.rdap;

import be.dnsbelgium.core.DomainName;
import be.dnsbelgium.rdap.controller.SearchDomainsController;
import be.dnsbelgium.rdap.core.Domain;
import be.dnsbelgium.rdap.core.DomainsSearchResult;
import be.dnsbelgium.rdap.core.RDAPError;
import be.dnsbelgium.rdap.service.DomainService;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.List;

import static be.dnsbelgium.rdap.RdapMediaType.APPLICATION_RDAP_JSON;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = SearchDomainsControllerTest.Config.class)
public class SearchDomainsControllerTest extends AbstractControllerTest {


  public static final String NAME = "example*.com";
  public static final String NS_LDH_NAME = "ns.example*.com";
  public static final String NS_IP = "193.82.65.1";

  @Configuration
  static class Config extends AbstractControllerTest.Config {

    @Bean
    public DomainService domainService() {
      return mock(DomainService.class);
    }

    @Bean
    public SearchDomainsController searchDomainsController() {
      return new SearchDomainsController(domainService());
    }
  }

  @Autowired
  SearchDomainsController searchDomainsController;

  @Autowired
  DomainService domainService;

  @After
  public void resetMock() {
    reset(domainService);
  }


  @Test
  public void testSearchByNameNotAuthoritative() throws Exception {
    when(domainService.searchDomainsByName(NAME)).thenThrow(RDAPError.notAuthoritative(DomainName.of(NAME)));
    mockMvc.perform(get("/domains?name=" + NAME)).andExpect(status().isMovedPermanently());
    verify(domainService, times(1)).searchDomainsByName(eq(NAME));
    verify(domainService, never()).searchDomainsByNsLdhName(anyString());
    verify(domainService, never()).searchDomainsByNsIp(anyString());
  }

  @Test
  public void testSearchByNameNoResults() throws Exception {
    DomainsSearchResult domainsSearchResult = new DomainsSearchResult(new ArrayList<>());
    domainsSearchResult.addRdapConformance(Domain.DEFAULT_RDAP_CONFORMANCE);
    when(domainService.searchDomainsByName(NAME)).thenReturn(domainsSearchResult);
    mockMvc.perform(get("/domains?name=" + NAME).accept(APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_RDAP_JSON));
  }

  @Test
  public void testCheckParams() throws Exception {
    mockMvc.perform(get("/domains?name=" + NAME + "&nsLdhName=" + NS_LDH_NAME + "&nsIp=" + NS_IP)).andExpect(status().isBadRequest());
    verify(domainService, never()).searchDomainsByName(anyString());
    verify(domainService, never()).searchDomainsByNsLdhName(anyString());
    verify(domainService, never()).searchDomainsByNsIp(anyString());
    mockMvc.perform(get("/domains?name=" + NAME + "&nsLdhName=" + NS_LDH_NAME)).andExpect(status().isBadRequest());
    verify(domainService, never()).searchDomainsByName(anyString());
    verify(domainService, never()).searchDomainsByNsLdhName(anyString());
    verify(domainService, never()).searchDomainsByNsIp(anyString());
    mockMvc.perform(get("/domains?nsLdhName=" + NS_LDH_NAME + "&nsIp=" + NS_IP)).andExpect(status().isBadRequest());
    verify(domainService, never()).searchDomainsByName(anyString());
    verify(domainService, never()).searchDomainsByNsLdhName(anyString());
    verify(domainService, never()).searchDomainsByNsIp(anyString());
    mockMvc.perform(get("/domains?nsIp=" + NS_IP + "&name=" + NAME)).andExpect(status().isBadRequest());
    verify(domainService, never()).searchDomainsByName(anyString());
    verify(domainService, never()).searchDomainsByNsLdhName(anyString());
    verify(domainService, never()).searchDomainsByNsIp(anyString());
    mockMvc.perform(get("/domains")).andExpect(status().isBadRequest());
    verify(domainService, never()).searchDomainsByName(anyString());
    verify(domainService, never()).searchDomainsByNsLdhName(anyString());
    verify(domainService, never()).searchDomainsByNsIp(anyString());
  }

  @Test
  public void testSearchByNameAcceptJsonSuccess() throws Exception {
    performSearchDomainsByName(APPLICATION_JSON);
  }

  @Test
  public void testSearchByNameAcceptRdapJsonSuccess() throws Exception {
    performSearchDomainsByName(APPLICATION_RDAP_JSON);
  }

  @Test
  public void testSearchByNameAcceptOtherAcceptHeadersSuccess() throws Exception {
    performSearchDomainsByName(TEXT_HTML);
  }

  @Test
  public void testSearchByNsLdhNameJson() throws Exception {
    DomainsSearchResult domainsSearchResult = initDomainsSearchResult();
    when(domainService.searchDomainsByNsLdhName(NS_LDH_NAME)).thenReturn(domainsSearchResult);
    mockMvc.perform(get("/domains?nsLdhName=" + NS_LDH_NAME).accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_RDAP_JSON))
            .andExpect(content().string("{\"rdapConformance\":[\"rdap_level_0\"],\"domainSearchResults\":[{\"objectClassName\":\"domain\",\"lang\":\"en\",\"status\":[\"active\",\"delete prohibited\",\"some specific status\"],\"handle\":\"Handle\",\"ldhName\":\"notexample.org\"}]}"));
    verify(domainService, times(1)).searchDomainsByNsLdhName(NS_LDH_NAME);
    verify(domainService, never()).searchDomainsByName(anyString());
    verify(domainService, never()).searchDomainsByNsIp(anyString());
  }

  @Test
  public void testSearchByNsLdhNameRdapJson() throws Exception {
    DomainsSearchResult domainsSearchResult = initDomainsSearchResult();
    when(domainService.searchDomainsByNsLdhName(NS_LDH_NAME)).thenReturn(domainsSearchResult);
    mockMvc.perform(get("/domains?nsLdhName=" + NS_LDH_NAME).accept(APPLICATION_RDAP_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_RDAP_JSON))
            .andExpect(content().string("{\"rdapConformance\":[\"rdap_level_0\"],\"domainSearchResults\":[{\"objectClassName\":\"domain\",\"lang\":\"en\",\"status\":[\"active\",\"delete prohibited\",\"some specific status\"],\"handle\":\"Handle\",\"ldhName\":\"notexample.org\"}]}"));
  }

  @Test
  public void testSearchByNsIpJsonHeader() throws Exception {
    DomainsSearchResult domainsSearchResult = initDomainsSearchResult();
    when(domainService.searchDomainsByNsIp(NS_IP)).thenReturn(domainsSearchResult);
    mockMvc.perform(get("/domains?nsIp=" + NS_IP).accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_RDAP_JSON))
            .andExpect(content().string("{\"rdapConformance\":[\"rdap_level_0\"],\"domainSearchResults\":[{\"objectClassName\":\"domain\",\"lang\":\"en\",\"status\":[\"active\",\"delete prohibited\",\"some specific status\"],\"handle\":\"Handle\",\"ldhName\":\"notexample.org\"}]}"));
    verify(domainService, times(1)).searchDomainsByNsIp(NS_IP);
    verify(domainService, never()).searchDomainsByName(anyString());
    verify(domainService, never()).searchDomainsByNsLdhName(anyString());
  }

  @Test
  public void testSearchByNsIpRdapJson() throws Exception {
    DomainsSearchResult domainsSearchResult = initDomainsSearchResult();
    when(domainService.searchDomainsByNsIp(NS_IP)).thenReturn(domainsSearchResult);
    mockMvc.perform(get("/domains?nsIp=" + NS_IP).accept(APPLICATION_RDAP_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_RDAP_JSON))
            .andExpect(content().string("{\"rdapConformance\":[\"rdap_level_0\"],\"domainSearchResults\":[{\"objectClassName\":\"domain\",\"lang\":\"en\",\"status\":[\"active\",\"delete prohibited\",\"some specific status\"],\"handle\":\"Handle\",\"ldhName\":\"notexample.org\"}]}"));
  }

  @Test
  public void testSearchByNsIpOtherHeaders() throws Exception {
    DomainsSearchResult domainsSearchResult = initDomainsSearchResult();
    when(domainService.searchDomainsByNsIp(NS_IP)).thenReturn(domainsSearchResult);
    mockMvc.perform(get("/domains?nsIp=" + NS_IP).accept(TEXT_HTML))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_RDAP_JSON))
            .andExpect(content().string("{\"rdapConformance\":[\"rdap_level_0\"],\"domainSearchResults\":[{\"objectClassName\":\"domain\",\"lang\":\"en\",\"status\":[\"active\",\"delete prohibited\",\"some specific status\"],\"handle\":\"Handle\",\"ldhName\":\"notexample.org\"}]}"));
  }
  
  @Test
  public void testMethodNotAllowed() throws Exception {
      mockMvc.perform(put("/domains?nsIp=" + NS_IP).accept(APPLICATION_RDAP_JSON))
              .andExpect(status().isMethodNotAllowed());
  }

  public void performSearchDomainsByName(MediaType acceptHeader) throws Exception {
    List<Domain> domains = new ArrayList<>();
    domains.add(new Domain(someLinks(), someNotices(), someRemarks(), "en", someEvents(), someStatuses(),
            DomainName.of("whois.example.com"), "Handle", DomainName.of("examples.com"), DomainName.of("examples.com"), someVariants(),
            someNameservers(), aSecureDNS(), someEntities(), somePublicIds(), anIPNetwork()));
    DomainsSearchResult domainsSearchResult = new DomainsSearchResult(domains);
    domainsSearchResult.addRdapConformance(Domain.DEFAULT_RDAP_CONFORMANCE);
    when(domainService.searchDomainsByName(NAME)).thenReturn(domainsSearchResult);
    performSearchDomainTest(acceptHeader);
    verify(domainService, times(1)).searchDomainsByName(NAME);
    verify(domainService, never()).searchDomainsByNsLdhName(anyString());
    verify(domainService, never()).searchDomainsByNsIp(anyString());
  }

  public void performSearchDomainTest(MediaType acceptHeader) throws Exception {
    String expectedJson = createExpectedJson("SearchDomainsControllerTest.searchByNameSuccess.json");

    mockMvc.perform(get("/domains?name=" + NAME).accept(acceptHeader))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_RDAP_JSON))
            .andExpect(content().json(expectedJson));
  }

  public DomainsSearchResult initDomainsSearchResult() throws Exception {
    List <Domain> domains = new ArrayList<>();
    domains.add(new Domain(null, null, null, "en",null, someStatuses(), null, "Handle", DomainName.of("notexample.org"), null,
            null, null, null, null, null, null));
    DomainsSearchResult domainsSearchResult = new DomainsSearchResult(domains);
    domainsSearchResult.addRdapConformance(Domain.DEFAULT_RDAP_CONFORMANCE);
    return domainsSearchResult;
  }
}