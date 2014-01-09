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

import be.dnsbelgium.core.DomainName;
import com.google.common.collect.ImmutableList;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class Domain extends Common {

  public List<PublicId> getPublicIds() {
    return publicIds;
  }

  public static final class DelegationKey {

    private final String digest;

    private final int algorithm, digestType, keyTag;

    public int getAlgorithm() {
      return algorithm;
    }

    public String getDigest() {
      return digest;
    }

    public int getDigestType() {
      return digestType;
    }

    public int getKeyTag() {
      return keyTag;
    }

    @JsonCreator
    public DelegationKey(
        @JsonProperty("algorithm") int algorithm,
        @JsonProperty("digest") String digest,
        @JsonProperty("digestType") int digestType,
        @JsonProperty("keyTag") int keyTag) {
      this.algorithm = algorithm;
      this.digest = digest;
      this.digestType = digestType;
      this.keyTag = keyTag;
    }
  }

  public static final class Variant {

    public interface Relation {

      final class Factory {

        private Factory() {

        }

        private static final Logger LOGGER = LoggerFactory.getLogger(Factory.class);

        public static Relation of(String relation) {
          try {
            return Default.valueOf(relation.toUpperCase(Locale.ENGLISH));
          } catch (IllegalArgumentException iae) {
            LOGGER.debug("Not a standard relation", iae);
            return new BasicRelation(relation);
          }
        }
      }

      public static enum Default implements Relation {
        REGISTERED("registered"),
        UNREGISTERED("unregistered"),
        RESTRICTED_REGISTRATION("restricted registration"),
        OPEN_REGISTRATION("open registration"),
        CONJOINED("conjoined");

        private final String value;

        private Default(String value) {
          this.value = value;
        }

        @Override
        public String getValue() {
          return this.value;
        }

      }

      class BasicRelation implements Relation {

        private final String value;

        public BasicRelation(String value) {
          this.value = value;
        }

        @Override
        public String getValue() {
          return value;
        }
      }

      String getValue();
    }

    public static class Name {

      private final DomainName ldhName;

      private final DomainName unicodeName;

      @JsonCreator
      public Name(
          @JsonProperty("ldhName") DomainName ldhName,
          @JsonProperty("unicodeName") DomainName unicodeName) {
        this.ldhName = ldhName;
        this.unicodeName = unicodeName;
      }

      public DomainName getLdhName() {
        return ldhName;
      }

      public DomainName getUnicodeName() {
        return unicodeName;
      }
    }

    private final List<Relation> relations;

    private final String idnTable;

    private final List<Name> variantNames;

    @JsonCreator
    public Variant(
        @JsonProperty("relation") List<Relation> relations,
        @JsonProperty("idnTable") String idnTable,
        @JsonProperty("variantNames") List<Name> variantNames) {
      this.relations = new ImmutableList.Builder<Relation>().addAll(relations).build();
      this.idnTable = idnTable;
      this.variantNames = new ImmutableList.Builder<Name>().addAll(variantNames).build();
    }

    public List<Relation> getRelations() {
      return relations;
    }

    public List<Name> getVariantNames() {
      return variantNames;
    }
  }

  private final String handle;

  private final DomainName ldhName, unicodeName;

  private final List<Variant> variants;

  private final List<Nameserver> nameservers;

  private final List<DelegationKey> delegationKeys;

  private final List<Entity> entities;

  private final List<PublicId> publicIds;

  public Domain(
      @JsonProperty("rdapConformance") Set<String> rdapConformance,
      @JsonProperty("links") List<Link> links,
      @JsonProperty("notices") List<Notice> notices,
      @JsonProperty("remarks") List<Notice> remarks,
      @JsonProperty("lang") String lang,
      @JsonProperty("events") List<Event> events,
      @JsonProperty("status") List<Status> status,
      @JsonProperty("port43") DomainName port43,
      @JsonProperty("handle") String handle,
      @JsonProperty("ldhName") DomainName ldhName,
      @JsonProperty("unicodeName") DomainName unicodeName,
      @JsonProperty("variants") List<Variant> variants,
      @JsonProperty("nameServers") List<Nameserver> nameservers,
      @JsonProperty("delegationKeys") List<DelegationKey> delegationKeys,
      @JsonProperty("entities") List<Entity> entities,
      @JsonProperty("publicIds") List<PublicId> publicIds) {
    super(rdapConformance, links, notices, remarks, lang, events, status, port43);
    this.handle = handle;
    this.ldhName = ldhName;
    this.unicodeName = unicodeName;
    this.variants = variants == null ? null : new ImmutableList.Builder<Variant>().addAll(variants).build();
    this.nameservers = nameservers == null ? null : new ImmutableList.Builder<Nameserver>().addAll(nameservers).build();
    this.delegationKeys = delegationKeys == null ? null : new ImmutableList.Builder<DelegationKey>().addAll(delegationKeys).build();
    this.entities = entities == null ? null : new ImmutableList.Builder<Entity>().addAll(entities).build();
    this.publicIds = publicIds == null ? null : new ImmutableList.Builder<PublicId>().addAll(publicIds).build();
  }

  public String getHandle() {
    return handle;
  }

  public DomainName getLdhName() {
    return ldhName;
  }

  public DomainName getUnicodeName() {
    return unicodeName;
  }

  public List<Variant> getVariants() {
    return variants;
  }

  public List<Nameserver> getNameservers() {
    return nameservers;
  }

  public List<DelegationKey> getDelegationKeys() {
    return delegationKeys;
  }

  public List<Entity> getEntities() {
    return entities;
  }

  public static class Builder {

    private Set<String> rdapConformance;
    private List<Link> links;
    private List<Notice> notices;
    private List<Notice> remarks;
    private String lang;
    private List<Event> events;
    private List<Status> status;
    private DomainName port43;
    private String handle;
    private DomainName ldhName;
    private DomainName unicodeName;
    private List<Variant> variants;
    private List<Nameserver> nameservers;
    private List<DelegationKey> delegationKeys;
    private List<Entity> entities;
    private List<PublicId> publicIds;

    public Builder() {
      rdapConformance = new HashSet<String>();
      rdapConformance.add(DEFAULT_RDAP_CONFORMANCE);
    }

    public Builder addRDAPConformance(String conformance) {
      rdapConformance.add(conformance);
      return this;
    }

    public Builder setLDHName(String domainName) {
      this.ldhName = DomainName.of(domainName);
      return this;
    }

    public Builder setUnicodeName(String domainName) {
      this.unicodeName = DomainName.of(domainName);
      return this;
    }

    public Builder addStatus(Status status) {
      if (this.status == null) {
        this.status = new ArrayList<Status>();
      }
      this.status.add(status);
      return this;
    }

    public Builder addLink(Link link) {
      if (links == null) {
        links = new ArrayList<Link>();
      }
      links.add(link);
      return this;
    }

    public Builder setPort43(DomainName domainName) {
      this.port43 = domainName;
      return this;
    }

    public Builder addEntity(Entity entity) {
      if (entities == null) {
        entities = new ArrayList<Entity>();
      }
      entities.add(entity);
      return this;
    }

    public Builder addEvent(Event e) {
      if (events == null) {
        events = new ArrayList<Event>();
      }
      this.events.add(e);
      return this;
    }

    public Builder addRemark(Notice n) {
      if (remarks == null) {
        remarks = new ArrayList<Notice>();
      }
      this.remarks.add(n);
      return this;
    }

    public Domain build() {
      return new Domain(rdapConformance, links, notices, remarks, lang, events, status, port43, handle, ldhName, unicodeName, variants, nameservers, delegationKeys, entities, publicIds);
    }

    public Builder addDelegationKey(DelegationKey delegationKey) {
      if (delegationKeys == null) {
        delegationKeys = new ArrayList<DelegationKey>();
      }
      this.delegationKeys.add(delegationKey);
      return this;
    }

    public Builder addNameserver(Nameserver ns) {
      if (nameservers == null) {
        nameservers = new ArrayList<Nameserver>();
      }
      nameservers.add(ns);
      return this;
    }
  }
}
