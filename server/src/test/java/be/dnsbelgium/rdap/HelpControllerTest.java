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
    performHelpTest("application/rdap+json");
  }

  @Test
  public void testHelpSuccessRdapJsonCharset() throws Exception {
    performHelpTest("application/rdap+json;charset=UTF-8");
  }

  @Test
  public void testHelpSuccessJson() throws Exception {
    performHelpTest("application/json");
  }

  @Test
  public void testHelpSuccessJsonCharset() throws Exception {
    performHelpTest("application/json;charset=UTF-8");
  }

  @Test
  public void testHelpSuccessOtherHeaders() throws Exception {
    performHelpTest("text/html");
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
        .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("application/rdap+json"))
            .andExpect(content().json(expectedJson, true));

  }

  public void performHelpTest(String acceptHeader) throws Exception {
    String expectedJson = createExpectedJson("HelpControllerTest.help.json");
    initHelp();
    mockMvc.perform(get("/help")
            .accept(MediaType.parseMediaType(acceptHeader))) //parseMediaType parses the content type into a MediaType object and compares it semantically. It handles variations in formatting (e.g., extra spaces, case differences).
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.valueOf("application/rdap+json"))) //contentTypeCompatibleWith checks whether the response content type is compatible with the expected type, ignoring charset differences. MediaType.valueOf for more flexibility in case the charset might change or be omitted in some environments.
            .andExpect(content().json(expectedJson, true));
  }

  public void initHelp() throws Exception {
    Help help = new Help(someNotices());
    help.addRdapConformance(Domain.DEFAULT_RDAP_CONFORMANCE);
    when(helpService.getHelp()).thenReturn(help);
  }
}