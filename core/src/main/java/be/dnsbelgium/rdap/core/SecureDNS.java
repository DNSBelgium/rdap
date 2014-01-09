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
package be.dnsbelgium.rdap.core;

/**
 * #%L
 * RDAP Core
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

import com.google.common.collect.ImmutableList;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

public class SecureDNS {

  private final boolean zoneSigned;
  private final boolean delegationSigned;
  private final int maxSigLife;
  private final List<DSData> dsData;
  private final List<KeyData> keyData;

  @JsonCreator
  public SecureDNS(
      @JsonProperty("zoneSigned") boolean zoneSigned, @JsonProperty("delegationSigned") boolean delegationSigned, @JsonProperty("maxSigLife") int maxSigLife, @JsonProperty("dsData") List<DSData> dsData, @JsonProperty("keyData") List<KeyData> keyData) {
    this.zoneSigned = zoneSigned;
    this.delegationSigned = delegationSigned;
    this.maxSigLife = maxSigLife;
    this.dsData = dsData == null? new ImmutableList.Builder<DSData>().build() : ImmutableList.copyOf(dsData);
    this.keyData = keyData == null? new ImmutableList.Builder<KeyData>().build() : ImmutableList.copyOf(keyData);
  }

  public boolean isZoneSigned() {
    return zoneSigned;
  }

  public boolean isDelegationSigned() {
    return delegationSigned;
  }

  public int getMaxSigLife() {
    return maxSigLife;
  }

  public List<DSData> getDsData() {
    return dsData;
  }

  public List<KeyData> getKeyData() {
    return keyData;
  }

  public static class DSData {

    private final int keyTag;

    private final int algorithm;

    private final String digest;

    private final int digestType;

    private final List<Event> events;

    @JsonCreator
    public DSData(@JsonProperty("keyTag") int keyTag, @JsonProperty("algorithm") int algorithm, @JsonProperty("digest") String digest, @JsonProperty("digestType") int digestType, @JsonProperty("events") List<Event> events) {
      this.keyTag = keyTag;
      this.algorithm = algorithm;
      this.digest = digest;
      this.digestType = digestType;
      this.events = events == null? new ImmutableList.Builder<Event>().build() : ImmutableList.copyOf(events);
    }

    public int getKeyTag() {
      return keyTag;
    }

    public int getAlgorithm() {
      return algorithm;
    }

    public String getDigest() {
      return digest;
    }

    public int getDigestType() {
      return digestType;
    }

    public List<Event> getEvents() {
      return events;
    }
  }

  public static class KeyData {
    private final String flags;

    private final String protocol;

    private final String publicKey;

    private final int algorithm;

    private final List<Event> events;

    @JsonCreator
    public KeyData(@JsonProperty("flags") String flags, @JsonProperty("protocol") String protocol, @JsonProperty("publicKey") String publicKey, @JsonProperty("algorithm") int algorithm, @JsonProperty("events") List<Event> events) {
      this.flags = flags;
      this.protocol = protocol;
      this.publicKey = publicKey;
      this.algorithm = algorithm;
      this.events = events == null? new ImmutableList.Builder<Event>().build() : ImmutableList.copyOf(events);
    }

    public String getFlags() {
      return flags;
    }

    public String getProtocol() {
      return protocol;
    }

    public String getPublicKey() {
      return publicKey;
    }

    public int getAlgorithm() {
      return algorithm;
    }

    public List<Event> getEvents() {
      return events;
    }
  }
}
