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

package be.dnsbelgium.rdap;

import be.dnsbelgium.core.CIDR;
import be.dnsbelgium.rdap.core.*;
import be.dnsbelgium.rdap.core.Error;
import be.dnsbelgium.rdap.service.IPService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("ip")
public final class IPController {

  private final static Logger logger = LoggerFactory.getLogger(IPController.class);

  @Autowired
  private IPService ipService;

  @RequestMapping(value = "/{ipaddress}", method = RequestMethod.GET, produces = Controllers.CONTENT_TYPE)
  @ResponseBody
  public IPNetwork get(@PathVariable("ipaddress") String ipAddress) throws Error {
    logger.debug("Query for ip {}", ipAddress);
    IPNetwork ipNetwork;
    try {
      ipNetwork = ipService.getIPNetwork(CIDR.of(ipAddress));
      if (ipNetwork == null) {
        logger.debug("IP result for {} is null. Throwing IPNotFound Error");
        throw new Error.IPNotFound(ipAddress);
      } else {
        ipNetwork.addRdapConformance(IPNetwork.DEFAULT_RDAP_CONFORMANCE);
      }
    } catch(Error e) {
      throw e;
    } catch(Exception e) {
      logger.error("Some errors not handled", e);
      throw new Error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
    }
    return ipNetwork;
  }

  @RequestMapping(value = "/{ipaddress}/{size}", method = RequestMethod.GET, produces = Controllers.CONTENT_TYPE)
  @ResponseBody
  public IPNetwork get(@PathVariable("ipaddress") String ipAddress, @PathVariable("size") int size) throws Error {
    IPNetwork ipNetwork;
    try {
      ipNetwork = ipService.getIPNetwork(CIDR.of(ipAddress+"/"+size));
      if (ipNetwork == null) {
        logger.debug("IP result for {}/{} is null. Throwing IPNotFound Error");
        throw new Error.IPNotFound(ipAddress + "/" + size);
      } else {
        ipNetwork.addRdapConformance(IPNetwork.DEFAULT_RDAP_CONFORMANCE);
      }
    } catch (Error e) {
      throw e;
    } catch(Exception e) {
      logger.error("Some errors not handled", e);
      throw new Error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
    }
    return ipNetwork;
  }

  @ExceptionHandler(value = Error.class)
  @ResponseBody
  protected Error handleResourceNotFoundException(Error error, HttpServletResponse response) {
    response.setStatus(error.getErrorCode());
    return error;
  }
}