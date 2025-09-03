package be.dnsbelgium.rdap;

import be.dnsbelgium.core.DomainName;
import be.dnsbelgium.rdap.controller.NameserverController;
import be.dnsbelgium.rdap.core.*;
import be.dnsbelgium.rdap.core.RDAPError;
import be.dnsbelgium.rdap.service.NameserverService;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static be.dnsbelgium.rdap.RdapMediaType.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = NameserverControllerTest.Config.class)
public class NameserverControllerTest extends AbstractControllerTest {

  private final static String REDIRECT_URL = "https://rdap.org";

  @Configuration
  static class Config extends AbstractControllerTest.Config {

    @Bean
    public NameserverService nameserverService() {
      return mock(NameserverService.class);
    }

    @Bean
    public NameserverController nameserverController() {
      return new NameserverController(REDIRECT_URL, nameserverService());
    }
  }

  @Autowired
  NameserverController nameserverController;

  @Autowired
  NameserverService nameserverService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  private MockMvc mockMvc;

  @Before
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  @After
  public void resetMock() {
    reset(nameserverService);
  }

  @Test
  public void testNotFound() throws Exception {
    when(nameserverService.getNameserver(any(DomainName.class))).thenReturn(null);
    mockMvc.perform(get("/nameserver/ns.example.com")).andExpect(status().isNotFound());
  }

  @Test
  public void testNotAuthoritative() throws Exception {
    when(nameserverService.getNameserver(any(DomainName.class))).thenThrow(RDAPError.notAuthoritative(DomainName.of("ns.example.com")));
    mockMvc.perform(get("/nameserver/ns.example.com")).andExpect(status().isMovedPermanently()).andExpect(redirectedUrl(REDIRECT_URL + "/nameserver/ns.example.com"));
  }
  
	@Test
	public void testMinimalHead() throws Exception {
		initNameServer();
		mockMvc.perform(head("/nameserver/ns.example.com")).andExpect(status().isOk());
	}
  
  @Test
  public void testAcceptRdapJsonMinimal() throws Exception {
    performGetNameserverTest(APPLICATION_RDAP_JSON);
  }

  @Test
  public void testAcceptRdapJsonUft8Minimal() throws Exception {
    performGetNameserverTest(APPLICATION_RDAP_JSON_UTF8);
  }

  @Test
  public void testAcceptJsonMinimal() throws Exception {
    performGetNameserverTest(APPLICATION_JSON);
  }

  @Test
  public void testAcceptJsonUft8Minimal() throws Exception {
    performGetNameserverTest(APPLICATION_RDAP_JSON_UTF8);
  }

  @Test
  public void testAcceptOtherAcceptHeadersMinimal() throws Exception {
    performGetNameserverTest(TEXT_HTML);
  }
  
  @Test
  public void testMethodNotAllowed() throws Exception {
      mockMvc.perform(put("/nameserver/ns.example").accept(APPLICATION_RDAP_JSON))
              .andExpect(status().isMethodNotAllowed());
  }

  @Test
  public void testMaximal() throws Exception {
    //Create some links
    Link link1 = new Link(new URI("http://example.com/domain/example"), null, new URI("http://example.com/domain/example"), null, null, null, APPLICATION_RDAP_JSON_VALUE);
    Set<String> hrefLangSet = new HashSet<>();
    hrefLangSet.add("en");
    hrefLangSet.add("nl");
    String title = "Title";
    Link link2 = new Link(new URI("http://example.com/nameserver/ns.example"), "relrel", new URI("http://example.com/nameserver/ns.example"), hrefLangSet, title, "This is media", APPLICATION_RDAP_JSON_VALUE);
    List<Link> linksList = new ArrayList<>();
    linksList.add(link1);
    linksList.add(link2);
    //end creating links

    //Notices
    List<String> noticeDescriptions = new ArrayList<>();
    noticeDescriptions.add("Call this a description!");
    noticeDescriptions.add("This one to!");
    Notice notice = new Notice("Notice title", "Notice type", noticeDescriptions, null);
    List<Notice> noticeList = new ArrayList<>();
    noticeList.add(notice);
    //end notices

    //Remarks
    List<String> remarkDescriptions = new ArrayList<>();
    remarkDescriptions.add("Describes the remark");
    Notice remark = new Notice("Remark title", "RemarkType", remarkDescriptions, null);
    List<Notice> remarksList = new ArrayList<>();
    remarksList.add(remark);
    //end remarks

    //Create some events
    DateTime createTime = DateTime.now().toDateTime(DateTimeZone.UTC).minusDays(200);
    Event created = new Event(Event.Action.Default.REGISTRATION, "Master-of-RDAP", createTime, null);
    DateTime lastChangedTime = createTime.plusDays(100);
    URI href1 = new URI("http://example.com/lastChangedTarget");
    Link lcLink1 = new Link(null, null, href1, null, null, null, null);
    URI value2 = new URI("http://example.com/lastChangedContextURI");
    URI href2 = new URI("http://example.com/lastChanged2target");
    Set<String> hrefLangs = new HashSet<>();
    hrefLangs.add("en");
    hrefLangs.add("mn-Cyrl-MN");
    String eventTitle = "This is a title";
    Link lcLink2 = new Link(value2, "related", href2, hrefLangs, eventTitle, "mediaString", APPLICATION_RDAP_JSON_VALUE);
    List<Link> lcLinkList = new ArrayList<>();
    lcLinkList.add(lcLink1);
    lcLinkList.add(lcLink2);
    Event lastChanged = new Event(Event.Action.Default.LAST_CHANGED, "RDAP-Slave", lastChangedTime, lcLinkList);
    List<Event> events = new ArrayList<>();
    events.add(created);
    events.add(lastChanged);
    //end creating events

    //Create some statuses
    List<Status> statusesList = new ArrayList<>();
    statusesList.add(Status.Default.ACTIVE);
    statusesList.add(Status.Default.DELETE_PROHIBITED);
    statusesList.add(new Status.BasicStatus("specific status"));
    //end creating statusses

    Nameserver nameserver = new Nameserver(linksList, noticeList, remarksList, "en", events, statusesList, DomainName.of("whois.example.com"), "This is a Handle", DomainName.of("ns.example.com"), DomainName.of("ns.example.com"), someIpAddresses());
    nameserver.addRdapConformance(Nameserver.DEFAULT_RDAP_CONFORMANCE);
    when(nameserverService.getNameserver(any(DomainName.class))).thenReturn(nameserver);
    mockMvc.perform(get("/nameserver/ns.example.com")
            .accept(parseMediaType("application/json")))
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_RDAP_JSON))
            .andExpect(content().string("{\"rdapConformance\":[\"rdap_level_0\"],\"objectClassName\":\"nameserver\",\"links\":[{\"value\":\"http://example.com/domain/example\"," +
                    "\"href\":\"http://example.com/domain/example\",\"type\":\"application/rdap+json\"},{\"value\":\"http://example.com/nameserver/ns.example\"," +
                    "\"rel\":\"relrel\",\"href\":\"http://example.com/nameserver/ns.example\",\"hreflang\":[\"en\",\"nl\"],\"title\":\"Title\"," +
                    "\"media\":\"This is media\",\"type\":\"application/rdap+json\"}],\"notices\":[{\"title\":\"Notice title\",\"type\":\"Notice type\",\"description\":[\"Call this a description!\",\"This one to!\"]}]," +
                    "\"remarks\":[{\"title\":\"Remark title\",\"type\":\"RemarkType\",\"description\":[\"Describes the remark\"]}],\"lang\":\"en\"," +
                    "\"events\":[{\"eventAction\":\"registration\",\"eventActor\":\"Master-of-RDAP\",\"eventDate\":\"" + createTime.toString(ISODateTimeFormat.dateTimeNoMillis()) + "\"}," +
                    "{\"eventAction\":\"last changed\",\"eventActor\":\"RDAP-Slave\",\"eventDate\":\"" + lastChangedTime.toString(ISODateTimeFormat.dateTimeNoMillis()) + "\"," +
                    "\"links\":[{\"href\":\"http://example.com/lastChangedTarget\"},{\"value\":\"http://example.com/lastChangedContextURI\",\"rel\":\"related\"," +
                    "\"href\":\"http://example.com/lastChanged2target\",\"hreflang\":[\"mn-Cyrl-MN\",\"en\"],\"title\":\"This is a title\",\"media\":\"mediaString\"," +
                    "\"type\":\"application/rdap+json\"}]}],\"status\":[\"active\",\"delete prohibited\",\"specific status\"],\"port43\":\"whois.example.com\"," +
                    "\"handle\":\"This is a Handle\"," +
                    "\"ldhName\":\"ns.example.com\"," +
                    "\"unicodeName\":\"ns.example.com\"," +
                    "\"ipAddresses\":{\"v4\":[\"193.5.6.198\",\"89.65.3.87\"],\"v6\":[\"2001:678:9::1\",\"FE80:0000:0000:0000:0202:B3FF:FE1E:8329\"]}}"));
  }

  public void performGetNameserverTest(MediaType acceptHeader) throws Exception {
    initNameServer();
    mockMvc.perform(get("/nameserver/ns.example").accept(acceptHeader))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(APPLICATION_RDAP_JSON))
        .andExpect(content().string("{\"rdapConformance\":[\"rdap_level_0\"],\"objectClassName\":\"nameserver\",\"ldhName\":\"ns.example.com\"}"));
  }

  public void initNameServer() throws RDAPError {
    Nameserver nameserver = new Nameserver(null, null, null, null, null, null, null, null, DomainName.of("ns.example.com"), null, null);
    nameserver.addRdapConformance(Nameserver.DEFAULT_RDAP_CONFORMANCE);
    when(nameserverService.getNameserver(any(DomainName.class))).thenReturn(nameserver);
  }
}