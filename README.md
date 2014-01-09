# RDAP Client + Server
[![Build Status](https://buildhive.cloudbees.com/job/DNSBelgium/job/rdap/badge/icon)](https://buildhive.cloudbees.com/job/DNSBelgium/job/rdap/)

Implementation of the [RDAP protocol](http://tools.ietf.org/wg/weirds/)

# UTS #46

You can change the Unicode IDNA Compatibility processing by creating a icu4j.properties file with

    idna.options=126

You can also set a System property with

    -Dicu4j.configuration=file:///path/to/file


# DomainName

    DomainName.of("xn--belgi-rsa.be").getTLD()

# Create a server project

    mvn archetype:create -DarchetypeGroupId=be.dnsbelgium.rdap \
                     -DarchetypeArtifactId=archetype \
                     -DarchetypeVersion=0.2.0 \
                     -DgroupId=com.mycompany.app \
                     -DartifactId=myproject

Implement MyDomainService

    import be.dnsbelgium.core.DomainName;
    import be.dnsbelgium.rdap.DomainController;
    import be.dnsbelgium.rdap.core.Notice;
    import be.dnsbelgium.rdap.core.Status;
    import be.dnsbelgium.rdap.service.DomainService;
    import be.dnsbelgium.rdap.core.Domain;
    import com.google.common.collect.Lists;

    import javax.annotation.Resource;

    public class MyDomainService implements DomainService {

      @Override
      public Domain getDomain(DomainName domainName) {

        if (domainName.getLevelSize() > 2) {
          throw new DomainController.DomainNotFoundException(domainName);
        }

        return new Domain.Builder()
            .setLDHName(domainName.toLDH().getStringValue())
            .addStatus(new Status.BasicStatus("active"))
            .build();

      }

    }

Although the standard doesn't allow you, you can get the Accept-Language header of the client (e.g. to only return the Notices in the client's preferred language)

    Locale l = LocaleContextHolder.getLocale();

# RDAP Extensions

Simply extend the be.dnsbelgium.rdap.core objects

Make sure you use the @JsonGenerator annotations in case of immutable objects. E.g. extending Notice

    public static class MyNotice extends Notice {

      private final String myExtension;

      @JsonCreator
      public MyNotice(
          @JsonProperty("title") String title,
          @JsonProperty("description") List<String> description,
          @JsonProperty("links") List<Link> links,
          @JsonProperty("my_extension") String extensionValue
      ) {
        super(title, description, links);
        this.myExtension = extensionValue;
      }

      public String getMyExtension() {
        return myExtension;
      }

    }

# vCard Extensions

Simply add Properties with custom names. In case of a new type, you must create a serializer for it and register it in the CustomObjectMapper
(extend CustomObjectMapper, add the Serializer in the construc

    package be.dnsbelgium.weirds.spring.rest;

    import be.dnsbelgium.rdap.jackson.CustomObjectMapper;
    import org.codehaus.jackson.JsonGenerator;
    import org.codehaus.jackson.map.JsonSerializer;
    import org.codehaus.jackson.map.ObjectMapper;
    import org.codehaus.jackson.map.SerializerProvider;

    import java.io.IOException;
    import java.util.List;

    public class MyConfig extends WebConfig {

      public static class HelloWorldJsonSerializer extends JsonSerializer {

        @Override
        public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
          jsonGenerator.writeString("Hello, World!");
        }
      }

      @Override
      public ObjectMapper getObjectMapper() {
        return new CustomObjectMapper() {
          @Override
          public List<JsonSerializer> getSerializers() {
            List<JsonSerializer> serializers = super.getSerializers();
            serializers.add(new HelloWorldJsonSerializer());
            return serializers;
          }
        };
      }

    }



# Installing the client

From source (requires Java and Maven)

    git clone git@github.com:DNSBelgium/rdap.git
    cd rdap/client
    mvn assembly:assembly
    # as root
    tar -C /opt -zxf target/client-0.2.0-bin.tar.gz
    export PATH="$PATH:/opt/client-0.2.0/bin"
    rdap --help

From RPM

    su -
    rpm -ivh rdap-client-0.2.0.rpm
    man rdap
