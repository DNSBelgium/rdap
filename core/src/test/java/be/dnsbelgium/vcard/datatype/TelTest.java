package be.dnsbelgium.vcard.datatype;

import org.junit.Test;

import java.net.URISyntaxException;

import static org.junit.Assert.*;

public class TelTest {

  @Test
  public void shouldBePossibleToCreateUnvalidedPhoneNumber() throws URISyntaxException {
    Tel tel = Tel.unvalidatedTel("+29716");
    assertNotNull(tel);
    assertEquals("tel:+29716", tel.getStringValue());
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidedPhoneNumberFailsWithConstructor() throws URISyntaxException {
    new Tel("+29716");
  }

}