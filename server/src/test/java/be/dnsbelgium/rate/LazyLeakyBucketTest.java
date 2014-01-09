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
package be.dnsbelgium.rate;

import be.dnsbelgium.junit.Assert;
import org.joda.time.DateTimeUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class LazyLeakyBucketTest {

  @Test
  public void testBucket() throws InterruptedException {
    long current = DateTimeUtils.currentTimeMillis();
    DateTimeUtils.setCurrentMillisFixed(current);
    LazyLeakyBucket bucket = new LazyLeakyBucket(10, 1);
    assertEquals(0, bucket.getLevel());
    assertTrue(bucket.add(9));
    assertEquals(9, bucket.getLevel());
    assertFalse(bucket.add(2));
    assertEquals(9, bucket.getLevel());
    DateTimeUtils.setCurrentMillisFixed(current + 1000);
    assertEquals(8, bucket.getLevel());
    DateTimeUtils.setCurrentMillisFixed(current + 10000);
    assertEquals(0, bucket.getLevel());
    assertTrue(bucket.add(10));
    assertFalse(bucket.add(1));
    DateTimeUtils.setCurrentMillisFixed(current + 11000);
    assertTrue(bucket.add(1));
  }

  @Test
  public void testNegativeAmount() {
    Assert.assertThrows(new Assert.Closure() {
      @Override
      public void execute() throws Throwable {
        new LazyLeakyBucket(10, 1).add(-1);
      }
    }, IllegalArgumentException.class, "Should throw IllegalArgumentException when negative amount");
  }

}
