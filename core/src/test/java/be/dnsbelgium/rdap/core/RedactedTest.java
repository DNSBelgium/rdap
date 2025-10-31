package be.dnsbelgium.rdap.core;

import be.dnsbelgium.rdap.core.Redacted.Method;
import be.dnsbelgium.rdap.core.Redacted.Name;
import be.dnsbelgium.rdap.core.Redacted.PathLang;
import be.dnsbelgium.rdap.core.Redacted.Reason;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import static be.dnsbelgium.rdap.core.Redacted.Method.*;
import static be.dnsbelgium.rdap.core.Redacted.Name.Type.Default.*;
import static be.dnsbelgium.rdap.core.Redacted.PathLang.JSON_PATH;
import static be.dnsbelgium.rdap.jackson.TestObjectMapper.assertJsonMapping;
import static org.junit.Assert.assertEquals;

public class RedactedTest {

  @Test
  public void testDefaultNameTypes() {
    assertNameTypeValue(REGISTRY_DOMAIN_ID, "Registry Domain ID");
    assertNameTypeValue(REGISTRY_REGISTRANT_ID, "Registry Registrant ID");
    assertNameTypeValue(REGISTRANT_NAME, "Registrant Name");
    assertNameTypeValue(REGISTRANT_ORGANIZATION, "Registrant Organization");
    assertNameTypeValue(REGISTRANT_STREET, "Registrant Street");
    assertNameTypeValue(REGISTRANT_CITY, "Registrant City");
    assertNameTypeValue(REGISTRANT_POSTAL_CODE, "Registrant Postal Code");
    assertNameTypeValue(REGISTRANT_PHONE, "Registrant Phone");
    assertNameTypeValue(REGISTRANT_PHONE_EXT, "Registrant Phone Ext");
    assertNameTypeValue(REGISTRANT_FAX, "Registrant Fax");
    assertNameTypeValue(REGISTRANT_FAX_EXT, "Registrant Fax Ext");
    assertNameTypeValue(REGISTRANT_EMAIL, "Registrant Email");
    assertNameTypeValue(REGISTRY_TECH_ID, "Registry Tech ID");
    assertNameTypeValue(TECH_NAME, "Tech Name");
    assertNameTypeValue(TECH_PHONE, "Tech Phone");
    assertNameTypeValue(TECH_PHONE_EXT, "Tech Phone Ext");
    assertNameTypeValue(TECH_EMAIL, "Tech Email");
  }

  @Test
  public void testDefaultPathLang() {
    assertEquals("jsonpath", JSON_PATH.getValue());
  }

  @Test
  public void testMethods() {
    assertMethodValue(REMOVAL, "removal");
    assertMethodValue(EMPTY_VALUE, "emptyValue");
    assertMethodValue(PARTIAL_VALUE, "partialValue");
    assertMethodValue(REPLACEMENT_VALUE, "replacementValue");
  }

  @Test
  public void testBuilder() {
    Name name = Name.fromType(Name.Type.of("Some Name"));
    String prePath = "$.some.path";
    String postPath = "$.some.other.path";
    String replacementPath = "$.yet.other.path";
    PathLang pathLang = PathLang.of("somepathlang");
    Method method = REMOVAL;
    Reason reason = Reason.fromType(Reason.Type.of("Some Reason"));

    Redacted redacted = Redacted.Builder
        .withName(name)
        .withPrePath(prePath)
        .withPostPath(postPath)
        .withReplacementPath(replacementPath)
        .withPathLang(pathLang)
        .withMethod(method)
        .withReason(reason)
        .build();

    assertEquals(name, redacted.getName());
    assertEquals(prePath, redacted.getPrePath());
    assertEquals(postPath, redacted.getPostPath());
    assertEquals(replacementPath, redacted.getReplacementPath());
    assertEquals(pathLang, redacted.getPathLang());
    assertEquals(method, redacted.getMethod());
    assertEquals(reason, redacted.getReason());
  }

  @Test
  public void testRedactedByRemovalSample() throws JsonProcessingException {
    String sample = """
        {
          "name" : {
            "description" : "Administrative Contact"
          },
          "prePath" : "$.entities[?(@.roles[0]=='administrative')]",
          "method" : "removal"
        }""";

    Redacted redactedByRemoval = Redacted.Builder
        .withName(Name.fromDescription("Administrative Contact"))
        .withPrePath("$.entities[?(@.roles[0]=='administrative')]")
        .withMethod(REMOVAL)
        .build();

    assertJsonMapping(redactedByRemoval, sample);
  }

  @Test
  public void testRedactedByEmptyValueSample() throws JsonProcessingException {
    String sample = """
        {
          "name" : {
            "description" : "Registrant Name"
          },
          "postPath" : "$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]",
          "pathLang" : "jsonpath",
          "method" : "emptyValue",
          "reason" : {
            "description" : "Server policy"
          }
        }""";

    Redacted redactedByEmptyValue = Redacted.Builder
        .withName(Name.fromDescription(Name.Type.Default.REGISTRANT_NAME.getValue()))
        .withPostPath("$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]")
        .withPathLang(JSON_PATH)
        .withMethod(EMPTY_VALUE)
        .withReason(Reason.fromDescription("Server policy"))
        .build();

    assertJsonMapping(redactedByEmptyValue, sample);
  }

  @Test
  public void testRedactedByPartialValueSample() throws JsonProcessingException {
    String sample = """
        {
          "name" : {
            "description" : "Home Address Label"
          },
          "postPath" : "$.vcardArray[1][?(@[0]=='adr')][1].label",
          "pathLang" : "jsonpath",
          "method" : "partialValue",
          "reason" : {
            "description" : "Server policy"
          }
        }""";

    Redacted redactedByPartialValue = Redacted.Builder
        .withName(Name.fromDescription("Home Address Label"))
        .withPostPath("$.vcardArray[1][?(@[0]=='adr')][1].label")
        .withPathLang(JSON_PATH)
        .withMethod(PARTIAL_VALUE)
        .withReason(Reason.fromDescription("Server policy"))
        .build();

    assertJsonMapping(redactedByPartialValue, sample);
  }

  @Test
  public void testRedactedByReplacementValueSample_anonymizedValue() throws JsonProcessingException {
    String sample = """
        {
          "name" : {
            "description" : "Registrant Email"
          },
          "postPath" : "$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='email')][3]",
          "pathLang" : "jsonpath",
          "method" : "replacementValue"
        }""";

    Redacted redactedByReplacementValue = Redacted.Builder
        .withName(Name.fromDescription(Name.Type.Default.REGISTRANT_EMAIL.getValue()))
        .withPostPath("$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='email')][3]")
        .withPathLang(JSON_PATH)
        .withMethod(REPLACEMENT_VALUE)
        .build();

    assertJsonMapping(redactedByReplacementValue, sample);
  }

  @Test
  public void testRedactedByReplacementValueSample_alternativeValue() throws JsonProcessingException {
    String sample = """
        {
          "name" : {
            "description" : "Registrant Email"
          },
          "prePath" : "$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='email')]",
          "replacementPath" : "$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='contact-uri')]",
          "pathLang" : "jsonpath",
          "method" : "replacementValue"
        }""";

    Redacted redactedByReplacementValue = Redacted.Builder
        .withName(Name.fromDescription(Name.Type.Default.REGISTRANT_EMAIL.getValue()))
        .withPrePath("$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='email')]")
        .withReplacementPath("$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='contact-uri')]")
        .withPathLang(JSON_PATH)
        .withMethod(REPLACEMENT_VALUE)
        .build();

    assertJsonMapping(redactedByReplacementValue, sample);
  }

  private static void assertNameTypeValue(Name.Type type, String expectedValue) {
    assertEquals(expectedValue, type.getValue());
  }

  private static void assertMethodValue(Method method, String expectedValue) {
    assertEquals(expectedValue, method.getValue());
  }
}
