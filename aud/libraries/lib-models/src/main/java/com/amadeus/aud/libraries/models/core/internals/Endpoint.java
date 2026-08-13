package com.amadeus.aud.libraries.models.core.internals;

import com.amadeus.aud.libraries.common.Constants;
import com.amadeus.aud.libraries.models.core.utils.IPv4Utils;
import com.google.common.collect.Range;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import java.math.BigInteger;
import java.util.Objects;

@Entity
public class Endpoint extends PanacheEntity {
  private int hashCode;

  private TypeEnum type;
  private String value;
  private String description;
  private Range<BigInteger> cidrRange;

  public Endpoint() {}

  @SuppressWarnings({"this-escape", "java:S1699"})
  public Endpoint(TypeEnum type, String value, String description) {
    this.type = type;
    this.description = description;
    this.setValue(value);
    this.hashCode = Objects.hash(type, value, description);
  }

  /** Return true if this Endpoint object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    return (o instanceof Endpoint endpoint)
        && Objects.equals(this.type, endpoint.type)
        && Objects.equals(this.value, endpoint.value)
        && Objects.equals(this.description, endpoint.description);
  }

  @Override
  public int hashCode() {
    if (this.hashCode == 0) {
      this.hashCode = Objects.hash(type, value, description);
    }
    return this.hashCode;
  }

  @Override
  public String toString() {
    return new StringBuffer()
        .append("{'description': '")
        .append(description)
        .append("', 'type': '")
        .append(type.toString())
        .append("', 'value': '")
        .append(value)
        .append("'}")
        .toString();
  }

  public TypeEnum getType() {
    return type;
  }

  public void setType(TypeEnum type) {
    this.type = type;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    if (Constants.N_A.equals(value)) {
      return;
    }

    this.value = value;
    if (this.type == TypeEnum.CIDR) {
      this.cidrRange = IPv4Utils.toRange(value);
    }
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Range<BigInteger> getCidrRange() {
    return cidrRange;
  }

  public enum TypeEnum {
    CIDR("cidr"),
    URL("url"),
    FQDN("fqdn");

    private final String value;

    TypeEnum(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    public static TypeEnum fromValue(String value) {
      for (TypeEnum b : TypeEnum.values()) {
        if (Objects.equals(b.getValue(), value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }
}
