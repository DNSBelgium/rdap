package be.dnsbelgium.rdap.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;

import static org.junit.Assert.assertEquals;

public interface TestObjectMapper {

  CustomObjectMapper OBJECT_MAPPER = (CustomObjectMapper) new CustomObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

  static void assertJsonMapping(Object object, String expectedJson) throws JsonProcessingException {
    assertEquals(expectedJson, OBJECT_MAPPER.writeValueAsString(object));
  }

}
