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

import be.dnsbelgium.rdap.Controllers;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RDAPErrorHandler extends AccessDeniedHandlerImpl {

  private final ObjectMapper mapper;

  public RDAPErrorHandler() {
    super();
    mapper = new ObjectMapper();
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  @Override
  public final void handle(final HttpServletRequest request, final HttpServletResponse response, final AccessDeniedException e) throws IOException, ServletException {
    if (e instanceof RDAPErrorException) {
      response.setContentType(Controllers.CONTENT_TYPE);
      RDAPErrorException exception = (RDAPErrorException) e;
      response.setStatus(exception.getErrorCode());
      mapper.writeValue(response.getOutputStream(), exception);
      response.flushBuffer();
    } else {
      super.handle(request, response, e);
    }
  }
}
