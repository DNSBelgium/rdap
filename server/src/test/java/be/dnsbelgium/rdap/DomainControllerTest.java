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
import be.dnsbelgium.rdap.core.*;
import be.dnsbelgium.rdap.core.Error;
import be.dnsbelgium.rdap.jackson.CustomObjectMapper;
import be.dnsbelgium.rdap.service.DomainService;
import be.dnsbelgium.vcard.Contact;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
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
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
  public void testNotAuthoritative() throws Exception {
    Mockito.when(domainService.getDomain(Mockito.any(DomainName.class))).thenThrow(new Error.NotAuthoritative(DomainName.of("example")));
    MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    mockMvc.perform(get("/domain/example")).andExpect(status().isMovedPermanently()).andExpect(redirectedUrl(REDIRECT_URL + "/domain/example"));
  }

  @Test
  public void testDefault() throws Exception {
    DomainName domainName = DomainName.of("example.com");
    Domain domain = new Domain(null, null, null, null, Domain.OBJECT_CLASS_NAME, null, null, null, null, domainName, domainName, null, null, null, null, null, null);
    Mockito.when(domainService.getDomain(Mockito.any(DomainName.class))).thenReturn(domain);
    MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    mockMvc.perform(get("/domain/example")).andExpect(status().isOk()).andExpect(jsonPath("$.ldhName", "example.com").exists());
  }

  @Test
  public void testNoGlue() throws Exception{
    DomainName domainName = DomainName.of("example.com");
    DomainName nameserverName = DomainName.of("ns.example.other");
    Nameserver nameserver = new Nameserver(null, null, null, null, Nameserver.OBJECT_CLASS_NAME, null, null, null, null, nameserverName, nameserverName, null);
    List<Nameserver> nameserverList = new ArrayList<Nameserver>();
    nameserverList.add(nameserver);
    Domain domain = new Domain(null, null, null, null, Domain.OBJECT_CLASS_NAME, null, null, null, null, domainName, domainName, null, nameserverList, null, null, null, null);
    Mockito.when(domainService.getDomain(Mockito.any(DomainName.class))).
            thenReturn(domain);
    MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    mockMvc.perform(get("/domain/example")
            .accept(MediaType.parseMediaType("application/rdap+json")))
            .andExpect(header().string("Content-type", "application/rdap+json;charset=UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"rdapConformance\":[\"rdap_level_0\"],\"objectClassName\":\"domain\",\"ldhName\":\"example.com\",\"unicodeName\":\"example.com\",\"nameservers\":[{\"objectClassName\":\"nameserver\",\"ldhName\":\"ns.example.other\",\"unicodeName\":\"ns.example.other\"}]}"));
  }

  @Test
  public void testMaximalDomain() throws Exception {
    DomainName domainName = DomainName.of("example.com");

    //Notices
    List<String> descriptions = new ArrayList<String>();
    descriptions.add("Call this a description!");
    descriptions.add("This one to!");
    Notice notice = new Notice("Notice", descriptions, null);
    List<Notice> notices = new ArrayList<Notice>();
    notices.add(notice);
    //end notices

    //Create some links
    Link link1 = new Link(new URI("http://example.com/domain/example"), "self", new URI("http://example.com/domain/example"), null, null, null, "application/rdap+json");
    List<Link> linksList = new ArrayList<Link>();
    linksList.add(link1);
    //end creating links

    //Create some statuses
    List<Status> statusesList = new ArrayList<Status>();
    statusesList.add(Status.Default.ACTIVE);
    statusesList.add(Status.Default.DELETE_PROHIBITED);
    statusesList.add(new Status.BasicStatus("specific status"));
    //end creating statusses

    //Create some events
    DateTime createTime = DateTime.now().toDateTime(DateTimeZone.UTC).minusDays(200);
    Event created = new Event(Event.Action.Default.REGISTRATION, "Master-of-RDAP", createTime, null);
    DateTime lastChangedTime = createTime.plusDays(100);
    URI href1 = new URI("http://example.com/lastChangedTarget");
    Link lcLink1 = new Link(null, null, href1, null, null, null, null);
    URI value2 = new URI("http://example.com/lastChangedContextURI");
    URI href2 = new URI("http://example.com/lastChanged2target");
    Set<String> hrefLangs = new HashSet<String>();
    hrefLangs.add("en");
    hrefLangs.add("mn-Cyrl-MN");
    List<String> titleList = new ArrayList<String>();
    titleList.add("This is a title");
    Link lcLink2 = new Link(value2, "related", href2, hrefLangs, titleList, "mediaString", "application/rdap+json");
    List<Link> lcLinkList = new ArrayList<Link>();
    lcLinkList.add(lcLink1);
    lcLinkList.add(lcLink2);
    Event lastChanged = new Event(Event.Action.Default.LAST_CHANGED, "RDAP-Slave", lastChangedTime, lcLinkList);
    List<Event> events = new ArrayList<Event>();
    events.add(created);
    events.add(lastChanged);
    //end creating events

    Domain domain = new Domain(linksList, notices, null, "en", Domain.OBJECT_CLASS_NAME, events, statusesList, DomainName.of("whois.example.com"), "exampleHandle", domainName, domainName, null, null,
            initSecureDNS(), initDefaultEntities(), null, null);
    Mockito.when(domainService.getDomain(Mockito.any(DomainName.class))).thenReturn(domain);
    MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    mockMvc.perform(get("/domain/example")
            .accept(MediaType.parseMediaType("application/rdap+json")))
            .andExpect(header().string("Content-type", "application/rdap+json;charset=UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"rdapConformance\":[\"rdap_level_0\"],\"links\":[{\"value\":\"http://example.com/domain/example\",\"rel\":\"self\",\"href\":\"http://example.com/domain/example\",\"type\":\"application/rdap+json\"}],\"notices\":[{\"title\":\"Notice\",\"description\":[\"Call this a description!\",\"This one to!\"]}],\"lang\":\"en\",\"objectClassName\":\"domain\",\"events\":" +
                    "[{\"eventAction\":\"REGISTRATION\",\"eventActor\":\"Master-of-RDAP\",\"eventDate\":\"" + createTime.toString(ISODateTimeFormat.dateTimeNoMillis()) + "\"}," +
                    "{\"eventAction\":\"LAST_CHANGED\",\"eventActor\":\"RDAP-Slave\",\"eventDate\":\"" + lastChangedTime.toString(ISODateTimeFormat.dateTimeNoMillis()) +
                    "\",\"links\":[{\"href\":\"http://example.com/lastChangedTarget\"},{\"value\":\"http://example.com/lastChangedContextURI\",\"rel\":\"related\"," +
                    "\"href\":\"http://example.com/lastChanged2target\",\"hreflang\":[\"mn-Cyrl-MN\",\"en\"],\"title\":[\"This is a title\"],\"media\":\"mediaString\",\"type\":\"application/rdap+json\"}]}],\"status\":[\"active\",\"delete prohibited\",\"specific status\"],\"port43\":\"whois.example.com\"," +
                    "\"handle\":\"exampleHandle\",\"ldhName\":\"example.com\"," +
                    "\"unicodeName\":\"example.com\"," +
                    "\"secureDNS\":{\"zoneSigned\":true,\"delegationSigned\":true,\"maxSigLife\":6000,\"dsData\":[{\"keyTag\":64156,\"algorithm\":8," +
                    "\"digest\":\"DC48B4183F9AC496574DEB8633F627A6DE207493\",\"digestType\":1},{\"keyTag\":64156,\"algorithm\":8," +
                    "\"digest\":\"DE3BBED2664B02B9FEC6FF81D8539B14A5714A2C7A92E8FE58914200 C30B1958\",\"digestType\":2}]}," +
                    "\"entities\":[{\"lang\":\"en\",\"objectClassName\":\"entity\",\"handle\":\"REGISTRANT\",\"vCard\":[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"This is a formatted name\"],[\"adr\",{},\"text\",null,null,\"street 1\",null,null,null,null]],\"roles\":[\"REGISTRANT\",\"ADMINISTRATIVE\"]}]}"));
  }

  @Test
  public void testBytes() throws Exception {
    DomainName domainName = DomainName.of("example.com");
    Domain domain = new Domain(null, null, null, null, Domain.OBJECT_CLASS_NAME, null, null, null, null, domainName, domainName, null, null, null, null, null, null);
    Mockito.when(domainService.getDomain(Mockito.any(DomainName.class))).thenReturn(domain);
    MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    mockMvc.perform(get("/domain/example")
        .accept(MediaType.parseMediaType("application/rdap+json")))
        .andExpect(header().string("Content-type", "application/rdap+json;charset=UTF-8"))
        .andExpect(status().isOk())
        .andExpect(content().string("{\"rdapConformance\":[\"rdap_level_0\"],\"objectClassName\":\"domain\",\"ldhName\":\"example.com\",\"unicodeName\":\"example.com\"}"));
  }

  @Test
  public void testWrongMediaType() throws Exception {
    DomainName domainName = DomainName.of("example.com");
    Domain domain = new Domain(null, null, null, null, Domain.OBJECT_CLASS_NAME, null, null, null, null, domainName, domainName, null, null, null, null, null, null);
    Mockito.when(domainService.getDomain(Mockito.any(DomainName.class))).thenReturn(domain);
    MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    mockMvc.perform(get("/domain/example")
            .accept(MediaType.TEXT_HTML))
        .andExpect(status().isNotAcceptable());
  }

  @Test
  public void testIDNParseException() throws Exception {
    MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    mockMvc.perform(get("/domain/-\u2620-.be")
        .accept(MediaType.parseMediaType("application/rdap+json")))
        .andExpect(header().string("Content-type", Controllers.CONTENT_TYPE))
        .andExpect(content().string("{\"errorCode\":400,\"title\":\"Invalid domain name\",\"description\":[\"LEADING_HYPHEN\",\"TRAILING_HYPHEN\",\"DISALLOWED\"]}"))
        .andExpect(status().isBadRequest());
  }

  private List<Entity> initDefaultEntities() {
    List<Entity> entityList = new ArrayList<Entity>();
    Contact vCard = new Contact.Builder().addOU("This is an OU").addStreet("street 1").setFormattedName("This is a formatted name").build();
    List<Entity.Role> rolesList = new ArrayList<Entity.Role>();
    rolesList.add(Entity.Role.Default.REGISTRANT);
    rolesList.add(Entity.Role.Default.ADMINISTRATIVE);
    Entity registrant = new Entity(null, null, null, "en", Entity.OBJECT_CLASS_NAME, null, null, null, "REGISTRANT", vCard, rolesList, null, null);
    entityList.add(registrant);
    return entityList;
  }

  private SecureDNS initSecureDNS() {
    SecureDNS.DSData dsData1 = new SecureDNS.DSData(64156, 8, "DC48B4183F9AC496574DEB8633F627A6DE207493", 1, null, null);
    SecureDNS.DSData dsData2 = new SecureDNS.DSData(64156, 8, "DE3BBED2664B02B9FEC6FF81D8539B14A5714A2C7A92E8FE58914200 C30B1958", 2, null, null);
    List<SecureDNS.DSData> dsList = new ArrayList<SecureDNS.DSData>();
    dsList.add(dsData1);
    dsList.add(dsData2);
    return new SecureDNS(true, true, 6000, dsList, null);
  }
}
