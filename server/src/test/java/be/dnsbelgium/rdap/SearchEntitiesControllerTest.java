package be.dnsbelgium.rdap;

import be.dnsbelgium.rdap.controller.SearchEntitiesController;
import be.dnsbelgium.rdap.core.EntitiesSearchResult;
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

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = SearchEntitiesControllerTest.Config.class)
public class SearchEntitiesControllerTest extends AbstractControllerTest {

  @Configuration
  static class Config extends AbstractControllerTest.Config {

    @Bean
    public EntityService entityService() {
      return mock(EntityService.class);
    }

    @Bean
    public SearchEntitiesController searchEntitiesController() {
      return new SearchEntitiesController(entityService());
    }
  }

  @Autowired
  SearchEntitiesController searchEntitiesController;
  @Autowired
  EntityService entityService;

  @After
  public void resetMock() {
    reset(entityService);
  }

  @Test
  public void testSearchByFnNoResult() throws Exception {
    EntitiesSearchResult entitiesSearchResult = new EntitiesSearchResult(new ArrayList<Entity>());
    entitiesSearchResult.addRdapConformance(Entity.DEFAULT_RDAP_CONFORMANCE);
    when(entityService.searchByFn(anyString())).thenReturn(entitiesSearchResult);
    mockMvc.perform(get("/entities?fn=formattedName").accept(MediaType.parseMediaType("application/rdap+json")))
        .andExpect(status().isNotFound())
        .andExpect(header().string("Content-type", "application/rdap+json;charset=UTF-8"));
  }

  @Test
  public void testSearchByHandleNoResult() throws Exception {
    EntitiesSearchResult entitiesSearchResult = new EntitiesSearchResult(new ArrayList<Entity>());
    entitiesSearchResult.addRdapConformance(Entity.DEFAULT_RDAP_CONFORMANCE);
    when(entityService.searchByHandle(anyString())).thenReturn(entitiesSearchResult);
    mockMvc.perform(get("/entities?handle=Handle").accept(MediaType.parseMediaType("application/rdap+json")))
        .andExpect(status().isNotFound())
        .andExpect(header().string("Content-type", "application/rdap+json;charset=UTF-8"));
  }

  @Test
  public void testSearchByHandleNoResultWithAcceptHeader() throws Exception {
    performSearchEntityTest("text/html");
  }

  @Test
  public void testSearchByHandleNoResultWithJson() throws Exception {
    performSearchEntityTest("application/json");
  }

  @Test
  public void testSearchByHandleNoResultWithRdapJson() throws Exception {
    performSearchEntityTest("application/rdap+json");
  }

  @Test
  public void testSearchByHandleNoResultWithOtherHeader() throws Exception {
    performSearchEntityTest("text/html");
  }

  @Test
  public void testMethodNotAllowed() throws Exception {
    mockMvc.perform(put("/entities?handle=Handle").accept(MediaType.parseMediaType("application/rdap+json")))
        .andExpect(status().isMethodNotAllowed());
  }

  public void performSearchEntityTest(String acceptHeader) throws Exception {
    EntitiesSearchResult entitiesSearchResult = new EntitiesSearchResult(new ArrayList<Entity>());
    entitiesSearchResult.addRdapConformance(Entity.DEFAULT_RDAP_CONFORMANCE);
    when(entityService.searchByHandle(anyString())).thenReturn(entitiesSearchResult);
    mockMvc.perform(get("/entities?handle=Handle")
            .accept(MediaType.parseMediaType(acceptHeader)))
            .andExpect(content().contentTypeCompatibleWith(MediaType.valueOf("application/rdap+json")))
            .andExpect(status().isNotFound())
            .andDo(print());
  }
}