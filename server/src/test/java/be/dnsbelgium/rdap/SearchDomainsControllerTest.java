package be.dnsbelgium.rdap;

import be.dnsbelgium.core.DomainName;
import be.dnsbelgium.rdap.core.Domain;
import be.dnsbelgium.rdap.core.DomainsSearchResult;
import be.dnsbelgium.rdap.core.RDAPError;
import be.dnsbelgium.rdap.service.DomainService;
import org.joda.time.format.ISODateTimeFormat;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
      return new SearchDomainsController();
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
  public void testWrongMediaType() throws Exception {
    mockMvc.perform(get("/domains?name=example*.com")
            .accept(MediaType.TEXT_HTML))
            .andExpect(status().isNotAcceptable());
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
    DomainsSearchResult domainsSearchResult = new DomainsSearchResult(new ArrayList<Domain>());
    domainsSearchResult.addRdapConformance(Domain.DEFAULT_RDAP_CONFORMANCE);
    when(domainService.searchDomainsByName(NAME)).thenReturn(domainsSearchResult);
    mockMvc.perform(get("/domains?name=" + NAME).accept(MediaType.parseMediaType("application/rdap+json")))
            .andExpect(status().isNotFound());
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
  public void testSearchByNameSuccess() throws Exception {
    List<Domain> domains = new ArrayList<Domain>();
    domains.add(new Domain(someLinks(), someNotices(), someRemarks(), "en", someEvents(), someStatuses(),
            DomainName.of("whois.example.com"), "Handle", DomainName.of("examples.com"), DomainName.of("examples.com"), someVariants(),
            someNameservers(), aSecureDNS(), someEntities(), somePublicIds(), anIPNetwork()));
    DomainsSearchResult domainsSearchResult = new DomainsSearchResult(domains);
    domainsSearchResult.addRdapConformance(Domain.DEFAULT_RDAP_CONFORMANCE);
    when(domainService.searchDomainsByName(NAME)).thenReturn(domainsSearchResult);
    mockMvc.perform(get("/domains?name=" + NAME).accept(MediaType.parseMediaType("application/rdap+json")))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-type", "application/rdap+json;charset=UTF-8"))
            .andExpect(content().string("{\"rdapConformance\":[\"rdap_level_0\"]," +
                    "\"domainSearchResults\":[{\"objectClassName\":\"domain\"," +
                    "\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]," +
                    "\"notices\":[{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}]," +
                    "\"remarks\":[{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}]," +
                    "\"lang\":\"en\"," +
                    "\"events\":[{\"eventAction\":\"REGISTRATION\",\"eventActor\":\"EventActor\",\"eventDate\":\"" + createTime.toString(ISODateTimeFormat.dateTimeNoMillis()) + "\",\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"eventAction\":\"LAST_CHANGED\",\"eventActor\":\"EventActor\",\"eventDate\":\"" + lastChangedTime.toString(ISODateTimeFormat.dateTimeNoMillis()) + "\",\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}]," +
                    "\"status\":[\"active\",\"delete prohibited\",\"some specific status\"]," +
                    "\"port43\":\"whois.example.com\"," +
                    "\"handle\":\"Handle\"," +
                    "\"ldhName\":\"examples.com\"," +
                    "\"unicodeName\":\"examples.com\"," +
                    "\"variants\":[{\"variantNames\":[{\"ldhName\":\"exomple.com\",\"unicodeName\":\"exomple.com\"},{\"ldhName\":\"eximple.com\",\"unicodeName\":\"eximple.com\"}],\"relations\":[\"UNREGISTERED\",\"RESTRICTED_REGISTRATION\"]},{\"variantNames\":[{\"ldhName\":\"xn--exmple-jta.com\",\"unicodeName\":\"exàmple.com\"}],\"relations\":[\"REGISTERED\"]}]," +
                    "\"secureDNS\":{\"zoneSigned\":true,\"delegationSigned\":true,\"maxSigLife\":6000,\"dsData\":[{\"keyTag\":64156,\"algorithm\":8,\"digest\":\"DC48B4183F9AC496574DEB8633F627A6DE207493\",\"digestType\":1},{\"keyTag\":64156,\"algorithm\":8,\"digest\":\"DE3BBED2664B02B9FEC6FF81D8539B14A5714A2C7A92E8FE58914200 C30B1958\",\"digestType\":2}]}," +
                    "\"entities\":[{\"objectClassName\":\"entity\",\"lang\":\"en\",\"handle\":\"REGISTRANT\",\"vcardArray\":[\"vcard\",[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"This is a formatted name\"],[\"adr\",{},\"text\",null,null,\"street 1\",null,null,null,null]]],\"roles\":[\"REGISTRANT\",\"ADMINISTRATIVE\"]}]," +
                    "\"publicIds\":[{\"type\":\"Type\",\"identifier\":\"Identifier\"},{\"type\":\"Type\",\"identifier\":\"Identifier\"}]," +
                    "\"network\":{\"objectClassName\":\"ip network\",\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}],\"notices\":[{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}],\"remarks\":[{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}],\"lang\":\"en\",\"events\":[{\"eventAction\":\"REGISTRATION\",\"eventActor\":\"EventActor\",\"eventDate\":\"" + createTime.toString(ISODateTimeFormat.dateTimeNoMillis()) + "\",\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"eventAction\":\"LAST_CHANGED\",\"eventActor\":\"EventActor\",\"eventDate\":\"" + lastChangedTime.toString(ISODateTimeFormat.dateTimeNoMillis()) + "\",\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}],\"status\":[\"active\",\"delete prohibited\",\"some specific status\"],\"port43\":\"whois.example.com\",\"handle\":\"Handle\",\"startAddress\":\"193.12.32.98\",\"endAddress\":\"193.12.32.98\",\"name\":\"Name\",\"type\":\"Type\",\"country\":\"Country\",\"parentHandle\":\"ParentHandle\",\"entities\":[{\"objectClassName\":\"entity\",\"lang\":\"en\",\"handle\":\"REGISTRANT\",\"vcardArray\":[\"vcard\",[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"This is a formatted name\"],[\"adr\",{},\"text\",null,null,\"street 1\",null,null,null,null]]],\"roles\":[\"REGISTRANT\",\"ADMINISTRATIVE\"]}]}," +
                    "\"nameservers\":[{\"objectClassName\":\"nameserver\",\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}],\"notices\":[{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}],\"remarks\":[{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}],\"lang\":\"en\",\"events\":[{\"eventAction\":\"REGISTRATION\",\"eventActor\":\"EventActor\",\"eventDate\":\"" + createTime.toString(ISODateTimeFormat.dateTimeNoMillis()) + "\",\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"eventAction\":\"LAST_CHANGED\",\"eventActor\":\"EventActor\",\"eventDate\":\"" + lastChangedTime.toString(ISODateTimeFormat.dateTimeNoMillis()) + "\",\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}],\"status\":[\"active\",\"delete prohibited\",\"some specific status\"],\"port43\":\"whois.example.com\",\"handle\":\"Handle\",\"ldhName\":\"ns.xn--exmple-jta.com\",\"unicodeName\":\"ns.exàmple.com\",\"ipAddresses\":{\"v4\":[\"193.5.6.198\",\"89.65.3.87\"],\"v6\":[\"2001:678:9::1\",\"FE80:0000:0000:0000:0202:B3FF:FE1E:8329\"]}},{\"objectClassName\":\"nameserver\",\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}],\"notices\":[{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}],\"remarks\":[{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"]," +
                    "\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}],\"lang\":\"en\",\"events\":[{\"eventAction\":\"REGISTRATION\",\"eventActor\":\"EventActor\",\"eventDate\":\"" + createTime.toString(ISODateTimeFormat.dateTimeNoMillis()) + "\",\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"eventAction\":\"LAST_CHANGED\",\"eventActor\":\"EventActor\",\"eventDate\":\"" + lastChangedTime.toString(ISODateTimeFormat.dateTimeNoMillis()) + "\",\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}],\"status\":[\"active\",\"delete prohibited\",\"some specific status\"],\"port43\":\"whois.example.com\",\"handle\":\"Handle\",\"ldhName\":\"ns.xn--exmple-jta.com\",\"unicodeName\":\"ns.exàmple.com\",\"ipAddresses\":{\"v4\":[\"193.5.6.198\",\"89.65.3.87\"],\"v6\":[\"2001:678:9::1\",\"FE80:0000:0000:0000:0202:B3FF:FE1E:8329\"]}}]}]}"));
    verify(domainService, times(1)).searchDomainsByName(NAME);
    verify(domainService, never()).searchDomainsByNsLdhName(anyString());
    verify(domainService, never()).searchDomainsByNsIp(anyString());
  }

  @Test
  public void testSearchByNsLdhName() throws Exception {
    List <Domain> domains = new ArrayList<Domain>();
    domains.add(new Domain(null, null, null, "en", null, someStatuses(), null, "Handle", DomainName.of("notexample.org"), null,
            null, null, null, null, null, null));
    DomainsSearchResult domainsSearchResult = new DomainsSearchResult(domains);
    domainsSearchResult.addRdapConformance(Domain.DEFAULT_RDAP_CONFORMANCE);
    when(domainService.searchDomainsByNsLdhName(NS_LDH_NAME)).thenReturn(domainsSearchResult);
    mockMvc.perform(get("/domains?nsLdhName=" + NS_LDH_NAME).accept(MediaType.parseMediaType("application/rdap+json")))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-type", "application/rdap+json;charset=UTF-8"))
            .andExpect(content().string("{\"rdapConformance\":[\"rdap_level_0\"],\"domainSearchResults\":[{\"objectClassName\":\"domain\",\"lang\":\"en\",\"status\":[\"active\",\"delete prohibited\",\"some specific status\"],\"handle\":\"Handle\",\"ldhName\":\"notexample.org\"}]}"));
    verify(domainService, times(1)).searchDomainsByNsLdhName(NS_LDH_NAME);
    verify(domainService, never()).searchDomainsByName(anyString());
    verify(domainService, never()).searchDomainsByNsIp(anyString());
  }

  @Test
  public void testSearchByNsIp() throws Exception {
    List <Domain> domains = new ArrayList<Domain>();
    domains.add(new Domain(null, null, null, "en",null, someStatuses(), null, "Handle", DomainName.of("notexample.org"), null,
            null, null, null, null, null, null));
    DomainsSearchResult domainsSearchResult = new DomainsSearchResult(domains);
    domainsSearchResult.addRdapConformance(Domain.DEFAULT_RDAP_CONFORMANCE);
    when(domainService.searchDomainsByNsIp(NS_IP)).thenReturn(domainsSearchResult);
    mockMvc.perform(get("/domains?nsIp=" + NS_IP).accept(MediaType.parseMediaType("application/rdap+json")))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-type", "application/rdap+json;charset=UTF-8"))
            .andExpect(content().string("{\"rdapConformance\":[\"rdap_level_0\"],\"domainSearchResults\":[{\"objectClassName\":\"domain\",\"lang\":\"en\",\"status\":[\"active\",\"delete prohibited\",\"some specific status\"],\"handle\":\"Handle\",\"ldhName\":\"notexample.org\"}]}"));
    verify(domainService, times(1)).searchDomainsByNsIp(NS_IP);
    verify(domainService, never()).searchDomainsByName(anyString());
    verify(domainService, never()).searchDomainsByNsLdhName(anyString());
  }
  
	@Test
	public void testMethodNotAllowed() throws Exception {
		mockMvc.perform(put("/domains?nsIp=" + NS_IP).accept(MediaType.parseMediaType("application/rdap+json")))
				.andExpect(status().isMethodNotAllowed());
	}
}