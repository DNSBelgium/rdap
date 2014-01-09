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
package be.dnsbelgium.rdap.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

public class LoggerFilter implements Filter {

  public static class StatusExposingServletResponse extends HttpServletResponseWrapper {

    private int httpStatus = SC_OK;

    public StatusExposingServletResponse(HttpServletResponse response) {
      super(response);
    }

    @Override
    public void sendError(int sc) throws IOException {
      httpStatus = sc;
      super.sendError(sc);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
      httpStatus = sc;
      super.sendError(sc, msg);
    }


    @Override
    public void setStatus(int sc) {
      httpStatus = sc;
      super.setStatus(sc);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
      httpStatus = SC_MOVED_TEMPORARILY;
      super.sendRedirect(location);
    }

    public int getStatus() {
      return httpStatus;
    }

  }

  private static final Logger LOGGER = LoggerFactory.getLogger(LoggerFilter.class);

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    StatusExposingServletResponse response = new StatusExposingServletResponse((HttpServletResponse) servletResponse);
    try {
      chain.doFilter(servletRequest, response);
    } finally {
      String currentUser = null;
      try {
        currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
      } catch (NullPointerException npe) {
        LOGGER.debug("No SecurityContext", npe);
      }
      LOGGER.info("{} {} {} {}", request.getRemoteAddr(), currentUser, request.getServletPath(), response.getStatus());
    }
  }

  @Override
  public void destroy() {

  }
}
