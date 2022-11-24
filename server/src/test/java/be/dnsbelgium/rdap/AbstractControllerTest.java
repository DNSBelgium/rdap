package be.dnsbelgium.rdap;

import be.dnsbelgium.core.DomainName;
import be.dnsbelgium.core.TelephoneNumber;
import be.dnsbelgium.rdap.core.*;
import be.dnsbelgium.rdap.exception.ExceptionAdvice;
import be.dnsbelgium.vcard.Contact;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Before;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractControllerTest {

  protected final DateTime createTime = DateTime.now().toDateTime(DateTimeZone.UTC).minusDays(200);
  protected final DateTime lastChangedTime = createTime.plusDays(100);

  protected MockMvc mockMvc;

  @Resource
  protected WebApplicationContext webApplicationContext;

  @Before
  public void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  @Configuration
  abstract static class Config extends WebConfig {
    @Bean
    public ExceptionAdvice exceptionAdvice() {
      return new ExceptionAdvice();
    }
  }

  protected List<Link> someLinks() throws Exception {
    Set<String> hrefLang = new HashSet<String>();
    hrefLang.add("en");
    hrefLang.add("de");
    String title = "Title";
    List<Link> links = new ArrayList<Link>();
    links.add(new Link(new URI("http://example.com/value"), "rel", new URI("http://example.com/href"), hrefLang, title, "Media", "Type"));
    links.add(new Link(new URI("http://example.com/value"), "rel", new URI("http://example.com/href"), hrefLang, title, "Media", "Type"));
    return links;
  }

  protected List<Notice> someNotices() throws Exception {
    List<Notice> notices = new ArrayList<Notice>();
    notices.add(aNoticeOrRemark());
    notices.add(aNoticeOrRemark());
    return notices;
  }

  protected List<Notice> someRemarks() throws Exception {
    List<Notice> remarks = new ArrayList<Notice>();
    remarks.add(aNoticeOrRemark());
    remarks.add(aNoticeOrRemark());
    return remarks;
  }

  protected Notice aNoticeOrRemark() throws Exception {
    List<String> description = new ArrayList<String>();
    description.add("Description part 1");
    description.add("Description part 2");
    return new Notice("Title", "Type", description, someLinks());
  }

  protected List<Event> someEvents() throws Exception {
    List<Event> events = new ArrayList<Event>();
    events.add(new Event(Event.Action.Default.REGISTRATION, "EventActor", createTime, someLinks()));
    events.add(new Event(Event.Action.Default.LAST_CHANGED, "EventActor", lastChangedTime, someLinks()));
    return events;
  }

  protected List<Status> someStatuses() throws Exception {
    List<Status> statuses = new ArrayList<Status>();
    statuses.add(Status.Default.ACTIVE);
    statuses.add(Status.Default.DELETE_PROHIBITED);
    statuses.add(Status.Factory.of("some specific status"));
    return statuses;
  }

  protected List<Nameserver> someNameservers() throws Exception {
    List<Nameserver> nameservers = new ArrayList<Nameserver>();
    nameservers.add(new Nameserver(someLinks(), someNotices(), someRemarks(), "en", someEvents(), someStatuses(), DomainName.of("whois.example.com"), "Handle", DomainName.of("ns.xn--exmple-jta.com"), DomainName.of("ns.exàmple.com"), someIpAddresses()));
    nameservers.add(new Nameserver(someLinks(), someNotices(), someRemarks(), "en", someEvents(), someStatuses(), DomainName.of("whois.example.com"), "Handle", DomainName.of("ns.xn--exmple-jta.com"), DomainName.of("ns.exàmple.com"), someIpAddresses()));
    return nameservers;
  }

  protected Nameserver.IpAddresses someIpAddresses() {
    List<String> v4s = new ArrayList<String>();
    v4s.add("193.5.6.198");
    v4s.add("89.65.3.87");
    List<String> v6s = new ArrayList<String>();
    v6s.add("2001:678:9::1");
    v6s.add("FE80:0000:0000:0000:0202:B3FF:FE1E:8329");
    return new Nameserver.IpAddresses(v4s, v6s);
  }

  protected List<PublicId> somePublicIds() {
    List<PublicId> publicIds = new ArrayList<PublicId>();
    publicIds.add(new PublicId("Type", "Identifier"));
    publicIds.add(new PublicId("Type", "Identifier"));
    return publicIds;
  }

  protected List<Entity.Role> someRoles() {
    List<Entity.Role> roles = new ArrayList<Entity.Role>();
    roles.add(Entity.Role.Default.REGISTRANT);
    roles.add(Entity.Role.Default.ADMINISTRATIVE);
    return roles;
  }

  protected Contact aContact() {
    return new Contact.Builder()
        .setFormattedName("Larry Ellison")
        .setGivenName("Larry")
        .setFamilyName("Ellison")
        .setOrganization("Retirees Inc.")
        .addOU("This is an OU")
        .addOU("This is another OU")
        .addStreet("street 1")
        .addStreet("street 2")
        .addLocality("New York")
        .addLocality("Brooklyn")
        .addRegion("New York")
        .addRegion("East coast")
        .addPostalCode("12345")
        .addCountry("United states of America")
        .addTelephoneNumber(TelephoneNumber.of(32, BigInteger.valueOf(123456)))
        .addTelephoneNumber(TelephoneNumber.of("+32.654321"))
        .addFaxNumber(TelephoneNumber.of(32, BigInteger.valueOf(987654)))
        .addEmailAddress("larry.ellison@retirees.com")
        .addEmailAddress("le@former.oracle.com")
        .setLanguages("en", "de", "es")
        .build();
  }

  protected List<Domain.Variant> someVariants() {
    List<Domain.Variant.Relation> relations1 = new ArrayList<Domain.Variant.Relation>();
    relations1.add(Domain.Variant.Relation.Default.UNREGISTERED);
    relations1.add(Domain.Variant.Relation.Default.RESTRICTED_REGISTRATION);
    List<Domain.Variant.Name> names1 = new ArrayList<Domain.Variant.Name>();
    names1.add(new Domain.Variant.Name(DomainName.of("exomple.com"), DomainName.of("exomple.com")));
    names1.add(new Domain.Variant.Name(DomainName.of("eximple.com"), DomainName.of("eximple.com")));
    List<Domain.Variant.Relation> relations2 = new ArrayList<Domain.Variant.Relation>();
    relations2.add(Domain.Variant.Relation.Default.REGISTERED);
    List<Domain.Variant.Name> names2 = new ArrayList<Domain.Variant.Name>();
    names2.add(new Domain.Variant.Name(DomainName.of("xn--exmple-jta.com"), DomainName.of("exàmple.com")));
    List<Domain.Variant> variants = new ArrayList<Domain.Variant>();
    variants.add(new Domain.Variant(relations1, "IdnTable", names1));
    variants.add(new Domain.Variant(relations2, "IdnTable2", names2));
    return variants;
  }

  protected SecureDNS aSecureDNS() {
    SecureDNS.DSData dsData1 = new SecureDNS.DSData(64156, 8, "DC48B4183F9AC496574DEB8633F627A6DE207493", 1, null, null);
    SecureDNS.DSData dsData2 = new SecureDNS.DSData(64156, 8, "DE3BBED2664B02B9FEC6FF81D8539B14A5714A2C7A92E8FE58914200 C30B1958", 2, null, null);
    List<SecureDNS.DSData> dsList = new ArrayList<SecureDNS.DSData>();
    dsList.add(dsData1);
    dsList.add(dsData2);
    return new SecureDNS(true, true, 6000, dsList, null);
  }

  protected IPNetwork anIPNetwork() throws Exception {
    return new IPNetwork(someLinks(), someNotices(), someRemarks(), "en", IPNetwork.OBJECT_CLASS_NAME, someEvents(), someStatuses(), DomainName.of("whois.example.com"), "Handle", InetAddress.getByName("193.12.32.98"), InetAddress.getByName("193.12.32.98"), "Name", "Type", "Country", "ParentHandle", someEntities());
  }

  protected List<Entity> someEntities() {
    List<Entity> entityList = new ArrayList<Entity>();
    Contact vCard = new Contact.Builder().addOU("This is an OU").addStreet("street 1").setFormattedName("This is a formatted name").build();
    Entity registrant = new Entity(null, null, null, "en", Entity.OBJECT_CLASS_NAME, null, null, null, "REGISTRANT", vCard, someRoles(), null, null, null);
    entityList.add(registrant);
    return entityList;
  }

  protected String createExpectedJson(String file) throws IOException {
    ClassPathResource cpr = new ClassPathResource(file);
    String expectedJson = Files.toString(cpr.getFile(), Charsets.UTF_8);
    expectedJson = expectedJson.replace("lastChangedTime", lastChangedTime.toString(ISODateTimeFormat.dateTimeNoMillis()));
    expectedJson = expectedJson.replace("createTime", createTime.toString(ISODateTimeFormat.dateTimeNoMillis()));
    return expectedJson;
  }
}
