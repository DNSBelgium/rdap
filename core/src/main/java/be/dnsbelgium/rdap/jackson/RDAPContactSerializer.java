package be.dnsbelgium.rdap.jackson;

import be.dnsbelgium.vcard.Contact;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

public class RDAPContactSerializer extends ContactSerializer {
  @Override
  public void serialize(Contact contact, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
    // start write version
    jsonGenerator.writeStartArray();
    jsonGenerator.writeString("vcard");
    super.serialize(contact, jsonGenerator, serializerProvider);
    jsonGenerator.writeEndArray();
  }
}
