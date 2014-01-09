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
package be.dnsbelgium.rate.pool;

import be.dnsbelgium.rate.LazyLeakyBucket;
import be.dnsbelgium.rate.spring.security.LazyLeakyBucketKey;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaticLeakyBucketFactory extends BaseKeyedPoolableObjectFactory<LazyLeakyBucketKey, LazyLeakyBucket> {

  private static final Logger LOGGER = LoggerFactory.getLogger(StaticLeakyBucketFactory.class);

  private final int capacity;

  private final int rate;


  /**
   * Creates a PoolableObjectFactory for LazyLeakyBuckets.
   * By default LazyLeakyBuckets are created with given capacity and rate
   *
   * @param capacity
   * @param rate
   */
  public StaticLeakyBucketFactory(int capacity, int rate) {
    this.capacity = capacity;
    this.rate = rate;
  }

  /**
   * Create a LazyLeakyBucket with capacity == this.capacity and rate == this.rate
   * Thus does not differentiate between different buckets
   *
   * @param key
   * @return a LazyLeakyBucket with capacity and rate equal to the constructor args of the StaticLeakyBucketFactory
   * @throws Exception
   */
  @Override
  public LazyLeakyBucket makeObject(LazyLeakyBucketKey key) throws Exception {
    LOGGER.debug("Creating LazyLeakyBucket for key {}", key);
    return new LazyLeakyBucket(capacity, rate);
  }
}
