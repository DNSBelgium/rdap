package be.dnsbelgium.rdap;

import be.dnsbelgium.rdap.core.Nameserver;
import be.dnsbelgium.rdap.core.NameserversSearchResult;
import be.dnsbelgium.rdap.core.RDAPError;
import be.dnsbelgium.rdap.service.NameserverService;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = SearchNameserversControllerTest.Config.class)
public class SearchNameserversControllerTest extends AbstractControllerTest {


  @Configuration
  static class Config extends AbstractControllerTest.Config {

    @Bean
    public NameserverService nameserverService() {
      return mock(NameserverService.class);
    }

    @Bean
    public SearchNameserversController searchNameserversController() {
      return new SearchNameserversController();
    }
  }

  @Autowired
  SearchNameserversController searchNameserversController;


  @Autowired
  NameserverService nameserverService;

  @After
  public void resetMock() {
    reset(nameserverService);
  }

  @Test
  public void testSearchByIpSuccess() throws Exception {
    String ipQuery = "193.56.54.32";
    NameserversSearchResult nameserversSearchResult = new NameserversSearchResult(someNameservers());
    nameserversSearchResult.addRdapConformance(Nameserver.DEFAULT_RDAP_CONFORMANCE);
    when(nameserverService.searchByIp(ipQuery)).thenReturn(nameserversSearchResult);
    mockMvc.perform(get("/nameservers?ip=" + ipQuery))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-type", "application/rdap+json;charset=UTF-8"))
            .andExpect(content().string(expectedContent));
    verify(nameserverService, times(1)).searchByIp(ipQuery);
    verify(nameserverService, never()).searchByName(anyString());
  }

  @Test
  public void testSearchByNameSuccess() throws Exception {
    String query = "ns1.example*.com";
    NameserversSearchResult nameserversSearchResult = new NameserversSearchResult(someNameservers());
    nameserversSearchResult.addRdapConformance(Nameserver.DEFAULT_RDAP_CONFORMANCE);
    when(nameserverService.searchByName(query)).thenReturn(nameserversSearchResult);
    mockMvc.perform(get("/nameservers?name=" + query))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-type", "application/rdap+json;charset=UTF-8"))
            .andExpect(content().string(expectedContent));
    verify(nameserverService, times(1)).searchByName(query);
    verify(nameserverService, never()).searchByIp(anyString());
  }

  @Test
  public void testCheckParams() throws Exception {
    mockMvc.perform(get("/nameservers")).andExpect(status().isBadRequest());
    verify(nameserverService, never()).searchByName(anyString());
    verify(nameserverService, never()).searchByIp(anyString());
    mockMvc.perform(get("/nameservers?name=name&ip=193.6.5.4")).andExpect(status().isBadRequest());
    verify(nameserverService, never()).searchByName(anyString());
    verify(nameserverService, never()).searchByIp(anyString());
  }

  @Test
  public void testWrongMediaType() throws Exception {
    mockMvc.perform(get("/nameservers?name=ns.example*.com")
            .accept(MediaType.TEXT_HTML))
            .andExpect(status().isNotAcceptable());
  }

  @Test
  public void testSearchByNameNoResultsFromService() throws Exception {
    String query = "ns1.example*.com";
    when(nameserverService.searchByName(query)).thenThrow(RDAPError.noResults(query));
    mockMvc.perform(get("nameservers?name=" + query)).andExpect(status().isNotFound());
  }

  @Test
  public void testSearchByNameNoResults() throws Exception {
    String query = "ns1.example*.com";
    when(nameserverService.searchByName(query)).thenReturn(new NameserversSearchResult(new ArrayList<Nameserver>()));
    mockMvc.perform(get("nameservers?name=" + query)).andExpect(status().isNotFound());
  }

  @Test
  public void testSearchByIpNoResultsFromService() throws Exception {
    String ipQuery = "201.5.6.166";
    when(nameserverService.searchByIp(ipQuery)).thenThrow(RDAPError.noResults(ipQuery));
    mockMvc.perform(get("nameservers?ip=" + ipQuery)).andExpect(status().isNotFound());
  }

  @Test
  public void testSearchByIpEmptyResult() throws Exception {
    String ipQuery = "201.5.6.166";
    when(nameserverService.searchByIp(ipQuery)).thenReturn(new NameserversSearchResult(null));
    mockMvc.perform(get("nameservers?ip=" + ipQuery)).andExpect(status().isNotFound());
  }

  private String expectedContent = "{\"rdapConformance\":[\"rdap_level_0\"],\"nameserverSearchResults\":[{" +
          "\"objectClassName\":\"nameserver\"," +
          "\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"]," +
          "\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\"," +
          "\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"]," +
          "\"media\":\"Media\",\"type\":\"Type\"}],\"notices\":[{\"title\":\"Title\",\"type\":\"Type\"," +
          "\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\"," +
          "\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\"," +
          "\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\"," +
          "\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{" +
          "\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{" +
          "\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"]," +
          "\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\"," +
          "\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"]," +
          "\"media\":\"Media\",\"type\":\"Type\"}]}],\"remarks\":[{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\"," +
          "\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\"," +
          "\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{" +
          "\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"]," +
          "\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"title\":\"Title\",\"type\":\"Type\"," +
          "\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\"," +
          "\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\"," +
          "\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\"," +
          "\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}],\"lang\":\"en\"," +
          "\"events\":[{\"eventAction\":\"REGISTRATION\",\"eventActor\":\"EventActor\",\"eventDate\":\"" + createTime.toString(ISODateTimeFormat.dateTimeNoMillis()) + "\",\"links\":[{" +
          "\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"]," +
          "\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\"," +
          "\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"]," +
          "\"media\":\"Media\",\"type\":\"Type\"}]},{\"eventAction\":\"LAST_CHANGED\",\"eventActor\":\"EventActor\"," +
          "\"eventDate\":\"" + lastChangedTime.toString(ISODateTimeFormat.dateTimeNoMillis()) + "\",\"links\":[{\"value\":\"http://example.com/value\"," +
          "\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"]," +
          "\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\"," +
          "\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}]," +
          "\"status\":[\"active\",\"delete prohibited\",\"some specific status\"],\"port43\":\"whois.example.com\",\"handle\":\"Handle\"," +
          "\"ldhName\":\"ns.xn--exmple-jta.com\",\"unicodeName\":\"ns.ex\u00E0mple.com\",\"ipAddresses\":{\"v4\":[\"193.5.6.198\",\"89.65.3.87\"]," +
          "\"v6\":[\"2001:678:9::1\",\"FE80:0000:0000:0000:0202:B3FF:FE1E:8329\"]}},{\"objectClassName\":\"nameserver\",\"links\":[{" +
          "\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"]," +
          "\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\"," +
          "\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"]," +
          "\"media\":\"Media\",\"type\":\"Type\"}],\"notices\":[{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\"," +
          "\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\"," +
          "\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{" +
          "\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[" +
          "\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"title\":\"Title\",\"type\":\"Type\"," +
          "\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\"," +
          "\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\"," +
          "\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\"," +
          "\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}]," +
          "\"remarks\":[{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"]," +
          "\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"]," +
          "\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\"," +
          "\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"]," +
          "\"media\":\"Media\",\"type\":\"Type\"}]},{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\"," +
          "\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\"," +
          "\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{" +
          "\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"]," +
          "\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}],\"lang\":\"en\",\"events\":[{" +
          "\"eventAction\":\"REGISTRATION\",\"eventActor\":\"EventActor\",\"eventDate\":\"" + createTime.toString(ISODateTimeFormat.dateTimeNoMillis()) +
          "\",\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"]," +
          "\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\"," +
          "\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\"," +
          "\"type\":\"Type\"}]},{\"eventAction\":\"LAST_CHANGED\",\"eventActor\":\"EventActor\"," +
          "\"eventDate\":\"" + lastChangedTime.toString(ISODateTimeFormat.dateTimeNoMillis()) + "\",\"links\":[{" +
          "\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"]," +
          "\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\"," +
          "\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"]," +
          "\"media\":\"Media\",\"type\":\"Type\"}]}],\"status\":[\"active\",\"delete prohibited\",\"some specific status\"]," +
          "\"port43\":\"whois.example.com\",\"handle\":\"Handle\",\"ldhName\":\"ns.xn--exmple-jta.com\",\"unicodeName\":\"ns.ex\u00E0mple.com\"," +
          "\"ipAddresses\":{\"v4\":[\"193.5.6.198\",\"89.65.3.87\"],\"v6\":[\"2001:678:9::1\",\"FE80:0000:0000:0000:0202:B3FF:FE1E:8329\"]}}]}";
}