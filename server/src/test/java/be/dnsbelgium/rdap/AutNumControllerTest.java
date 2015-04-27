package be.dnsbelgium.rdap;

import be.dnsbelgium.core.DomainName;
import be.dnsbelgium.rdap.core.AutNum;
import be.dnsbelgium.rdap.service.AutNumService;
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

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = AutNumControllerTest.Config.class)
public class AutNumControllerTest extends AbstractControllerTest {

  @Configuration
  static class Config extends AbstractControllerTest.Config {

    @Bean
    public AutNumService autNumService() {
      return mock(AutNumService.class);
    }

    @Bean
    public AutNumController autNumController() {
      return new AutNumController();
    }
  }

  @Autowired
  AutNumService autNumService;

  @Autowired
  AutNumController autNumController;

  @After
  public void resetMock() {
    reset(autNumService);
  }

  @Test
  public void testAutNumNotFound() throws Exception {
    when(autNumService.getAutNum(anyInt())).thenReturn(null);
    mockMvc.perform(get("/autnum/123456")).andExpect(status().isNotFound());
  }

  @Test
  public void testWrongMediaType() throws Exception {
    mockMvc.perform(get("/autnum/123456")
            .accept(MediaType.TEXT_HTML))
            .andExpect(status().isNotAcceptable());
  }

  @Test
  public void testWrongTypeForParam() throws Exception {
    mockMvc.perform(get("autnum/testje")
            .accept(MediaType.parseMediaType("application/rdap+json")))
            .andExpect(status().isNotFound());
  }

  @Test
  public void testMinimal() throws Exception {
    AutNum autNum = new AutNum(null, null, null, "en", AutNum.OBJECT_CLASS_NAME, null, null, null, "AutNumHandle", 6000, 6300, "AutNumName", "AutNumType", "Belgium");
    when(autNumService.getAutNum(anyInt())).thenReturn(autNum);
    mockMvc.perform(get("/autnum/6000")
            .accept(MediaType.parseMediaType("application/rdap+json")))
            .andExpect(header().string("Content-type", "application/rdap+json;charset=UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"rdapConformance\":[\"rdap_level_0\"],\"lang\":\"en\",\"objectClassName\":\"autnum\",\"handle\":\"AutNumHandle\",\"startAutnum\":6000,\"endAutnum\":6300,\"name\":\"AutNumName\",\"type\":\"AutNumType\",\"country\":\"Belgium\"}"));
  }

  @Test
  public void testMaximal() throws Exception {
    AutNum autNum = new AutNum(someLinks(), someNotices(), someRemarks(), "en", AutNum.OBJECT_CLASS_NAME, someEvents(), someStatuses(),
            DomainName.of("whois.example.com"), "Handle", 6000, 6300, "Name", "Type", "Country");
    when(autNumService.getAutNum(anyInt())).thenReturn(autNum);
    mockMvc.perform(get("/autnum/6000")
            .accept(MediaType.parseMediaType("application/rdap+json")))
            .andExpect(header().string("Content-type", "application/rdap+json;charset=UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"rdapConformance\":[\"rdap_level_0\"]," +
                    "\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]," +
                    "\"notices\":[{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}]," +
                    "\"remarks\":[{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}]," +
                    "\"lang\":\"en\"," +
                    "\"objectClassName\":\"autnum\"," +
                    "\"events\":[{\"eventAction\":\"REGISTRATION\",\"eventActor\":\"EventActor\",\"eventDate\":\"" + createTime.toString(ISODateTimeFormat.dateTimeNoMillis()) + "\",\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"eventAction\":\"LAST_CHANGED\",\"eventActor\":\"EventActor\",\"eventDate\":\"" + lastChangedTime.toString(ISODateTimeFormat.dateTimeNoMillis()) + "\",\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}]," +
                    "\"status\":[\"active\",\"delete prohibited\",\"some specific status\"]," +
                    "\"port43\":\"whois.example.com\"," +
                    "\"handle\":\"Handle\"," +
                    "\"startAutnum\":6000," +
                    "\"endAutnum\":6300," +
                    "\"name\":\"Name\"," +
                    "\"type\":\"Type\"," +
                    "\"country\":\"Country\"}"));
  }
}