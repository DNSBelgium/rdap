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

import static be.dnsbelgium.rdap.RdapMediaType.APPLICATION_RDAP_JSON;
import static be.dnsbelgium.rdap.RdapMediaType.APPLICATION_RDAP_JSON_UTF8;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
  public void testHelpSuccessRdapJson() throws Exception {
    performHelpTest(APPLICATION_RDAP_JSON);
  }

  @Test
  public void testHelpSuccessRdapJsonUtf8() throws Exception {
    performHelpTest(APPLICATION_RDAP_JSON_UTF8);
  }

  @Test
  public void testHelpSuccessJson() throws Exception {
    performHelpTest(APPLICATION_JSON);
  }

  @Test
  public void testHelpSuccessJsonUtf8() throws Exception {
    performHelpTest(APPLICATION_JSON_UTF8);
  }

  @Test
  public void testHelpSuccessOtherAcceptHeaders() throws Exception {
    performHelpTest(TEXT_HTML);
  }

  @Test
  public void testHelpWithLinksSuccess() throws Exception {
    String expectedJson = createExpectedJson("HelpControllerTest.helpWithLinks.json");
    Help help = new Help(someNotices());
    help.addRdapConformance(Domain.DEFAULT_RDAP_CONFORMANCE);
    help.links = someLinks();
    when(helpService.getHelp()).thenReturn(help);
    mockMvc.perform(get("/help").accept(APPLICATION_RDAP_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(APPLICATION_RDAP_JSON))
        .andExpect(content().json(expectedJson, true));

  }

  public void performHelpTest(MediaType acceptHeader) throws Exception {
    String expectedJson = createExpectedJson("HelpControllerTest.help.json");
    initHelp();
    mockMvc.perform(get("/help").accept(acceptHeader))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(APPLICATION_RDAP_JSON))
        .andExpect(content().json(expectedJson, true));
  }

  public void initHelp() throws Exception {
    Help help = new Help(someNotices());
    help.addRdapConformance(Domain.DEFAULT_RDAP_CONFORMANCE);
    when(helpService.getHelp()).thenReturn(help);
  }
}