package be.dnsbelgium.rdap;

import be.dnsbelgium.rdap.core.Entity;
import be.dnsbelgium.rdap.jackson.CustomObjectMapper;
import be.dnsbelgium.rdap.service.EntityService;
import be.dnsbelgium.vcard.Contact;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.Resource;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = EntityControllerTest.Config.class)
public class EntityControllerTest {

  private final static int REDIRECT_THRESHOLD = 3;
  private final static String REDIRECT_URL = "https://rdap.org";
  private MockMvc mockMvc;

  @Configuration
  static class Config extends WebMvcConfigurationSupport {
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
      converters.add(converter());
    }

    @Bean
    MappingJacksonHttpMessageConverter converter() {
      MappingJacksonHttpMessageConverter converter = new MappingJacksonHttpMessageConverter();
      converter.setObjectMapper(new CustomObjectMapper());
      return converter;
    }

    @Bean
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
      RequestMappingHandlerMapping handlerMapping = super.requestMappingHandlerMapping();
      handlerMapping.setUseSuffixPatternMatch(false);
      handlerMapping.setUseTrailingSlashMatch(false);
      return handlerMapping;
    }

    @Bean
    public EntityService entityService() {
      return mock(EntityService.class);
    }

    @Bean
    public EntityController entityController() {
      return new EntityController(REDIRECT_URL, REDIRECT_THRESHOLD);
    }
  }

  @Autowired
  EntityController entityController;

  @Autowired
  EntityService entityService;

  @Resource
  private WebApplicationContext webApplicationContext;

  @Before
  public void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

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
  public void testMinimal() throws Exception {
    String handle = "123456";
    Contact contact = new Contact.Builder()
            .setFormattedName("Larry Ellison")
            .addEmailAddress("larry.ellison@retirees.com")
            .addStreet("First street")
            .addStreet("Second street")
            .setPostalCode("10001")
            .setLocality("New York")
            .setCountry("USA")
            .build();
    Entity entity = new Entity(null, null, null, null, Entity.OBJECT_CLASS_NAME, null, null, null, handle, contact, null, null, null);
    when(entityService.getEntity(anyString())).thenReturn(entity);
    mockMvc.perform(get("/entity/" + handle)
            .accept(MediaType.parseMediaType("application/rdap+json")))
            .andExpect(header().string("Content-type", "application/rdap+json;charset=UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"rdapConformance\":[\"rdap_level_0\"],\"objectClassName\":\"entity\",\"handle\":\"123456\"," +
                    "\"vCard\":[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Larry Ellison\"],[\"adr\",{},\"text\",null,null,[\"First street\",\"Second street\"],\"New York\",null,\"10001\",\"USA\"],[\"email\",{},\"text\",\"larry.ellison@retirees.com\"]]}"));
  }
}