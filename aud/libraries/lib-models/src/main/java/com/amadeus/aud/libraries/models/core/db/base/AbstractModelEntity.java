package com.amadeus.aud.libraries.models.core.db.base;

import com.amadeus.aud.libraries.common.WithId;
import com.amadeus.aud.libraries.models.core.Constants;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.util.Objects;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@MappedSuperclass
public abstract class AbstractModelEntity<T> extends PanacheEntity implements WithId {

  protected String uniqueId;
  protected int hashCode;

  /** The inner data of this object */
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(length = Constants.DB_STRING_LENGTH)
  protected T data;

  protected AbstractModelEntity() {}

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  @Override
  public int hashCode() {
    return hashCode;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    AbstractModelEntity<?> that = (AbstractModelEntity<?>) obj;

    return Objects.equals(id(), that.id()) && Objects.equals(data, that.data);
  }

  public static String normalizeUniqueId(String uniqueId) {
    return uniqueId;
  }

  public static String generateUniqueId(String uniqueId) {
    return normalizeUniqueId(uniqueId);
  }

  public void setUniqueIdFrom(String uniqueId) {
    this.uniqueId = normalizeUniqueId(uniqueId);
    this.hashCode = this.uniqueId != null ? this.uniqueId.hashCode() : 0;
  }

  @Override
  public @Nullable String id() {
    return uniqueId;
  }

  /**
   * @return the unique ID constructed on the base of instance data
   */
  public abstract String generateInstanceUniqueId();
}
