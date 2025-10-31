package be.dnsbelgium.rdap.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import java.util.List;

import static be.dnsbelgium.rdap.core.Common.DEFAULT_RDAP_CONFORMANCE;
import static be.dnsbelgium.rdap.core.Common.REDACTED_EXTENSION_CONFORMANCE;
import static be.dnsbelgium.rdap.jackson.TestObjectMapper.assertJsonMapping;

public class EntityTest {

  @Test
  public void testUnredactedEntitySample() throws JsonProcessingException {
    String sample = """
        {
          "rdapConformance" : [ "rdap_level_0" ],
          "objectClassName" : "entity",
          "handle" : "abc-123"
        }""";

    Entity entity = new Entity(null, null, null, null, Entity.OBJECT_CLASS_NAME, null,
        null, null, "abc-123", null, null, null, null, null);
    entity.addRdapConformance(DEFAULT_RDAP_CONFORMANCE);

    assertJsonMapping(entity, sample);
  }

  @Test
  public void testRedactedEntitySample() throws JsonProcessingException {
    String sample = """
        {
          "rdapConformance" : [ "rdap_level_0", "redacted" ],
          "objectClassName" : "entity",
          "redacted" : [ {
            "name" : {
              "type" : "Registry Registrant ID"
            }
          }, {
            "name" : {
              "type" : "Registrant Name"
            }
          } ]
        }""";

    Redacted redactedId = new Redacted(Redacted.Name.fromType(Redacted.Name.Type.Default.REGISTRY_REGISTRANT_ID));
    Redacted redactedName = new Redacted(Redacted.Name.fromType(Redacted.Name.Type.Default.REGISTRANT_NAME));
    Entity entity = new Entity(null, null, null, null, Entity.OBJECT_CLASS_NAME, null,
        null, null, null, null, null, null, null,
        null, List.of(redactedId, redactedName));
    entity.addRdapConformance(DEFAULT_RDAP_CONFORMANCE);
    entity.addRdapConformance(REDACTED_EXTENSION_CONFORMANCE);

    assertJsonMapping(entity, sample);
  }

}