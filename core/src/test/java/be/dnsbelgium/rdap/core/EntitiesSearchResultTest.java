package be.dnsbelgium.rdap.core;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import java.util.List;

import static be.dnsbelgium.rdap.core.Common.DEFAULT_RDAP_CONFORMANCE;
import static be.dnsbelgium.rdap.core.Common.REDACTED_EXTENSION_CONFORMANCE;
import static be.dnsbelgium.rdap.jackson.TestObjectMapper.assertJsonMapping;

public class EntitiesSearchResultTest {

  private final Entity unredactedEntity1 = new Entity(null, null, null, null,
      Entity.OBJECT_CLASS_NAME, null, null, null, "abc-123", null, null,
      null, null, null);
  private final Entity unredactedEntity2 = new Entity(null, null, null, null,
      Entity.OBJECT_CLASS_NAME, null, null, null, "xyz-789", null, null,
      null, null, null);
  private final Redacted redactedId = new Redacted(Redacted.Name.fromType(Redacted.Name.Type.Default.REGISTRY_REGISTRANT_ID));
  private final Redacted redactedName = new Redacted(Redacted.Name.fromType(Redacted.Name.Type.Default.REGISTRANT_NAME));
  private final Entity redactedEntity = new Entity(null, null, null, null,
      Entity.OBJECT_CLASS_NAME, null, null, null, null, null, null,
      null, null, null, List.of(redactedId, redactedName));

  @Test
  public void testUnredactedEntitySearchResultSample() throws JsonProcessingException {
    String sample = """
        {
          "rdapConformance" : [ "rdap_level_0" ],
          "entitySearchResults" : [ {
            "objectClassName" : "entity",
            "handle" : "abc-123"
          }, {
            "objectClassName" : "entity",
            "handle" : "xyz-789"
          } ]
        }""";

    EntitiesSearchResult entitySearchResult = new EntitiesSearchResult(List.of(unredactedEntity1, unredactedEntity2));
    entitySearchResult.addRdapConformance(DEFAULT_RDAP_CONFORMANCE);

    assertJsonMapping(entitySearchResult, sample);
  }

  @Test
  public void testRedactedEntitySearchResultSample() throws JsonProcessingException {
    String sample = """
        {
          "rdapConformance" : [ "rdap_level_0", "redacted" ],
          "entitySearchResults" : [ {
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
          }, {
            "objectClassName" : "entity",
            "handle" : "xyz-789"
          }, {
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
          } ]
        }""";

    EntitiesSearchResult entitySearchResult = new EntitiesSearchResult(List.of(redactedEntity, unredactedEntity2, redactedEntity));
    entitySearchResult.addRdapConformance(DEFAULT_RDAP_CONFORMANCE);
    entitySearchResult.addRdapConformance(REDACTED_EXTENSION_CONFORMANCE);

    assertJsonMapping(entitySearchResult, sample);
  }

}