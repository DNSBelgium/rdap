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
package be.dnsbelgium.rate.spring.security;

import be.dnsbelgium.rate.LeakyBucketKey;
import be.dnsbelgium.rate.LeakyBucketService;
import be.dnsbelgium.rdap.spring.security.RDAPErrorException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class LeakyBucketVoterTest {

  private static final String USERNAME = "bob";
  public static final int DEFAULT_AMOUNT = 1;
  public static final int CONFIG_AMOUNT = 2;
  public static final String LB_CONFIG = LeakyBucketVoter.PREFIX + "#" + CONFIG_AMOUNT;
  private UsernameLeakyBucketKeyFactory keyFactory;

  @Before
  public void setup() {
    SecurityContext context = new SecurityContextImpl();
    context.setAuthentication(new TestingAuthenticationToken(USERNAME, null));
    SecurityContextHolder.setContext(context);

    keyFactory = new UsernameLeakyBucketKeyFactory();
  }

  @After
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void testEmptyBucket() {
    LeakyBucketService service = mock(LeakyBucketService.class);
    when(service.add(any(LeakyBucketKey.class), eq(CONFIG_AMOUNT))).thenReturn(true);
    final LeakyBucketVoter voter = new LeakyBucketVoter(service, keyFactory, DEFAULT_AMOUNT);
    final Authentication authentication = mock(Authentication.class);
    final Object securedObject = mock(Object.class);
    assertEquals(
        AccessDecisionVoter.ACCESS_GRANTED,
        voter.vote(authentication, securedObject, SecurityConfig.createList(LB_CONFIG)));
    verify(service, times(1)).add(any(LeakyBucketKey.class), eq(CONFIG_AMOUNT));
  }

  @Test(expected = RDAPErrorException.class)
  public void testFullBucket() {
    LeakyBucketService service = mock(LeakyBucketService.class);
    when(service.add(any(LeakyBucketKey.class), eq(CONFIG_AMOUNT))).thenReturn(false);
    final LeakyBucketVoter voter = new LeakyBucketVoter(service, keyFactory, DEFAULT_AMOUNT);
    final Authentication authentication = mock(Authentication.class);
    final Object securedObject = mock(Object.class);
    try {
      voter.vote(authentication, securedObject, SecurityConfig.createList(LB_CONFIG));
    } catch (RDAPErrorException rde) {
      verify(service, times(1)).add(any(LeakyBucketKey.class), eq(CONFIG_AMOUNT));
      throw rde;
    }
  }

  @Test
  public void testNoLeakyBucketConfigAttributes() {
    LeakyBucketService service = mock(LeakyBucketService.class);
    when(service.add(any(LeakyBucketKey.class), eq(DEFAULT_AMOUNT))).thenReturn(true);
    final LeakyBucketVoter voter = new LeakyBucketVoter(service, keyFactory, DEFAULT_AMOUNT);
    final Authentication authentication = mock(Authentication.class);
    final Object securedObject = mock(Object.class);
    assertEquals(
        AccessDecisionVoter.ACCESS_GRANTED,
        voter.vote(authentication, securedObject, SecurityConfig.createList("ROLE_USER")));
    verify(service, times(1)).add(any(LeakyBucketKey.class), eq(DEFAULT_AMOUNT));

  }


  @Test
  public void testNullConfigAttributes() {
    LeakyBucketService service = mock(LeakyBucketService.class);
    when(service.add(any(LeakyBucketKey.class), eq(DEFAULT_AMOUNT))).thenReturn(true);
    final LeakyBucketVoter voter = new LeakyBucketVoter(service, keyFactory, DEFAULT_AMOUNT);
    final Authentication authentication = mock(Authentication.class);
    final Object securedObject = mock(Object.class);
    assertEquals(
        AccessDecisionVoter.ACCESS_GRANTED,
        voter.vote(authentication, securedObject, null));
    verify(service, times(1)).add(any(LeakyBucketKey.class), eq(DEFAULT_AMOUNT));
  }

  @Test
  public void testNoNumber() throws Exception {
    LeakyBucketService service = mock(LeakyBucketService.class);
    when(service.add(any(LeakyBucketKey.class), eq(DEFAULT_AMOUNT))).thenReturn(true);
    final LeakyBucketVoter voter = new LeakyBucketVoter(service, keyFactory, DEFAULT_AMOUNT);
    final Authentication authentication = mock(Authentication.class);
    final Object securedObject = mock(Object.class);
    assertEquals(
        AccessDecisionVoter.ACCESS_GRANTED,
        voter.vote(authentication, securedObject, SecurityConfig.createList(LeakyBucketVoter.PREFIX + "#A")));
    verify(service, times(1)).add(any(LeakyBucketKey.class), eq(DEFAULT_AMOUNT));
  }

  @Test
  public void testNegativeNumber() throws Exception {
    LeakyBucketService service = mock(LeakyBucketService.class);
    when(service.add(any(LeakyBucketKey.class), eq(DEFAULT_AMOUNT))).thenReturn(true);
    final LeakyBucketVoter voter = new LeakyBucketVoter(service, keyFactory, DEFAULT_AMOUNT);
    final Authentication authentication = mock(Authentication.class);
    final Object securedObject = mock(Object.class);

    assertEquals(
        AccessDecisionVoter.ACCESS_GRANTED,
        voter.vote(authentication, securedObject, SecurityConfig.createList(LeakyBucketVoter.PREFIX + "#" + (-DEFAULT_AMOUNT))));
    verify(service, never()).add(any(LeakyBucketKey.class), eq(DEFAULT_AMOUNT));
  }

  @Test
  public void testEmptyNumber() throws Exception {
    LeakyBucketService service = mock(LeakyBucketService.class);
    when(service.add(any(LeakyBucketKey.class), eq(DEFAULT_AMOUNT))).thenReturn(true);
    final LeakyBucketVoter voter = new LeakyBucketVoter(service, keyFactory, DEFAULT_AMOUNT);
    final Authentication authentication = mock(Authentication.class);
    final Object securedObject = mock(Object.class);

    assertEquals(
        AccessDecisionVoter.ACCESS_GRANTED,
        voter.vote(authentication, securedObject, SecurityConfig.createList(LeakyBucketVoter.PREFIX + "#")));
    verify(service, times(1)).add(any(LeakyBucketKey.class), eq(DEFAULT_AMOUNT));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testSupportsConfigAttribute() {
    final LeakyBucketVoter voter = new LeakyBucketVoter(mock(LeakyBucketService.class), keyFactory, DEFAULT_AMOUNT);
    assertEquals(true, voter.supports(new SecurityConfig(LeakyBucketVoter.PREFIX + "ANY_SUFFIX")));
    assertEquals(false, voter.supports(new SecurityConfig("NOT" + LeakyBucketVoter.PREFIX + "ANY_SUFFIX")));
    assertEquals(false, voter.supports(new ConfigAttribute() {
      @Override
      public String getAttribute() {
        return null;
      }
    }));
  }

}