package be.dns.rate;

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

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Leaky Bucket as a meter implementation
 * with lazy update of the bucket level.
 */
public class LazyLeakyBucket {

  private static final Logger LOGGER = LoggerFactory.getLogger(LazyLeakyBucket.class);
  public static final int MILLIS_PER_SECOND = 1000;

  /**
   * Level of the bucket at timestamp lastUpdate.
   * Use LazyLeakyBucket#getLevel to get the current level
   *
   * @see LazyLeakyBucket#lastUpdate
   */
  private int level;

  /**
   * Maximum level/capacity of the bucket.
   */
  private final int capacity;

  /**
   * Rate at which the bucket leaks.
   */
  private final int rate;

  /**
   * DateTime when the level was last updated.
   *
   * @see LazyLeakyBucket#level
   */
  private DateTime lastUpdate;

  /**
   * @param capacity total number of tokens
   * @param rate     number of tokens to be added per second
   */
  public LazyLeakyBucket(final int capacity, final int rate) {
    this.level = 0;
    this.capacity = capacity;
    this.rate = rate;
    this.lastUpdate = DateTime.now();
  }

  /**
   * @param nbTokens number of tokens to be added to the bucket
   * @return true if method could add nbTokens to the bucket
   *         else returns false
   */
  public final synchronized boolean add(final int nbTokens) {
    LOGGER.debug("Adding {} tokens to bucket", nbTokens);
    if (nbTokens < 0) {
      throw new IllegalArgumentException(String.format("Cannot add negative number of tokens: %s", nbTokens));
    }
    int level = getLevel();
    if (level + nbTokens > capacity) {
      LOGGER.debug("Failed. Level: {}, Tokens: {}, Capacity: {}", level, nbTokens, capacity);
      return false;
    }
    this.level = this.level + nbTokens;
    LOGGER.debug("Succesful");
    return true;
  }

  /**
   * Calculate (lazy) the current level and return it.
   *
   * @return the level of the bucket
   */
  public final synchronized int getLevel() {
    int seconds = (int) Math.floor((DateTime.now().getMillis() - this.lastUpdate.getMillis()) / MILLIS_PER_SECOND);
    this.lastUpdate = this.lastUpdate.plusSeconds(seconds);
    this.level = Math.max(0, this.level - rate * seconds);
    LOGGER.debug("Updated level to {}", this.level);
    return level;
  }

}
