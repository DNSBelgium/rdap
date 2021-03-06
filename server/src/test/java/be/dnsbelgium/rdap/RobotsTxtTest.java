package be.dnsbelgium.rdap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.hamcrest.core.StringContains.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = AbstractControllerTest.Config.class)
public class RobotsTxtTest extends AbstractControllerTest {
  @Test
  public void requestForRobotsTxtShouldReturnSomething() throws Exception{
    this.mockMvc.perform(get("/robots.txt"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Disallow: /")))
        .andExpect(content().string(containsString("User-agent: *")));
  }
}
