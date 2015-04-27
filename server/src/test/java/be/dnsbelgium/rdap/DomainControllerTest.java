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
import be.dnsbelgium.rdap.service.DomainService;
import be.dnsbelgium.vcard.Contact;
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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = DomainControllerTest.Config.class)
public class DomainControllerTest extends AbstractControllerTest {

  private final static int REDIRECT_THRESHOLD = 3;
  private final static String REDIRECT_URL = "https://rdap.org";

  @Configuration
  static class Config extends AbstractControllerTest.Config {

    @Bean
    public DomainService domainService() {
      return Mockito.mock(DomainService.class);
    }

    @Bean
    public DomainController domainController() {
      return new DomainController(REDIRECT_URL, REDIRECT_THRESHOLD);
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
    mockMvc.perform(get("/domain/example")).andExpect(status().isNotFound());
  }

  @Test
  public void testNotAuthoritative() throws Exception {
    when(domainService.getDomain(Mockito.any(DomainName.class))).thenThrow(new Error.NotAuthoritative(DomainName.of("example")));
    mockMvc.perform(get("/domain/example")).andExpect(status().isMovedPermanently()).andExpect(redirectedUrl(REDIRECT_URL + "/domain/example"));
  }

  @Test
  public void testDefault() throws Exception {
    DomainName domainName = DomainName.of("example.com");
    Domain domain = new Domain(null, null, null, null, Domain.OBJECT_CLASS_NAME, null, null, null, null, domainName, domainName, null, null, null, null, null, null);
    when(domainService.getDomain(Mockito.any(DomainName.class))).thenReturn(domain);
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
    when(domainService.getDomain(Mockito.any(DomainName.class))).
            thenReturn(domain);
    mockMvc.perform(get("/domain/example")
            .accept(MediaType.parseMediaType("application/rdap+json")))
            .andExpect(header().string("Content-type", "application/rdap+json;charset=UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"rdapConformance\":[\"rdap_level_0\"],\"objectClassName\":\"domain\",\"ldhName\":\"example.com\",\"unicodeName\":\"example.com\",\"nameservers\":[{\"objectClassName\":\"nameserver\",\"ldhName\":\"ns.example.other\",\"unicodeName\":\"ns.example.other\"}]}"));
  }

  @Test
  public void testMaximalDomain() throws Exception {
    DomainName domainName = DomainName.of("example.com");

    Domain domain = new Domain(someLinks(), someNotices(), someRemarks(), "en", Domain.OBJECT_CLASS_NAME, someEvents(), someStatuses(), DomainName.of("whois.example.com"),
            "Handle", domainName, domainName, someVariants(), someNameservers(), aSecureDNS(), someEntities(), somePublicIds(), anIPNetwork());
    when(domainService.getDomain(Mockito.any(DomainName.class))).thenReturn(domain);
    mockMvc.perform(get("/domain/example")
            .accept(MediaType.parseMediaType("application/rdap+json")))
            .andExpect(header().string("Content-type", "application/rdap+json;charset=UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"rdapConformance\":[\"rdap_level_0\"]," +
                    "\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]," +
                    "\"notices\":[{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}]," +
                    "\"remarks\":[{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}]," +
                    "\"lang\":\"en\"," +
                    "\"objectClassName\":\"domain\"," +
                    "\"events\":[{\"eventAction\":\"REGISTRATION\",\"eventActor\":\"EventActor\",\"eventDate\":\"" + createTime.toString(ISODateTimeFormat.dateTimeNoMillis()) + "\",\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"eventAction\":\"LAST_CHANGED\",\"eventActor\":\"EventActor\",\"eventDate\":\"" + lastChangedTime.toString(ISODateTimeFormat.dateTimeNoMillis()) + "\",\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}],\"status\":[\"active\",\"delete prohibited\",\"some specific status\"]," +
                    "\"port43\":\"whois.example.com\"," +
                    "\"handle\":\"Handle\"," +
                    "\"ldhName\":\"example.com\"," +
                    "\"unicodeName\":\"example.com\"," +
                    "\"variants\":[{\"variantNames\":[{\"ldhName\":\"exomple.com\",\"unicodeName\":\"exomple.com\"},{\"ldhName\":\"eximple.com\",\"unicodeName\":\"eximple.com\"}],\"relations\":[\"UNREGISTERED\",\"RESTRICTED_REGISTRATION\"]},{\"variantNames\":[{\"ldhName\":\"xn--exmple-jta.com\",\"unicodeName\":\"exàmple.com\"}],\"relations\":[\"REGISTERED\"]}]," +
                    "\"secureDNS\":{\"zoneSigned\":true,\"delegationSigned\":true,\"maxSigLife\":6000,\"dsData\":[{\"keyTag\":64156,\"algorithm\":8,\"digest\":\"DC48B4183F9AC496574DEB8633F627A6DE207493\",\"digestType\":1},{\"keyTag\":64156,\"algorithm\":8,\"digest\":\"DE3BBED2664B02B9FEC6FF81D8539B14A5714A2C7A92E8FE58914200 C30B1958\",\"digestType\":2}]}," +
                    "\"entities\":[{\"lang\":\"en\",\"objectClassName\":\"entity\",\"handle\":\"REGISTRANT\",\"vCard\":[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"This is a formatted name\"],[\"adr\",{},\"text\",null,null,\"street 1\",null,null,null,null]],\"roles\":[\"REGISTRANT\",\"ADMINISTRATIVE\"]}]," +
                    "\"publicIds\":[{\"type\":\"Type\",\"identifier\":\"Identifier\"},{\"type\":\"Type\",\"identifier\":\"Identifier\"}]," +
                    "\"network\":{\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}],\"notices\":[{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}],\"remarks\":[{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}],\"lang\":\"en\",\"objectClassName\":\"ip network\",\"events\":[{\"eventAction\":\"REGISTRATION\",\"eventActor\":\"EventActor\",\"eventDate\":\"" + createTime.toString(ISODateTimeFormat.dateTimeNoMillis()) + "\",\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"eventAction\":\"LAST_CHANGED\",\"eventActor\":\"EventActor\",\"eventDate\":\"" + lastChangedTime.toString(ISODateTimeFormat.dateTimeNoMillis()) + "\",\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}],\"status\":[\"active\",\"delete prohibited\",\"some specific status\"],\"port43\":\"whois.example.com\",\"handle\":\"Handle\",\"startAddress\":\"193.12.32.98\",\"endAddress\":\"193.12.32.98\",\"name\":\"Name\",\"type\":\"Type\",\"country\":\"Country\",\"parentHandle\":\"ParentHandle\",\"entities\":[{\"lang\":\"en\",\"objectClassName\":\"entity\",\"handle\":\"REGISTRANT\",\"vCard\":[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"This is a formatted name\"],[\"adr\",{},\"text\",null,null,\"street 1\",null,null,null,null]],\"roles\":[\"REGISTRANT\",\"ADMINISTRATIVE\"]}]}," +
                    "\"nameservers\":[{\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}],\"notices\":[{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}],\"remarks\":[{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}],\"lang\":\"en\",\"objectClassName\":\"nameserver\",\"events\":[{\"eventAction\":\"REGISTRATION\",\"eventActor\":\"EventActor\",\"eventDate\":\"" + createTime.toString(ISODateTimeFormat.dateTimeNoMillis()) + "\",\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"eventAction\":\"LAST_CHANGED\",\"eventActor\":\"EventActor\",\"eventDate\":\"" + lastChangedTime.toString(ISODateTimeFormat.dateTimeNoMillis()) + "\",\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}],\"status\":[\"active\",\"delete prohibited\",\"some specific status\"],\"port43\":\"whois.example.com\",\"handle\":\"Handle\",\"ldhName\":\"ns.xn--exmple-jta.com\",\"unicodeName\":\"ns.exàmple.com\",\"ipAddresses\":{\"v4\":[\"193.5.6.198\",\"89.65.3.87\"],\"v6\":[\"2001:678:9::1\",\"FE80:0000:0000:0000:0202:B3FF:FE1E:8329\"]}},{\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}],\"notices\":[{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}],\"remarks\":[{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}],\"lang\":\"en\",\"objectClassName\":\"nameserver\",\"events\":[{\"eventAction\":\"REGISTRATION\",\"eventActor\":\"EventActor\",\"eventDate\":\"" + createTime.toString(ISODateTimeFormat.dateTimeNoMillis()) + "\",\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"eventAction\":\"LAST_CHANGED\",\"eventActor\":\"EventActor\",\"eventDate\":\"" + lastChangedTime.toString(ISODateTimeFormat.dateTimeNoMillis()) + "\",\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}],\"status\":[\"active\",\"delete prohibited\",\"some specific status\"],\"port43\":\"whois.example.com\",\"handle\":\"Handle\",\"ldhName\":\"ns.xn--exmple-jta.com\",\"unicodeName\":\"ns.exàmple.com\",\"ipAddresses\":{\"v4\":[\"193.5.6.198\",\"89.65.3.87\"],\"v6\":[\"2001:678:9::1\",\"FE80:0000:0000:0000:0202:B3FF:FE1E:8329\"]}}]}"));
  }

  @Test
  public void testBytes() throws Exception {
    DomainName domainName = DomainName.of("example.com");
    Domain domain = new Domain(null, null, null, null, Domain.OBJECT_CLASS_NAME, null, null, null, null, domainName, domainName, null, null, null, null, null, null);
    when(domainService.getDomain(Mockito.any(DomainName.class))).thenReturn(domain);
    mockMvc.perform(get("/domain/example")
        .accept(MediaType.parseMediaType("application/rdap+json")))
        .andExpect(header().string("Content-type", "application/rdap+json;charset=UTF-8"))
        .andExpect(status().isOk())
        .andExpect(content().string("{\"rdapConformance\":[\"rdap_level_0\"],\"objectClassName\":\"domain\",\"ldhName\":\"example.com\",\"unicodeName\":\"example.com\"}"));
  }

  @Test
  public void testWrongMediaType() throws Exception {
    mockMvc.perform(get("/domain/example")
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

  protected List<Entity> someEntities() {
    List<Entity> entityList = new ArrayList<Entity>();
    Contact vCard = new Contact.Builder().addOU("This is an OU").addStreet("street 1").setFormattedName("This is a formatted name").build();
    Entity registrant = new Entity(null, null, null, "en", Entity.OBJECT_CLASS_NAME, null, null, null, "REGISTRANT", vCard, someRoles(), null, null);
    entityList.add(registrant);
    return entityList;
  }

  private SecureDNS aSecureDNS() {
    SecureDNS.DSData dsData1 = new SecureDNS.DSData(64156, 8, "DC48B4183F9AC496574DEB8633F627A6DE207493", 1, null, null);
    SecureDNS.DSData dsData2 = new SecureDNS.DSData(64156, 8, "DE3BBED2664B02B9FEC6FF81D8539B14A5714A2C7A92E8FE58914200 C30B1958", 2, null, null);
    List<SecureDNS.DSData> dsList = new ArrayList<SecureDNS.DSData>();
    dsList.add(dsData1);
    dsList.add(dsData2);
    return new SecureDNS(true, true, 6000, dsList, null);
  }

  private List<Domain.Variant> someVariants() {
    List<Domain.Variant.Relation> relations1 = new ArrayList<Domain.Variant.Relation>();
    relations1.add(Domain.Variant.Relation.Default.UNREGISTERED);
    relations1.add(Domain.Variant.Relation.Default.RESTRICTED_REGISTRATION);
    List<Domain.Variant.Name> names1 = new ArrayList<Domain.Variant.Name>();
    names1.add(new Domain.Variant.Name(DomainName.of("exomple.com"), DomainName.of("exomple.com")));
    names1.add(new Domain.Variant.Name(DomainName.of("eximple.com"), DomainName.of("eximple.com")));
    List<Domain.Variant.Relation> relations2 = new ArrayList<Domain.Variant.Relation>();
    relations2.add(Domain.Variant.Relation.Default.REGISTERED);
    List<Domain.Variant.Name> names2 = new ArrayList<Domain.Variant.Name>();
    names2.add(new Domain.Variant.Name(DomainName.of("xn--exmple-jta.com"), DomainName.of("exàmple.com")));
    List<Domain.Variant> variants = new ArrayList<Domain.Variant>();
    variants.add(new Domain.Variant(relations1, "IdnTable", names1));
    variants.add(new Domain.Variant(relations2, "IdnTable2", names2));
    return variants;
  }

  private IPNetwork anIPNetwork() throws Exception {
    return new IPNetwork(someLinks(), someNotices(), someRemarks(), "en", IPNetwork.OBJECT_CLASS_NAME, someEvents(), someStatuses(), DomainName.of("whois.example.com"), "Handle", InetAddress.getByName("193.12.32.98"), InetAddress.getByName("193.12.32.98"), "Name", "Type", "Country", "ParentHandle", someEntities());
  }
}
