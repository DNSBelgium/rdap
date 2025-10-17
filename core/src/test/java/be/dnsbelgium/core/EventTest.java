package be.dnsbelgium.core;

import be.dnsbelgium.rdap.core.Event;
import org.junit.Test;

import static be.dnsbelgium.rdap.core.Event.Action.Default.*;
import static org.junit.Assert.assertEquals;

public class EventTest {

  @Test
  public void testDefaultActions() {
    assertActionValue(REGISTRATION, "registration");
    assertActionValue(REREGISTRATION, "reregistration");
    assertActionValue(LAST_CHANGED, "last changed");
    assertActionValue(EXPIRATION, "expiration");
    assertActionValue(REGISTRAR_EXPIRATION, "registrar expiration");
    assertActionValue(DELETION, "deletion");
    assertActionValue(REINSTANTIATION, "reinstantiation");
    assertActionValue(TRANSFER, "transfer");
    assertActionValue(LOCKED, "locked");
    assertActionValue(UNLOCKED, "unlocked");
    assertActionValue(LAST_UPDATE_OF_RDAP_DATABASE, "last update of RDAP database");
  }

  private static void assertActionValue(Event.Action action, String expected) {
    assertEquals(expected, action.getValue());
  }

}
