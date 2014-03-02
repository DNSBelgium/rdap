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

import be.dnsbelgium.rate.UsernameLazyLeakyBucketKey;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UsernameLazyLeakyBucketKeyTest {
  public static final Logger LOGGER = LoggerFactory.getLogger(UsernameLazyLeakyBucketKeyTest.class);

  @Test
  public void testEquals() {
    Assert.assertEquals(new UsernameLazyLeakyBucketKey("userid"), new UsernameLazyLeakyBucketKey("userid"));
    Assert.assertNotEquals(new UsernameLazyLeakyBucketKey("USERID"), new UsernameLazyLeakyBucketKey("userid"));
    Assert.assertNotEquals(new UsernameLazyLeakyBucketKey("userid"), new UsernameLazyLeakyBucketKey("otherid"));
    Assert.assertNotEquals(new UsernameLazyLeakyBucketKey("userid"), null);
  }

  @Test
  public void testHashCode() {
    LOGGER.debug("Javadoc for Object#hashCode(): If two objects are equal according to the equals(Object) method, then calling the hashCode method on each of the two objects must produce the same integer result.");
    Assert.assertEquals(new UsernameLazyLeakyBucketKey("userid").hashCode(), new UsernameLazyLeakyBucketKey("userid").hashCode());
  }
}
