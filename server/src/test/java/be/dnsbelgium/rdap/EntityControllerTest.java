package be.dnsbelgium.rdap;

import be.dnsbelgium.core.DomainName;
import be.dnsbelgium.rdap.controller.EntityController;
import be.dnsbelgium.rdap.core.Entity;
import be.dnsbelgium.rdap.service.EntityService;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

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
		mockMvc.perform(put("/entity/123456").accept(MediaType.parseMediaType("application/rdap+json")))
				.andExpect(status().isMethodNotAllowed());
	}
  
	  @Test
	  public void testMinimalHead() throws Exception {
	    String handle = "123456";
	    Entity entity = new Entity(null, null, null, null, Entity.OBJECT_CLASS_NAME, null, null, null, handle, aContact(), null, null, null);
	    entity.addRdapConformance(Entity.DEFAULT_RDAP_CONFORMANCE);
	    when(entityService.getEntity(anyString())).thenReturn(entity);
		mockMvc.perform(head("/entity/123456").accept(MediaType.parseMediaType("application/rdap+json")))
		.andExpect(status().isOk());
	  }

	
	@Test
  public void testMinimalGet() throws Exception {
    String handle = "123456";
    Entity entity = new Entity(null, null, null, null, Entity.OBJECT_CLASS_NAME, null, null, null, handle, aContact(), null, null, null);
    entity.addRdapConformance(Entity.DEFAULT_RDAP_CONFORMANCE);
    when(entityService.getEntity(anyString())).thenReturn(entity);
    mockMvc.perform(get("/entity/" + handle)
            .accept(MediaType.parseMediaType("application/rdap+json")))
            .andExpect(header().string("Content-type", "application/rdap+json;charset=UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"rdapConformance\":[\"rdap_level_0\"],\"objectClassName\":\"entity\",\"handle\":\"123456\"," +
                    "\"vcardArray\":[\"vcard\",[[\"version\",{},\"text\",\"4.0\"],[\"n\",{},\"text\",\"Ellison\",\"Larry\",null,null,null],[\"fn\",{},\"text\",\"Larry Ellison\"],[\"adr\",{},\"text\",null,null,[\"street 1\",\"street 2\"],[\"New York\",\"Brooklyn\"],[\"New York\",\"East coast\"],\"12345\",\"United states of America\"],[\"org\",{},\"text\",\"Retirees Inc.\",\"This is an OU\",\"This is another OU\"],[\"tel\",{\"type\":\"tel\"},\"text\",\"tel:+32.123456\"],[\"tel\",{\"type\":\"tel\"},\"text\",\"tel:+32.654321\"],[\"tel\",{\"type\":\"fax\"},\"text\",\"tel:+32.987654\"],[\"email\",{},\"text\",\"larry.ellison@retirees.com\"],[\"email\",{},\"text\",\"le@former.oracle.com\"],[\"lang\",{\"pref\":\"1\"},\"text\",\"en\"],[\"lang\",{\"pref\":\"2\"},\"text\",\"de\"],[\"lang\",{\"pref\":\"3\"},\"text\",\"es\"]]]}"));
  }

  @Test
  public void testMaximal() throws Exception {
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
            somePublicIds());
    entity.addRdapConformance(Entity.DEFAULT_RDAP_CONFORMANCE);
    when(entityService.getEntity(eq(handle))).thenReturn(entity);
    mockMvc.perform(get("/entity/" + handle)
            .accept(MediaType.parseMediaType("application/rdap+json")))
            .andExpect(header().string("Content-type", "application/rdap+json;charset=UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"rdapConformance\":[\"rdap_level_0\"],\"objectClassName\":\"entity\"," +
                    "\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]," +
                    "\"notices\":[{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}]," +
                    "\"remarks\":[{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"title\":\"Title\",\"type\":\"Type\",\"description\":[\"Description part 1\",\"Description part 2\"],\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}]," +
                    "\"lang\":\"en\"," +
                    "\"events\":[{\"eventAction\":\"REGISTRATION\",\"eventActor\":\"EventActor\",\"eventDate\":\"" + createTime.toString(ISODateTimeFormat.dateTimeNoMillis()) + "\",\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"eventAction\":\"LAST_CHANGED\",\"eventActor\":\"EventActor\",\"eventDate\":\"" + lastChangedTime.toString(ISODateTimeFormat.dateTimeNoMillis()) + "\",\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}]," +
                    "\"status\":[\"active\",\"delete prohibited\",\"some specific status\"]," +
                    "\"port43\":\"whois.example.com\"," +
                    "\"handle\":\"123456\"," +
                    "\"vcardArray\":[\"vcard\",[[\"version\",{},\"text\",\"4.0\"],[\"n\",{},\"text\",\"Ellison\",\"Larry\",null,null,null],[\"fn\",{},\"text\",\"Larry Ellison\"],[\"adr\",{},\"text\",null,null,[\"street 1\",\"street 2\"],[\"New York\",\"Brooklyn\"],[\"New York\",\"East coast\"],\"12345\",\"United states of America\"],[\"org\",{},\"text\",\"Retirees Inc.\",\"This is an OU\",\"This is another OU\"],[\"tel\",{\"type\":\"tel\"},\"text\",\"tel:+32.123456\"],[\"tel\",{\"type\":\"tel\"},\"text\",\"tel:+32.654321\"],[\"tel\",{\"type\":\"fax\"},\"text\",\"tel:+32.987654\"],[\"email\",{},\"text\",\"larry.ellison@retirees.com\"],[\"email\",{},\"text\",\"le@former.oracle.com\"],[\"lang\",{\"pref\":\"1\"},\"text\",\"en\"],[\"lang\",{\"pref\":\"2\"},\"text\",\"de\"],[\"lang\",{\"pref\":\"3\"},\"text\",\"es\"]]]," +
                    "\"roles\":[\"REGISTRANT\",\"ADMINISTRATIVE\"]," +
                    "\"asEventActor\":[{\"eventAction\":\"REGISTRATION\",\"eventActor\":\"EventActor\",\"eventDate\":\"" + createTime.toString(ISODateTimeFormat.dateTimeNoMillis()) + "\",\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]},{\"eventAction\":\"LAST_CHANGED\",\"eventActor\":\"EventActor\",\"eventDate\":\"" + lastChangedTime.toString(ISODateTimeFormat.dateTimeNoMillis()) + "\",\"links\":[{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"},{\"value\":\"http://example.com/value\",\"rel\":\"rel\",\"href\":\"http://example.com/href\",\"hreflang\":[\"de\",\"en\"],\"title\":[\"Title part 1\",\"Title part 2\"],\"media\":\"Media\",\"type\":\"Type\"}]}]," +
                    "\"publicIds\":[{\"type\":\"Type\",\"identifier\":\"Identifier\"},{\"type\":\"Type\",\"identifier\":\"Identifier\"}]}"));
  }
}