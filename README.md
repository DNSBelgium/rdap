# RDAP Client + Server

Implementation of the [RDAP protocol](http://tools.ietf.org/html/rfc7480)

# UTS #46

You can change the Unicode IDNA Compatibility processing by creating a icu4j.properties file with

    idna.options=126

You can also set a System property with

    -Dicu4j.configuration=file:///path/to/file

# DomainName

    DomainName.of("xn--belgi-rsa.be").getTLD()

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
    rpm -ivh rdap-client-0.2.0.rpm
    man rdap
