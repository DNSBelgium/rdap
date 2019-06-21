package be.dnsbelgium.rdap;

import be.dnsbelgium.rdap.controller.HelpController;
import be.dnsbelgium.rdap.core.Domain;
import be.dnsbelgium.rdap.core.Help;
import be.dnsbelgium.rdap.service.HelpService;
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = HelpControllerTest.Config.class)
public class HelpControllerTest extends AbstractControllerTest {

  @Configuration
  static class Config extends AbstractControllerTest.Config {

    @Bean
    public HelpService helpService() {
      return mock(HelpService.class);
    }

    @Bean
    public HelpController helpController() {
      return new HelpController(helpService());
    }
  }

  @Autowired
  HelpController helpController;

  @Autowired
  HelpService helpService;

  @After
  public void resetMock() {
    reset(helpService);
  }

  @Test
  public void testException() throws Exception {
    when(helpService.getHelp()).thenThrow(new IllegalArgumentException());
    mockMvc.perform(get("/help")).andExpect(status().isInternalServerError());
  }

  @Test
  public void testHelpSuccess() throws Exception {
    String expectedJson = createExpectedJson("HelpControllerTest.help.json");

    Help help = new Help(someNotices());
    help.addRdapConformance(Domain.DEFAULT_RDAP_CONFORMANCE);
    when(helpService.getHelp()).thenReturn(help);
    mockMvc.perform(get("/help")
            .accept(MediaType.parseMediaType("application/rdap+json")))
            .andExpect(header().string("Content-type", "application/rdap+json;charset=UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson, true));

  }

  @Test
  public void testHelpWithLinksSuccess() throws Exception {
    String expectedJson = createExpectedJson("HelpControllerTest.helpWithLinks.json");
    Help help = new Help(someNotices());
    help.addRdapConformance(Domain.DEFAULT_RDAP_CONFORMANCE);
    help.links = someLinks();
    when(helpService.getHelp()).thenReturn(help);
    mockMvc.perform(get("/help")
        .accept(MediaType.parseMediaType("application/rdap+json")))
        .andExpect(header().string("Content-type", "application/rdap+json;charset=UTF-8"))
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson, true));

  }
}