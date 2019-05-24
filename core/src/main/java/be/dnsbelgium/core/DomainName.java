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
package be.dnsbelgium.core;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class DomainName {

  private final List<Label> labels;

  public DomainName(List<Label> labels) {
    checkNotNull(labels, "labels must not be null");
    checkArgument(labels.size() > 0, "labels.size() should be > 0");
    for (int i = 0; i < labels.size() - 1; i++) {
      if (labels.get(i) instanceof Label.RootLabel) {
        throw new IllegalArgumentException("Only the last label may be a root label");
      }
    }

    this.labels = new ImmutableList.Builder<Label>().addAll(labels).build();
  }

  public static DomainName of(String domainName) {
    String[] labels = StringUtils.splitPreserveAllTokens(domainName, '.');
    ImmutableList.Builder<Label> builder = new ImmutableList.Builder<Label>();
    for (String label : labels) {
      builder.add(Label.of(label));
    }
    return new DomainName(builder.build());
  }

  public DomainName toFQDN() {
    if (isFQDN()) {
      return this;
    }
    return new DomainName(new ImmutableList.Builder<Label>().addAll(labels).add(Label.RootLabel.getInstance()).build());
  }

  public boolean isFQDN() {
    return labels.get(labels.size() - 1) instanceof Label.RootLabel;
  }

  public int getLevelSize() {
    return (isFQDN() ? labels.size() - 1 : labels.size());
  }

  public List<Label> getLabels() {
    return labels;
  }

  public Label getTLDLabel() {
    return labels.get(getLevelSize() - 1);
  }

  public String getStringValue() {
    StringBuffer sb = new StringBuffer(labels.get(0).getStringValue());
    for (int i = 1; i < labels.size(); i++) {
      sb.append("." + labels.get(i).getStringValue());
    }
    return sb.toString();
  }

  public Label getLevel(int level) {
    if (isFQDN()) {
      return labels.get(labels.size() - 1 - level);
    }
    return labels.get(labels.size() - level);
  }

  public DomainName toLDH() {
    List<Label> labelList = new ArrayList<Label>();
    for (Label label : getLabels()) {
      labelList.add(label.toLDH());
    }
    return new DomainName(labelList);
  }

  public DomainName toUnicode() {
    List<Label> labelList = new ArrayList<Label>();
    for (Label label : getLabels()) {
      labelList.add(label.toUnicode());
    }
    return new DomainName(labelList);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DomainName)) return false;

    DomainName that = (DomainName) o;

    return this.toLDH().getStringValue().equals(that.toLDH().getStringValue());
  }

  @Override
  public int hashCode() {
    return toLDH().getStringValue().hashCode();
  }
}
