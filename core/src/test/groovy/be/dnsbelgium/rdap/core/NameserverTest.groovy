package be.dnsbelgium.rdap.core

import be.dnsbelgium.core.DomainName
import groovy.util.logging.Slf4j
import org.junit.Test

@Slf4j
class NameserverTest {

  @Test
  void testNoGlue() {
    log.debug('Builder should return no IpAddresses')
    def ns = new Nameserver.Builder().setLDHName(DomainName.of('ns.example.com')).build()
    assert null == ns.getIpAddresses()

    log.debug('Constructor should return empty v4 and v6 lists')
    def addr = new Nameserver.IpAddresses(null)
    assert 0 == addr.v4.size()
    assert 0 == addr.v6.size()
  }

}
