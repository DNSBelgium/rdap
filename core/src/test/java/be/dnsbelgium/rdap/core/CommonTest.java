package be.dnsbelgium.rdap.core;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class CommonTest {

  Redacted redacted = new Redacted(Redacted.Name.fromType(Redacted.Name.Type.Default.REGISTRY_DOMAIN_ID));
  Redacted anotherRedacted = new Redacted(Redacted.Name.fromType(Redacted.Name.Type.Default.REGISTRY_REGISTRANT_ID));

  @Test
  public void testGetRedacted_implicitNull() {
    Common common = new Common(null, null, null, null, "test", null, null, null);
    assertNull(common.getRedacted());
  }

  @Test
  public void testGetRedacted_explicitNull() {
    Common common = new Common(null, null, null, null, "test", null, null, null, null);
    assertNull(common.getRedacted());
  }

  @Test
  public void testGetRedacted_emptyList() {
    Common common = new Common(null, null, null, null, "test", null, null, null, List.of());
    assertNotNull(common.getRedacted());
    assertTrue(common.getRedacted().isEmpty());
  }

  @Test
  public void testGetRedacted_immutableList() {
    Common common = new Common(null, null, null, null, "test", null, null, null, List.of());
    assertNotNull(common.getRedacted());
    try {
      common.getRedacted().add(redacted);
      fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      // expected
    }
  }

  @Test
  public void testGetRedacted_nonEmptyList() {
    Common common = new Common(null, null, null, null, "test", null, null, null, List.of(redacted));
    assertNotNull(common.getRedacted());
    assertEquals(1, common.getRedacted().size());
    assertEquals(redacted, common.getRedacted().get(0));
  }

  @Test
  public void testAddRedacted_startFromNull() {
    Common common = new Common(null, null, null, null, "test", null, null, null, null);
    common.addRedacted(redacted);
    assertNotNull(common.getRedacted());
    assertEquals(1, common.getRedacted().size());
    assertEquals(redacted, common.getRedacted().get(0));
  }

  @Test
  public void testAddRedacted_startFromEmptyList() {
    Common common = new Common(null, null, null, null, "test", null, null, null, List.of());
    common.addRedacted(redacted);
    assertNotNull(common.getRedacted());
    assertEquals(1, common.getRedacted().size());
    assertEquals(redacted, common.getRedacted().get(0));
  }

  @Test
  public void testAddRedacted_startFromNonEmptyList() {
    Common common = new Common(null, null, null, null, "test", null, null, null, List.of(redacted));
    common.addRedacted(anotherRedacted);
    assertNotNull(common.getRedacted());
    assertEquals(2, common.getRedacted().size());
    assertEquals(redacted, common.getRedacted().get(0));
    assertEquals(anotherRedacted, common.getRedacted().get(1));
  }

  @Test
  public void testAddRedacted_multiple() {
    Common common = new Common(null, null, null, null, "test", null, null, null);
    common.addRedacted(redacted, anotherRedacted);
    assertNotNull(common.getRedacted());
    assertEquals(2, common.getRedacted().size());
    assertEquals(redacted, common.getRedacted().get(0));
    assertEquals(anotherRedacted, common.getRedacted().get(1));
  }

  @Test
  public void testAddRedacted_none() {
    Common common = new Common(null, null, null, null, "test", null, null, null, List.of(redacted, anotherRedacted));
    common.addRedacted();
    assertNotNull(common.getRedacted());
    assertEquals(2, common.getRedacted().size());
    assertEquals(redacted, common.getRedacted().get(0));
    assertEquals(anotherRedacted, common.getRedacted().get(1));
  }

}