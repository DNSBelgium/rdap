package be.dnsbelgium.rdap.jackson;

import be.dnsbelgium.vcard.Contact;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

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
