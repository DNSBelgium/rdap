package be.dns.rate.spring.security;

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

import be.dns.rate.LazyLeakyBucket;
import be.dns.rate.pool.BucketFactory;
import be.dns.rdap.spring.security.RDAPErrorException;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
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

import static be.dns.junit.Assert.Closure;
import static be.dns.junit.Assert.assertThrows;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class LazyLeakyBucketVoterTest {

  private static final String USERNAME = "bob";

  @Before
  public void setup() {
    SecurityContext context = new SecurityContextImpl();
    context.setAuthentication(new TestingAuthenticationToken(USERNAME, null));
    SecurityContextHolder.setContext(context);
  }

  @After
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void testEmptyBucket() {
    final KeyedObjectPool<String, LazyLeakyBucket> pool = new GenericKeyedObjectPool<String, LazyLeakyBucket>(new BucketFactory(10, 1));
    final LazyLeakyBucketVoter voter = new LazyLeakyBucketVoter(pool, 1);
    final Authentication authentication = mock(Authentication.class);
    final Object securedObject = mock(Object.class);
    assertEquals(
        AccessDecisionVoter.ACCESS_GRANTED,
        voter.vote(authentication, securedObject, SecurityConfig.createList(LazyLeakyBucketVoter.PREFIX + "#" + 1)));
  }

  @Test
  public void testAccessDenied() {
    final KeyedObjectPool<String, LazyLeakyBucket> pool = new GenericKeyedObjectPool<String, LazyLeakyBucket>(new BucketFactory(10, 1));
    final LazyLeakyBucketVoter voter = new LazyLeakyBucketVoter(pool, 1);
    final Authentication authentication = mock(Authentication.class);
    final Object securedObject = mock(Object.class);
    assertThrows(new Closure() {
      @Override
      public void execute() throws Throwable {
        voter.vote(authentication, securedObject, SecurityConfig.createList(LazyLeakyBucketVoter.PREFIX + "#" + 20));
      }
    }, RDAPErrorException.class, "not equal");
  }

  @Test
  public void testNoLeakyBucketConfigAttributes() {
    final KeyedObjectPool<String, LazyLeakyBucket> pool = new GenericKeyedObjectPool<String, LazyLeakyBucket>(new BucketFactory(10, 1));
    final LazyLeakyBucketVoter voter = new LazyLeakyBucketVoter(pool, 20);
    final Authentication authentication = mock(Authentication.class);
    final Object securedObject = mock(Object.class);
    assertThrows(new Closure() {
      @Override
      public void execute() throws Throwable {
        voter.vote(authentication, securedObject, SecurityConfig.createList("ROLE_USER"));
      }
    }, RDAPErrorException.class, "not equal");
  }

  @Test
  public void testNullConfigAttributes() {
    final KeyedObjectPool<String, LazyLeakyBucket> pool = new GenericKeyedObjectPool<String, LazyLeakyBucket>(new BucketFactory(10, 1));
    final LazyLeakyBucketVoter voter = new LazyLeakyBucketVoter(pool, 1);
    final Authentication authentication = mock(Authentication.class);
    final Object securedObject = mock(Object.class);
    assertEquals(
        AccessDecisionVoter.ACCESS_GRANTED,
        voter.vote(authentication, securedObject, null));
  }

  @Test
  public void testNoNumber() throws Exception {
    final KeyedObjectPool<String, LazyLeakyBucket> pool = new GenericKeyedObjectPool<String, LazyLeakyBucket>(new BucketFactory(10, 1));
    final LazyLeakyBucketVoter voter = new LazyLeakyBucketVoter(pool, 1);
    final Authentication authentication = mock(Authentication.class);
    final Object securedObject = mock(Object.class);
    voter.vote(authentication, securedObject, SecurityConfig.createList(LazyLeakyBucketVoter.PREFIX + "#A"));
    LazyLeakyBucket bucket = pool.borrowObject(USERNAME);
    assertEquals(1, bucket.getLevel());
    pool.returnObject(USERNAME, bucket);
  }

  @Test
  public void testNegativeNumber() throws Exception {
    final KeyedObjectPool<String, LazyLeakyBucket> pool = new GenericKeyedObjectPool<String, LazyLeakyBucket>(new BucketFactory(10, 1));
    final LazyLeakyBucketVoter voter = new LazyLeakyBucketVoter(pool, 1);
    final Authentication authentication = mock(Authentication.class);
    final Object securedObject = mock(Object.class);
    voter.vote(authentication, securedObject, SecurityConfig.createList(LazyLeakyBucketVoter.PREFIX + "#" + (-1)));
    LazyLeakyBucket bucket = pool.borrowObject(USERNAME);
    assertEquals(0, bucket.getLevel());
    pool.returnObject(USERNAME, bucket);
  }

  @Test
  public void testEmptyNumber() throws Exception {
    final KeyedObjectPool<String, LazyLeakyBucket> pool = new GenericKeyedObjectPool<String, LazyLeakyBucket>(new BucketFactory(10, 1));
    final LazyLeakyBucketVoter voter = new LazyLeakyBucketVoter(pool, 1);
    final Authentication authentication = mock(Authentication.class);
    final Object securedObject = mock(Object.class);
    voter.vote(authentication, securedObject, SecurityConfig.createList(LazyLeakyBucketVoter.PREFIX + "#"));
    LazyLeakyBucket bucket = pool.borrowObject(USERNAME);
    assertEquals(1, bucket.getLevel());
    pool.returnObject(USERNAME, bucket);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testSupportsConfigAttribute() {
    final LazyLeakyBucketVoter voter = new LazyLeakyBucketVoter(mock(KeyedObjectPool.class), 1);
    assertEquals(true, voter.supports(new SecurityConfig(LazyLeakyBucketVoter.PREFIX + "ANY_SUFFIX")));
    assertEquals(false, voter.supports(new SecurityConfig("NOT" + LazyLeakyBucketVoter.PREFIX + "ANY_SUFFIX")));
    assertEquals(false, voter.supports(new ConfigAttribute() {
      @Override
      public String getAttribute() {
        return null;
      }
    }));
  }

}