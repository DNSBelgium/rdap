package be.dnsbelgium.rdap;

import be.dnsbelgium.core.DomainName;
import be.dnsbelgium.rdap.controller.EntityController;
import be.dnsbelgium.rdap.core.Entity;
import be.dnsbelgium.rdap.core.RDAPError;
import be.dnsbelgium.rdap.service.EntityService;
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

import static be.dnsbelgium.rdap.RdapMediaType.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = EntityControllerTest.Config.class)
public class EntityControllerTest extends AbstractControllerTest {


  @Configuration
  static class Config extends AbstractControllerTest.Config {
    @Bean
    public EntityService entityService() {
      return mock(EntityService.class);
    }

    @Bean
    public EntityController entityController() {
      return new EntityController(entityService());
    }
  }

  @Autowired
  EntityController entityController;
  @Autowired
  EntityService entityService;

  @After
  public void resetMock() {
    reset(entityService);
  }

  @Test
  public void testNotFound() throws Exception {
    when(entityService.getEntity(anyString())).thenReturn(null);
    mockMvc.perform(get("/entity/123456")).andExpect(status().isNotFound());
  }

  @Test
  public void testInternalServerError() throws Exception {
    when(entityService.getEntity(anyString())).thenThrow(new IllegalArgumentException("Some uncaught exception"));
    mockMvc.perform(get("/entity/123456")).andExpect(status().isInternalServerError());
  }

  @Test
  public void testMethodNotAllowed() throws Exception {
    mockMvc.perform(put("/entity/123456").accept(APPLICATION_RDAP_JSON))
        .andExpect(status().isMethodNotAllowed());
  }

  @Test
  public void testAcceptRdapJsonMinimal() throws Exception {
    performGetEntityTest(APPLICATION_RDAP_JSON);
  }

  @Test
  public void testAcceptRdapJsonUtf8Minimal() throws Exception {
    performGetEntityTest(APPLICATION_RDAP_JSON_UTF8);
  }

  @Test
  public void testAcceptJsonMinimal() throws Exception {
    performGetEntityTest(APPLICATION_JSON);
  }

  @Test
  public void testAcceptJsonUtf8Minimal() throws Exception {
    performGetEntityTest(APPLICATION_JSON_UTF8);
  }

  @Test
  public void testAcceptOtherAcceptHeadersMinimal() throws Exception {
    performGetEntityTest(TEXT_HTML);
  }

  @Test
  public void testMinimalGet() throws Exception {
    String expectedJson = createExpectedJson("EntityControllerTest.minimalGet.json");
    String handle = "123456";
    initMinimalHead();
    mockMvc.perform(get("/entity/" + handle).accept(APPLICATION_RDAP_JSON))
        .andExpect(header().string("Content-type", APPLICATION_RDAP_JSON_UTF8_VALUE))
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson));
  }

  @Test
  public void testMaximal() throws Exception {
    String expectedJson = createExpectedJson("EntityControllerTest.maximalGet.json");
    String handle = "123456";
    Entity entity = new Entity(
        someLinks(),
        someNotices(),
        someRemarks(),
        "en",
        Entity.OBJECT_CLASS_NAME,
        someEvents(),
        someStatuses(),
        DomainName.of("whois.example.com"),
        handle,
        aContact(),
        someRoles(),
        someEvents(),
        somePublicIds(),
        someEntities());
    entity.addRdapConformance(Entity.DEFAULT_RDAP_CONFORMANCE);
    when(entityService.getEntity(eq(handle))).thenReturn(entity);
    mockMvc.perform(get("/entity/" + handle)
        .accept(APPLICATION_RDAP_JSON))
        .andExpect(content().contentTypeCompatibleWith(APPLICATION_RDAP_JSON))
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson));
  }

  public void performGetEntityTest(MediaType acceptHeader) throws Exception {
    initMinimalHead();
    mockMvc.perform(get("/entity/123456").accept(acceptHeader))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(APPLICATION_RDAP_JSON));
  }

  public void initMinimalHead() throws RDAPError {
    String handle = "123456";
    Entity entity = new Entity(null, null, null, null, Entity.OBJECT_CLASS_NAME, null, null, null, handle, aContact(), null, null, null, null);
    entity.addRdapConformance(Entity.DEFAULT_RDAP_CONFORMANCE);
    when(entityService.getEntity(anyString())).thenReturn(entity);
  }
}