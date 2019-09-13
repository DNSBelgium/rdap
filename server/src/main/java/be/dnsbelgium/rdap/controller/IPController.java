/**
 * Copyright 2014 DNS Belgium vzw
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package be.dnsbelgium.rdap.controller;

import be.dnsbelgium.core.CIDR;
import be.dnsbelgium.rdap.core.*;
import be.dnsbelgium.rdap.core.RDAPError;
import be.dnsbelgium.rdap.service.IPService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("ip")
public class IPController {

  private final static Logger logger = LoggerFactory.getLogger(IPController.class);

  private final IPService ipService;

  @Autowired
  public IPController(IPService ipService) {
    this.ipService = ipService;
  }

  @RequestMapping(value = "/{ipaddress}", method = RequestMethod.GET, produces = Controllers.CONTENT_TYPE)
  @ResponseBody
  public IPNetwork get(@PathVariable("ipaddress") String ipAddress) throws RDAPError {
    logger.debug("Query for ip {}", ipAddress);
    return getNetwork(ipAddress);
  }

  @RequestMapping(value = "/{ipaddress}/{size}", method = RequestMethod.GET, produces = Controllers.CONTENT_TYPE)
  @ResponseBody
  public IPNetwork get(@PathVariable("ipaddress") String ipAddress, @PathVariable("size") int size) throws RDAPError {
    ipAddress = ipAddress+"/"+size;
    return getNetwork(ipAddress);
  }

  private IPNetwork getNetwork(String ipAddress) throws RDAPError {
    IPNetwork ipNetwork;
    try {
      ipNetwork = ipService.getIPNetwork(CIDR.of(ipAddress));
      if (ipNetwork == null) {
        logger.debug("IP result for {} is null. Throwing IPNotFound Error", ipAddress);
        throw RDAPError.ipNotFound(ipAddress);
      }
    } catch (RDAPError e) {
      throw e;
    } catch(Exception e) {
      logger.error("Some errors not handled", e);
      throw new RDAPError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
    }
    return ipNetwork;
  }
}
