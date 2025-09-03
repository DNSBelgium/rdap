package be.dnsbelgium.rdap;

import be.dnsbelgium.rdap.controller.SearchEntitiesController;
import be.dnsbelgium.rdap.core.EntitiesSearchResult;
import be.dnsbelgium.rdap.core.Entity;
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

import static be.dnsbelgium.rdap.RdapMediaType.APPLICATION_RDAP_JSON;
import static be.dnsbelgium.rdap.RdapMediaType.APPLICATION_RDAP_JSON_UTF8_VALUE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.*;
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
    EntitiesSearchResult entitiesSearchResult = new EntitiesSearchResult(new ArrayList<>());
    entitiesSearchResult.addRdapConformance(Entity.DEFAULT_RDAP_CONFORMANCE);
    when(entityService.searchByFn(anyString())).thenReturn(entitiesSearchResult);
    mockMvc.perform(get("/entities?fn=formattedName").accept(APPLICATION_RDAP_JSON))
        .andExpect(status().isNotFound())
        .andExpect(header().string("Content-type", APPLICATION_RDAP_JSON_UTF8_VALUE));
  }

  @Test
  public void testSearchByHandleNoResult() throws Exception {
    EntitiesSearchResult entitiesSearchResult = new EntitiesSearchResult(new ArrayList<>());
    entitiesSearchResult.addRdapConformance(Entity.DEFAULT_RDAP_CONFORMANCE);
    when(entityService.searchByHandle(anyString())).thenReturn(entitiesSearchResult);
    mockMvc.perform(get("/entities?handle=Handle").accept(APPLICATION_RDAP_JSON))
        .andExpect(status().isNotFound())
        .andExpect(header().string("Content-type", APPLICATION_RDAP_JSON_UTF8_VALUE));
  }

  @Test
  public void testSearchByHandleNoResultWithJson() throws Exception {
    performSearchEntityTest(APPLICATION_JSON);
  }

  @Test
  public void testSearchByHandleNoResultWithRdapJson() throws Exception {
    performSearchEntityTest(APPLICATION_RDAP_JSON);
  }

  @Test
  public void testSearchByHandleNoResultWithOtherAcceptHeaders() throws Exception {
    performSearchEntityTest(TEXT_HTML);
  }

  @Test
  public void testMethodNotAllowed() throws Exception {
    mockMvc.perform(put("/entities?handle=Handle").accept(APPLICATION_RDAP_JSON))
        .andExpect(status().isMethodNotAllowed());
  }

  public void performSearchEntityTest(MediaType acceptHeader) throws Exception {
    EntitiesSearchResult entitiesSearchResult = new EntitiesSearchResult(new ArrayList<>());
    entitiesSearchResult.addRdapConformance(Entity.DEFAULT_RDAP_CONFORMANCE);
    when(entityService.searchByHandle(anyString())).thenReturn(entitiesSearchResult);
    mockMvc.perform(get("/entities?handle=Handle")
            .accept(acceptHeader))
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_RDAP_JSON))
            .andExpect(status().isNotFound())
            .andDo(print());
  }
}