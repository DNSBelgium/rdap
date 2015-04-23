package be.dnsbelgium.rdap;

import be.dnsbelgium.rdap.service.AutNumService;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
}