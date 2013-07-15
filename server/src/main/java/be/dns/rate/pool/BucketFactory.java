package be.dns.rate.pool;

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
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BucketFactory extends BaseKeyedPoolableObjectFactory<String, LazyLeakyBucket> {

  private static final Logger LOGGER = LoggerFactory.getLogger(BucketFactory.class);

  private final int capacity;

  private final int rate;


  /**
   * Creates a PoolableObjectFactory for LazyLeakyBuckets.
   * By default LazyLeakyBuckets are created with given capacity and rate
   *
   * @param capacity
   * @param rate
   */
  public BucketFactory(int capacity, int rate) {
    this.capacity = capacity;
    this.rate = rate;
  }

  /**
   * Create a LazyLeakyBucket that can be served by the pool.
   * <p/>
   * Currently it creates a default LazyLeakyBucket with equal paramater values as provided by the constructor arguments
   * <p/>
   * This method could be extended in the future by extracting info from the key to return a specific bucket
   * <p/>
   * It's not possible to extract info from the Spring RequestContextHolder (ip address, http headers),
   * SecurityContextHolder (current user, roles, ...) since the BaseKeyedObjectPool is a map of pools with the key param as key.
   * If one would like to take into account this info, the key should be composed of all that info
   * e.g. username#192.168.0.0/24 could be a key for a bucket for requests from the user username in ip range 192.168.0.0/24
   *
   * @param key
   * @return a LazyLeakyBucket with capacity and rate equal to the constructor args of the BucketFactory
   * @throws Exception
   */
  @Override
  public LazyLeakyBucket makeObject(String key) throws Exception {
    LOGGER.debug("Creating LazyLeakyBucket for key {}", key);
    return new LazyLeakyBucket(capacity, rate);
  }
}
