# RDAP server library

This java library makes it very easy to build an RDAP server that talks with your registry back-end.

# Features
* Include this library in your Java web application to significantly ease implementing an RDAP server
* Can be combined with any back-end by simply implementing one or more methods
* All you need to do is retrieve the data and populate some POJO's
* The library takes care of parsing the incoming RDAP requests, calling the right methods and generating JSON responses
  from those POJO's
* This project also contains a simple command-line RDAP client to test your RDAP server

# Details

This library understands and supports the following RFC's:

* [RFC-7480 : HTTP Usage in the Registration Data Access Protocol (RDAP)](http://tools.ietf.org/html/rfc7480)
* [RFC-7481 : Security Services for the Registration Data Access Protocol (RDAP)](http://tools.ietf.org/html/rfc7481)
* [RFC-7482 : Registration Data Access Protocol (RDAP) Query Format](http://tools.ietf.org/html/rfc7482)
* [RFC-7483 : JSON Responses for the Registration Data Access Protocol (RDAP)](http://tools.ietf.org/html/rfc7483)
* [RFC-7484 : Finding the Authoritative Registration Data (RDAP) Service](http://tools.ietf.org/html/rfc7484)

# How it works

* The library contains a number of POJO's (plain old Java objects) representing the data structures defined in rfc7483
* You need to write the code to populate these objects whenever a query comes in

# The flow
Let's assume that you have implemented a class com.example.rdap.MyDomainService which extends DefaultDomainService
and that you have deployed your application in an application server listening at port 8080

* The client sends an RDAP query to your server (eg. curl http://localhost:8080/rdap/domain/abc.brussels)
* Your implementation of getDomainImpl(DomainName domainName) gets called with a DomainName object corresponding to the input (abc.brussels)
* Your implementation retrieves all relevant data about abc.brussels from your datastore an populates a be.dnsbelgium.rdap.core.Domain object
* The rdap server library uses this object to generate the correct JSON an return it to the client

# How to use this library

## Java web application example implementation

We have created a sample project which could help you in implemention. You can find both the source and instructions in the following project: [rdap-server-sample-gtld](https://github.com/DNSBelgium/rdap-server-sample-gtld)

## Build your own implementation

The instructions below assume that you have a recent version of the JDK and Apache Maven on your system.
Of course you can use other build tools (Ant, Gradle, ...) instead of Maven.

## Create a new java web application, for example by running

   mvn archetype:generate -DgroupId=be.yourcompany -DartifactId=rdap -DarchetypeArtifactId=maven-archetype-webapp

## Declare a dependency on the RDAP server library in your project

Maven: add this dependency in your pom.xml file

    <dependency>
      <groupId>be.dnsbelgium</groupId>
      <artifactId>rdap-server</artifactId>
      <version>1.0.3</version>
    </dependency>

Gradle: add this snippet in your build.gradle file
    
    dependencies {
      be.dnsbelgium:rdap-server:1.0.3
    }

For other build tools, check http://mvnrepository.com/artifact/be.dnsbelgium/rdap-server/1.0.3

## How to integrate with your back-end

The library uses Spring MVC to map incoming requests to the default service implementations
The default implementations will return:

    {
        "errorCode": 501,
        "title": "Not implemented"
    }

You only need to do the following to use your own Service implementations:

* Extend one or more default implementations

* Extend WebConfig and override the appropriate methods to make sure your implementations are being used

* configure the org.springframework.web.servlet.DispatcherServlet

The following sections describe these steps in more detail

## Extend the default service implementation(s):

    For example:

    public class MyDomainService extends DefaultDomainService {

      @Override
      public Domain getDomainImpl(DomainName domainName) {
        String tld = domainName.getTldLabel().getStringValue();
        if (!supportedTlds.contains(tld)) {
          throw RDAPError.notAuthoritative(domainName);
        }
        // obviously, you need to implement DomainDAO.getDomain to retrieve the data from your back-end
        // for example by querying your RDMBS via JDBC
        Domain domain = domainDAO.getDomain(domainName);
        if (domain == null) {
          throw RDAPError.noResults(domainName.getStringValue());        
        }
        return domain
      }
    }

Please note that DomainName, Domain and RDAPError are all classes defined in the rdap-service library,
while in this example DomainDAO is supposed to be a class that you have implemented to retrieve the relevant data from your datastore.


The methods in DefaultXXXService ending in Impl can be overridden. rdap_level_0 rdapconformance will be set for you if you omit it.

## Make sure you inject your implementation
Extend WebConfig and override methods to make sure your classes are being used instead of the default implementations:
    
    @Configuration    
    public class Config extends WebConfig {

      @Bean
      @Override
      public DomainService getDomainService() {
        return new MyDomainService();
      }

    }

## Configure the DispatcherServlet

This servlet needs to know your WebConfig implementation. The easiest way to do this is by passing in the fully qualified classname in web.xml

    <web-app xmlns="http://java.sun.com/xml/ns/javaee"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
            version="3.0">

        <display-name>RDAP Web Application</display-name>

        <servlet>
            <servlet-name>rdap</servlet-name>
            <servlet-class>
              org.springframework.web.servlet.DispatcherServlet
            </servlet-class>
            <init-param>
                <param-name>contextClass</param-name>
                <param-value>org.springframework.web.context.support.AnnotationConfigWebApplicationContext</param-value>
            </init-param>
            <init-param>
                <param-name>contextConfigLocation</param-name>
                <param-value>at.nic.rdap.sample.Config</param-value>
            </init-param>
            <load-on-startup>1</load-on-startup>
        </servlet>

        <servlet-mapping>
            <servlet-name>rdap</servlet-name>
            <url-pattern>/</url-pattern>
        </servlet-mapping>

    </web-app>

# Various other tips

## Constructing a DomainName object

    DomainName.of("xn--belgi-rsa.be").getTLD()

## Implementing RDAP Extensions

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

# Installing the RDAP client

From source (requires Java and Gradle)

    git clone git@github.com:DNSBelgium/rdap.git
    cd rdap/client
    gradle distTar
    sudo tar -C /opt -xf build/distributions/rdap-<version>.tar
    export PATH="$PATH:/opt/rdap-<version>/bin"
    rdap --help

From RPM

    su -
    rpm -ivh rdap-<version>.rpm
    man rdap
