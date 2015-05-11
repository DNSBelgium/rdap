# RDAP Client + Server

Implementation of the [RDAP protocol](http://tools.ietf.org/html/rfc7480)

# Include RDAP library in your project
Maven:

    <dependency>
      <groupId>be.dnsbelgium</groupId>
      <artifactId>rdap-server</artifactId>
      <version>1.0.0</version>
    </dependency>
Gradle:
    
    dependencies {
      be.dnsbelgium:rdap-server:1.0.0
    }

# Extend the default service implementation(s):

    public class MyDomainService extends DefaultDomainService {

      @Override
      public Domain getDomainImpl(DomainName domainName) {
        String tld = domainName.getTldLabel().getStringValue();
        if (!supportedTlds.contains(tld)) {
          throw RDAPError.notAuthoritative(domainName);
        }
        Domain domain = domainDAO.getDomain(domainName);
        if (domain == null) {
          throw RDAPError.noResults(domainName.getStringValue());        
        }
        return domain
      }
    }

Methods in DefaultXXXService ending in Impl can be overridden. rdap_level_0 rdapconformance will be set for you if you ommit it.
The default implementations will return:
    
    {
        "errorCode": 501,
        "title": "Not implemented"
    }

# Make sure you inject your implementation
Extend WebConfig and override methods to inject your implementation(s)
    
    @Configuration    
    public class Config extends WebConfig {

      @Bean
      @Override
      public DomainService getDomainService() {
        return new MyDomainService();
      }
      ...
    }

# DomainName

    DomainName.of("xn--belgi-rsa.be").getTLD()

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

# Installing the client

From source (requires Java and Gradle)

    git clone git@github.com:DNSBelgium/rdap.git
    cd rdap/client
    gradle distTar
    # as root
    tar -C /opt -xf build/distributions/rdap-<version>.tar
    export PATH="$PATH:/opt/rdap-<version>/bin"
    rdap --help

From RPM

    su -
    rpm -ivh rdap-<version>.rpm
    man rdap
