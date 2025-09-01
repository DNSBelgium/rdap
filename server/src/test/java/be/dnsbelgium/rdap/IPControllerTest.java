package be.dnsbelgium.rdap;

import be.dnsbelgium.core.CIDR;
import be.dnsbelgium.rdap.controller.IPController;
import be.dnsbelgium.rdap.core.IPNetwork;
import be.dnsbelgium.rdap.service.IPService;
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

import java.net.InetAddress;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = IPControllerTest.Config.class)
public class IPControllerTest extends AbstractControllerTest {

  @Configuration
  static class Config extends AbstractControllerTest.Config {

    @Bean
    public IPService ipService() {
      return mock(IPService.class);
    }

    @Bean
    public IPController ipController() {
      return new IPController(ipService());
    }
  }

  @Autowired
  IPController ipController;

  @Autowired
  IPService ipService;

  @After
  public void resetMock() {
    reset(ipService);
  }

  @Test
  public void testIPNotFound() throws Exception {
    when(ipService.getIPNetwork(any(CIDR.class))).thenReturn(null);
    mockMvc.perform(get("/ip/193.12.32.98")).andExpect(status().isNotFound());
  }

  @Test
  public void testCIDRNotFound() throws Exception {
    when(ipService.getIPNetwork(any(CIDR.class))).thenReturn(null);
    mockMvc.perform(get("/ip/193.12.32.98/24")).andExpect(status().isNotFound());
  }

  @Test
  public void testIPMinimalJson() throws Exception {
    performIpTest("application/json");
  }

  @Test
  public void testIPMinimalRdapJson() throws Exception {
    performIpTest("application/rdap+json");
  }

  @Test
  public void testIPMinimalOtherHeader() throws Exception {
    performIpTest("text/html;charset=UTF-8");
  }

  @Test
  public void testCIDRMinimalJson() throws Exception {
    performCIDRMTest("application/json");
  }

  @Test
  public void testCIDRMinimalRdapJson() throws Exception {
    performCIDRMTest("application/rdap+json");
  }

  @Test
  public void testCIDRMinimalOtherHeader() throws Exception {
    performCIDRMTest("text/html;charset=UTF-8");
  }

  public void performIpTest(String acceptHeader) throws Exception {
    InetAddress inetAddress = InetAddress.getByName("193.12.32.98");
    IPNetwork ipNetwork = new IPNetwork(null, null, null, "en", IPNetwork.OBJECT_CLASS_NAME, null, null, null, null, inetAddress, inetAddress, null, "IPv4", "USA", null, null);
    ipNetwork.addRdapConformance(IPNetwork.DEFAULT_RDAP_CONFORMANCE);
    when(ipService.getIPNetwork(any(CIDR.class))).thenReturn(ipNetwork);
    mockMvc.perform(get("/ip/193.12.32.98")
                    .accept(MediaType.parseMediaType(acceptHeader)))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.valueOf("application/rdap+json")))
            .andExpect(content().string("{\"rdapConformance\":[\"rdap_level_0\"],\"objectClassName\":\"ip network\",\"lang\":\"en\"," +
                    "\"startAddress\":\"193.12.32.98\",\"endAddress\":\"193.12.32.98\",\"type\":\"IPv4\",\"country\":\"USA\"}"));
  }

  public void performCIDRMTest(String acceptHeader) throws Exception {
    InetAddress startAddress = InetAddress.getByName("FE80:0000:0000:0000:0202:B3FF:FE1E:0000");
    InetAddress endAddress = InetAddress.getByName("FE80:0000:0000:0000:0202:B3FF:FE1E:FFFF");
    IPNetwork ipNetwork = new IPNetwork(null, null, null, "en", IPNetwork.OBJECT_CLASS_NAME, null, null, null, null, startAddress, endAddress, null, "IPv6", "USA", null, null);
    ipNetwork.addRdapConformance(IPNetwork.DEFAULT_RDAP_CONFORMANCE);
    when(ipService.getIPNetwork(any(CIDR.class))).thenReturn(ipNetwork);
    mockMvc.perform(get("/ip/FE80:0000:0000:0000:0202:B3FF:FE1E:1111/112")
                    .accept(MediaType.parseMediaType(acceptHeader)))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.valueOf("application/rdap+json")))
            .andExpect(content().string("{\"rdapConformance\":[\"rdap_level_0\"],\"objectClassName\":\"ip network\",\"lang\":\"en\"," +
                    "\"startAddress\":\"fe80:0:0:0:202:b3ff:fe1e:0\",\"endAddress\":\"fe80:0:0:0:202:b3ff:fe1e:ffff\",\"type\":\"IPv6\",\"country\":\"USA\"}"));
  }
}