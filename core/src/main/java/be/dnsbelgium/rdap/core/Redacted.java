package be.dnsbelgium.rdap.core;

import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.text.CaseUtils;
import org.apache.commons.text.WordUtils;

public class Redacted {

  private final Name name;
  private final String prePath;
  private final String postPath;
  private final String replacementPath;
  private final PathLang pathLang;
  private final Method method;
  private final Reason reason;

  public Redacted(Name name,
                  String prePath,
                  String postPath,
                  String replacementPath,
                  PathLang pathLang,
                  Method method,
                  Reason reason) {
    this.name = name;
    this.prePath = prePath;
    this.postPath = postPath;
    this.replacementPath = replacementPath;
    this.pathLang = pathLang;
    this.method = method;
    this.reason = reason;
  }

  public Redacted(Name name) {
    this(name, null, null, null, null, null, null);
  }

  public Name getName() {
    return name;
  }

  public String getPrePath() {
    return prePath;
  }

  public String getPostPath() {
    return  postPath;
  }

  public String getReplacementPath() {
    return replacementPath;
  }

  public PathLang getPathLang() {
    return pathLang;
  }

  public Method getMethod() {
    return method;
  }

  public Reason getReason() {
    return reason;
  }

  public static class Name {

    private final Type type;
    private final String description;

    public static Name fromType(Type type) {
      return new Name(type, null);
    }

    public static Name fromDescription(String description) {
      return new Name(null, description);
    }

    private Name(Type type, String description) {
      this.type = type;
      this.description = description;
    }

    public Type getType() {
      return type;
    }

    public String getDescription() {
      return description;
    }

    public interface Type {
      @JsonValue
      String getValue();

      static Type of(String value) {
        return () -> value;
      }

      enum Default implements Type {
        REGISTRY_DOMAIN_ID("Registry Domain ID"),
        REGISTRY_REGISTRANT_ID("Registry Registrant ID"),
        REGISTRANT_NAME,
        REGISTRANT_ORGANIZATION,
        REGISTRANT_STREET,
        REGISTRANT_CITY,
        REGISTRANT_POSTAL_CODE,
        REGISTRANT_PHONE,
        REGISTRANT_PHONE_EXT,
        REGISTRANT_FAX,
        REGISTRANT_FAX_EXT,
        REGISTRANT_EMAIL,
        REGISTRY_TECH_ID("Registry Tech ID"),
        TECH_NAME,
        TECH_PHONE,
        TECH_PHONE_EXT,
        TECH_EMAIL;

        private final String value;

        Default() {
          this.value = WordUtils.capitalizeFully(name().replace('_', ' '));
        }

        Default(String value) {
          this.value = value;
        }

        @Override
        public String getValue() {
          return value;
        }
      }
    }

  }

  public interface PathLang {
    @JsonValue
    String getValue();

    static PathLang of(String value) {
      return () -> value;
    }

    PathLang JSON_PATH = PathLang.of("jsonpath");
  }

  public enum Method {
    REMOVAL, EMPTY_VALUE, PARTIAL_VALUE, REPLACEMENT_VALUE;

    @JsonValue
    public String getValue() {
      return CaseUtils.toCamelCase(name(), false, '_');
    }
  }

  public static class Reason {
    private final Type type;
    private final String description;

    public static Reason fromType(Type type) {
      return new Reason(type, null);
    }

    public static Reason fromDescription(String description) {
      return new Reason(null, description);
    }

    private Reason(Type type, String description) {
      this.type = type;
      this.description = description;
    }

    public Type getType() {
      return type;
    }

    public String getDescription() {
      return description;
    }

    public interface Type {
      @JsonValue
      String getValue();

      static Type of(String value) {
        return () -> value;
      }

      // No defaults were defined yet
    }
  }

  public static class Builder {
    private final Name name;
    private String prePath;
    private String postPath;
    private String replacementPath;
    private PathLang pathLang;
    private Method method;
    private Reason reason;

    public Builder(Name name) {
      this.name = name;
    }

    public static Builder withName(Name name) {
      return new Builder(name);
    }

    public Builder withPrePath(String prePath) {
      this.prePath = prePath;
      return this;
    }

    public Builder withPostPath(String postPath) {
      this.postPath = postPath;
      return this;
    }

    public Builder withReplacementPath(String replacementPath) {
      this.replacementPath = replacementPath;
      return this;
    }

    public Builder withPathLang(PathLang pathLang) {
      this.pathLang = pathLang;
      return this;
    }

    public Builder withMethod(Method method) {
      this.method = method;
      return this;
    }

    public Builder withReason(Reason reason) {
      this.reason = reason;
      return this;
    }

    public Redacted build() {
      return new Redacted(name, prePath, postPath, replacementPath, pathLang, method, reason);
    }
  }

}
