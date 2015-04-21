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
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@JsonPropertyOrder({"rdapConformance"})
public final class Domain extends Common {
  
  public static final String OBJECT_CLASS_NAME = "domain";

  public List<PublicId> getPublicIds() {
    return publicIds;
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

  public String handle;

  public DomainName ldhName, unicodeName;

  public List<Variant> variants;

  public List<Nameserver> nameservers;

  public SecureDNS secureDNS;

  public List<Entity> entities;

  public List<PublicId> publicIds;

  public IPNetwork network;

  public Domain(
      @JsonProperty("links") List<Link> links,
      @JsonProperty("notices") List<Notice> notices,
      @JsonProperty("remarks") List<Remark> remarks,
      @JsonProperty("lang") String lang,
      @JsonProperty("objectClassName") String objectClassName,
      @JsonProperty("events") List<Event> events,
      @JsonProperty("status") List<Status> status,
      @JsonProperty("port43") DomainName port43,
      @JsonProperty("handle") String handle,
      @JsonProperty("ldhName") DomainName ldhName,
      @JsonProperty("unicodeName") DomainName unicodeName,
      @JsonProperty("variants") List<Variant> variants,
      @JsonProperty("nameServers") List<Nameserver> nameservers,
      @JsonProperty("secureDNS") SecureDNS secureDNS,
      @JsonProperty("entities") List<Entity> entities,
      @JsonProperty("publicIds") List<PublicId> publicIds,
      @JsonProperty("network") IPNetwork network) {
    super(links, notices, remarks, lang, objectClassName, events, status, port43);
    this.handle = handle;
    this.ldhName = ldhName;
    this.unicodeName = unicodeName;
    this.variants = variants == null ? null : new ImmutableList.Builder<Variant>().addAll(variants).build();
    this.nameservers = nameservers == null ? null : new ImmutableList.Builder<Nameserver>().addAll(nameservers).build();
    this.secureDNS = secureDNS;
    this.entities = entities == null ? null : new ImmutableList.Builder<Entity>().addAll(entities).build();
    this.publicIds = publicIds == null ? null : new ImmutableList.Builder<PublicId>().addAll(publicIds).build();
    this.network = network;
  }

//  public String getHandle() {
//    return handle;
//  }
//
//  public DomainName getLdhName() {
//    return ldhName;
//  }
//
//  public DomainName getUnicodeName() {
//    return unicodeName;
//  }
//
//  public List<Variant> getVariants() {
//    return variants;
//  }
//
//  public List<Nameserver> getNameservers() {
//    return nameservers;
//  }
//
//  public SecureDNS getSecureDNS() {
//    return secureDNS;
//  }
//
//  public List<Entity> getEntities() {
//    return entities;
//  }

//  public static class Builder {
//
//    private Set<String> rdapConformance;
//    private List<Link> links;
//    private List<Notice> notices;
//    private List<Remark> remarks;
//    private String lang;
//    private List<Event> events;
//    private List<Status> status;
//    private DomainName port43;
//    private String handle;
//    private DomainName ldhName;
//    private DomainName unicodeName;
//    private List<Variant> variants;
//    private List<Nameserver> nameservers;
//    private SecureDNS secureDNS;
//    private List<Entity> entities;
//    private List<PublicId> publicIds;
//    private IPNetwork network;
//
//    public Builder addRDAPConformance(String conformance) {
//      if (rdapConformance == null) {
//        rdapConformance = new HashSet<String>();
//      }
//      rdapConformance.add(conformance);
//      return this;
//    }
//
//    public Builder setLDHName(String domainName) {
//      this.ldhName = DomainName.of(domainName);
//      return this;
//    }
//
//    public Builder setUnicodeName(String domainName) {
//      this.unicodeName = DomainName.of(domainName);
//      return this;
//    }
//
//    public Builder addStatus(Status status) {
//      if (this.status == null) {
//        this.status = new ArrayList<Status>();
//      }
//      this.status.add(status);
//      return this;
//    }
//
//    public Builder addLink(Link link) {
//      if (links == null) {
//        links = new ArrayList<Link>();
//      }
//      links.add(link);
//      return this;
//    }
//
//    public Builder setPort43(DomainName domainName) {
//      this.port43 = domainName;
//      return this;
//    }
//
//    public Builder addEntity(Entity entity) {
//      if (entities == null) {
//        entities = new ArrayList<Entity>();
//      }
//      entities.add(entity);
//      return this;
//    }
//
//    public Builder addEvent(Event e) {
//      if (events == null) {
//        events = new ArrayList<Event>();
//      }
//      this.events.add(e);
//      return this;
//    }
//
//    public Builder addRemark(Remark n) {
//      if (remarks == null) {
//        remarks = new ArrayList<Remark>();
//      }
//      this.remarks.add(n);
//      return this;
//    }
//
//    public Domain build() {
//      return new Domain(rdapConformance, links, notices, remarks, lang, OBJECT_CLASS_NAME, events, status, port43, handle, ldhName, unicodeName, variants, nameservers, secureDNS, entities, publicIds, network);
//    }
//
//    public Builder setSecureDNS(SecureDNS secureDNS) {
//      this.secureDNS = secureDNS;
//      return this;
//    }
//
//    public Builder addNameserver(Nameserver ns) {
//      if (nameservers == null) {
//        nameservers = new ArrayList<Nameserver>();
//      }
//      nameservers.add(ns);
//      return this;
//    }
//
//    public Builder setNetwork(IPNetwork network) {
//      this.network = network;
//      return this;
//    }
//  }
}
