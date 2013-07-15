package be.dns.servlet;

/*
 * #%L
 * Server
 * %%
 * Copyright (C) 2013 DNS Belgium vzw
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CORSFilterTest {

  @Test
  public void testCORSFilter() throws IOException, ServletException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain filterChain = new MockFilterChain();
    CORSFilter filter = new CORSFilter();
    filter.doFilter(request, response, filterChain);
    assertTrue(response.containsHeader(CORSFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER));
    assertEquals(response.getHeader(CORSFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER), "*");
  }

  @Test
  public void testCORS() throws IOException, ServletException {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    FilterChain filterChain = Mockito.mock(FilterChain.class);
    CORSFilter filter = new CORSFilter();
    filter.doFilter(request, response, filterChain);
    Mockito.verify(response, Mockito.times(1)).addHeader(CORSFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
  }
}
