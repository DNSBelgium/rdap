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

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
* Created by pieterv on 12/19/13.
*/
public class UsernameLazyLeakyBucketKey implements LazyLeakyBucketKey {

  private final String userId;

  public UsernameLazyLeakyBucketKey(String userId) {
    this.userId = userId;
  }

  public String getUsername() {
    return userId;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(3,5).append(userId).toHashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    UsernameLazyLeakyBucketKey that = (UsernameLazyLeakyBucketKey) o;

    return userId.equals(that.userId);

  }
}
