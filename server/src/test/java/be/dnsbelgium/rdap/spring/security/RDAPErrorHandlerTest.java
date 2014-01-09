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
package be.dnsbelgium.rdap.spring.security;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import java.io.IOException;

public class RDAPErrorHandlerTest {

  @Test
  public void testHandler() throws IOException, ServletException {
    RDAPErrorHandler errorHandler = new RDAPErrorHandler();
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    RDAPErrorException exception = new RDAPErrorException(499, "title", "desc1", "desc2");
    errorHandler.handle(request, response, exception);
    Assert.assertEquals("{\"errorCode\":499,\"title\":\"title\",\"description\":[\"desc1\",\"desc2\"]}", response.getContentAsString());
  }

  @Test
  public void testHandlerNullDescription() throws IOException, ServletException {
    RDAPErrorHandler errorHandler = new RDAPErrorHandler();
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    RDAPErrorException exception = new RDAPErrorException(499, "title", (String) null);
    errorHandler.handle(request, response, exception);
    Assert.assertEquals("{\"errorCode\":499,\"title\":\"title\"}", response.getContentAsString());
  }
}
