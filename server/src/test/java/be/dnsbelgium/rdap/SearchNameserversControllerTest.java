package be.dnsbelgium.rdap;

import be.dnsbelgium.rdap.controller.SearchNameserversController;
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
      return new SearchNameserversController(nameserverService());
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
  public void testSearchByIpSuccessJson() throws Exception {
    performSearchBy("ip", "application/json");
  }

  @Test
  public void testSearchByIpSuccessRdapJson() throws Exception {
    performSearchBy("ip", "application/rdap+json");
  }

  @Test
  public void testSearchByIpSuccessOtherHeader() throws Exception {
    performSearchBy("ip", "text/html");
  }

  @Test
  public void testSearchByNameSuccessJson() throws Exception {
    performSearchBy("name", "application/json");
  }

  @Test
  public void testSearchByNameSuccessRdapJson() throws Exception {
    performSearchBy("name", "application/rdap+json");
  }

  @Test
  public void testSearchByNameSuccessOtherHeader() throws Exception {
    performSearchBy("name", "text/html");
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
  public void testSearchByNameNoResultsFromService() throws Exception {
    String query = "ns1.example*.com";
    when(nameserverService.searchByName(query)).thenThrow(RDAPError.noResults(query));
    mockMvc.perform(get("/nameservers?name=" + query))
            .andExpect(status().isNotFound())
            .andExpect(header().string("Content-type", "application/rdap+json;charset=UTF-8"));
  }

  @Test
  public void testSearchByNameNoResults() throws Exception {
    String query = "ns1.example*.com";
    when(nameserverService.searchByName(query)).thenReturn(new NameserversSearchResult(new ArrayList<>()));
    mockMvc.perform(get("/nameservers?name=" + query))
            .andExpect(status().isNotFound())
            .andExpect(header().string("Content-type", "application/rdap+json;charset=UTF-8"));
  }

  @Test
  public void testSearchByIpNoResultsFromService() throws Exception {
    String ipQuery = "201.5.6.166";
    when(nameserverService.searchByIp(ipQuery)).thenThrow(RDAPError.noResults(ipQuery));
    mockMvc.perform(get("/nameservers?ip=" + ipQuery))
            .andExpect(status().isNotFound())
            .andExpect(header().string("Content-type", "application/rdap+json;charset=UTF-8"));
  }

  @Test
  public void testSearchByIpEmptyResult() throws Exception {
    String ipQuery = "201.5.6.166";
    when(nameserverService.searchByIp(ipQuery)).thenReturn(new NameserversSearchResult(null));
    mockMvc.perform(get("/nameservers?ip=" + ipQuery))
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(MediaType.valueOf("application/rdap+json")));
  }

  public void performSearchBy(String searchBy, String acceptHeader) throws Exception {
    NameserversSearchResult nameserversSearchResult = new NameserversSearchResult(someNameservers());
    nameserversSearchResult.addRdapConformance(Nameserver.DEFAULT_RDAP_CONFORMANCE);

    if (searchBy.equals("name")) {
      String query = "ns1.example*.com";

      when(nameserverService.searchByName(query)).thenReturn(nameserversSearchResult);
      mockMvc.perform(get("/nameservers?name=" + query).accept(MediaType.parseMediaType(acceptHeader)))
              .andExpect(status().isOk())
              .andExpect(content().contentTypeCompatibleWith(MediaType.valueOf("application/rdap+json")))
              .andExpect(content().string(expectedContent));
      verify(nameserverService, times(1)).searchByName(query);
      verify(nameserverService, never()).searchByIp(anyString());
    }
    if (searchBy.equals("ip")) {
      String ipQuery = "193.56.54.32";

      when(nameserverService.searchByIp(ipQuery)).thenReturn(nameserversSearchResult);
      mockMvc.perform(get("/nameservers?ip=" + ipQuery).accept(MediaType.parseMediaType(acceptHeader)))
              .andExpect(status().isOk())
              .andExpect(content().contentTypeCompatibleWith(MediaType.valueOf("application/rdap+json")))
              .andExpect(content().string(expectedContent));
      verify(nameserverService, times(1)).searchByIp(ipQuery);
      verify(nameserverService, never()).searchByName(anyString());
    }
  }

  private String expectedContent = "{\"rdapConformance\":[\"rdap_level_0\"],\"nameserverSearchResults\":[{" +
          "\"objectClassName\":\"nameserver\"," +
          "\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"]," +
          "\"title\":\"Title\",\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\"," +
          "\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":\"Title\"," +
          "\"media\":\"Media\",\"type\":\"Type\"}],\"notices\":[{\"title\":\"Title\",\"type\":\"Type\"," +
          "\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\"," +
          "\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":\"Title\",\"media\":\"Media\"," +
          "\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\"," +
          "\"hreflang\":[\"de\",\"en\"],\"title\":\"Title\",\"media\":\"Media\",\"type\":\"Type\"}]},{" +
          "\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{" +
          "\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"]," +
          "\"title\":\"Title\",\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\"," +
          "\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":\"Title\"," +
          "\"media\":\"Media\",\"type\":\"Type\"}]}],\"remarks\":[{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\"," +
          "\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\"," +
          "\"hreflang\":[\"de\",\"en\"],\"title\":\"Title\",\"media\":\"Media\",\"type\":\"Type\"},{" +
          "\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"]," +
          "\"title\":\"Title\",\"media\":\"Media\",\"type\":\"Type\"}]},{\"title\":\"Title\",\"type\":\"Type\"," +
          "\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\"," +
          "\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":\"Title\",\"media\":\"Media\"," +
          "\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\"," +
          "\"hreflang\":[\"de\",\"en\"],\"title\":\"Title\",\"media\":\"Media\",\"type\":\"Type\"}]}],\"lang\":\"en\"," +
          "\"events\":[{\"eventAction\":\"registration\",\"eventActor\":\"EventActor\",\"eventDate\":\"" + createTime.toString(ISODateTimeFormat.dateTimeNoMillis()) + "\",\"links\":[{" +
          "\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"]," +
          "\"title\":\"Title\",\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\"," +
          "\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":\"Title\"," +
          "\"media\":\"Media\",\"type\":\"Type\"}]},{\"eventAction\":\"last changed\",\"eventActor\":\"EventActor\"," +
          "\"eventDate\":\"" + lastChangedTime.toString(ISODateTimeFormat.dateTimeNoMillis()) + "\",\"links\":[{\"value\":\"http://example.com/value\"," +
          "\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":\"Title\"," +
          "\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\"," +
          "\"hreflang\":[\"de\",\"en\"],\"title\":\"Title\",\"media\":\"Media\",\"type\":\"Type\"}]}]," +
          "\"status\":[\"active\",\"delete prohibited\",\"some specific status\"],\"port43\":\"whois.example.com\",\"handle\":\"Handle\"," +
          "\"ldhName\":\"ns.xn--exmple-jta.com\",\"unicodeName\":\"ns.ex\u00E0mple.com\",\"ipAddresses\":{\"v4\":[\"193.5.6.198\",\"89.65.3.87\"]," +
          "\"v6\":[\"2001:678:9::1\",\"FE80:0000:0000:0000:0202:B3FF:FE1E:8329\"]}},{\"objectClassName\":\"nameserver\",\"links\":[{" +
          "\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"]," +
          "\"title\":\"Title\",\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\"," +
          "\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":\"Title\"," +
          "\"media\":\"Media\",\"type\":\"Type\"}],\"notices\":[{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\"," +
          "\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\"," +
          "\"hreflang\":[\"de\",\"en\"],\"title\":\"Title\",\"media\":\"Media\",\"type\":\"Type\"},{" +
          "\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":" +
          "\"Title\",\"media\":\"Media\",\"type\":\"Type\"}]},{\"title\":\"Title\",\"type\":\"Type\"," +
          "\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\"," +
          "\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":\"Title\",\"media\":\"Media\"," +
          "\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\"," +
          "\"hreflang\":[\"de\",\"en\"],\"title\":\"Title\",\"media\":\"Media\",\"type\":\"Type\"}]}]," +
          "\"remarks\":[{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"]," +
          "\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"]," +
          "\"title\":\"Title\",\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\"," +
          "\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":\"Title\"," +
          "\"media\":\"Media\",\"type\":\"Type\"}]},{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\"," +
          "\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\"," +
          "\"hreflang\":[\"de\",\"en\"],\"title\":\"Title\",\"media\":\"Media\",\"type\":\"Type\"},{" +
          "\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"]," +
          "\"title\":\"Title\",\"media\":\"Media\",\"type\":\"Type\"}]}],\"lang\":\"en\",\"events\":[{" +
          "\"eventAction\":\"registration\",\"eventActor\":\"EventActor\",\"eventDate\":\"" + createTime.toString(ISODateTimeFormat.dateTimeNoMillis()) +
          "\",\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"]," +
          "\"title\":\"Title\",\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\"," +
          "\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":\"Title\",\"media\":\"Media\"," +
          "\"type\":\"Type\"}]},{\"eventAction\":\"last changed\",\"eventActor\":\"EventActor\"," +
          "\"eventDate\":\"" + lastChangedTime.toString(ISODateTimeFormat.dateTimeNoMillis()) + "\",\"links\":[{" +
          "\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"]," +
          "\"title\":\"Title\",\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\"," +
          "\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":\"Title\"," +
          "\"media\":\"Media\",\"type\":\"Type\"}]}],\"status\":[\"active\",\"delete prohibited\",\"some specific status\"]," +
          "\"port43\":\"whois.example.com\",\"handle\":\"Handle\",\"ldhName\":\"ns.xn--exmple-jta.com\",\"unicodeName\":\"ns.ex\u00E0mple.com\"," +
          "\"ipAddresses\":{\"v4\":[\"193.5.6.198\",\"89.65.3.87\"],\"v6\":[\"2001:678:9::1\",\"FE80:0000:0000:0000:0202:B3FF:FE1E:8329\"]}}]}";
}