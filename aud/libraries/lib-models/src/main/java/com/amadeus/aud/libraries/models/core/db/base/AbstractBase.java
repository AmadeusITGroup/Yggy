package com.amadeus.aud.libraries.models.core.db.base;

import com.amadeus.aud.libraries.models.core.Constants;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.utils.HashUtils;
import com.amadeus.aud.libraries.models.core.utils.VersionUtils;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import com.amadeus.aud.libraries.yaml.models.V1Metadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.logging.Log;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@MappedSuperclass
public abstract class AbstractBase<T extends ModelElement> extends AbstractModelEntity<T>
    implements BomElement {
  private static final ObjectMapper mapper = new ObjectMapper();

  /** The scope that the security area belongs to */
  @ManyToOne(targetEntity = Scope.class, cascade = CascadeType.ALL)
  @JoinColumn(name = "scope_id")
  private Scope scope;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(length = Constants.DB_STRING_LENGTH)
  private Set<Map<String, String>> tags;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(length = Constants.DB_STRING_LENGTH)
  private Map<String, String> tagsAsMap;

  private String name;

  @Column(length = Constants.DB_STRING_LENGTH)
  private String comment;

  @Column(length = Constants.DB_STRING_LENGTH)
  private String providerSpecific;

  @Column(length = Constants.DB_STRING_LENGTH)
  private String securityJustification;

  /**
   * Metadata property can be either a V1Metadata object or a V1MetadataInstance object. It is a
   * transient field that is not persisted in the database.
   */
  @Transient private V1Metadata metadata;

  private long version;

  protected AbstractBase() {}

  protected AbstractBase(String uniqueId) {
    Objects.requireNonNull(uniqueId, "uniqueId cannot be null");
    this.uniqueId = normalizeUniqueId(uniqueId);
    this.hashCode = this.uniqueId.hashCode();
  }

  protected AbstractBase(String uniqueId, Scope scope) {
    this(uniqueId);
    Objects.requireNonNull(scope, "scope cannot be null");
    this.scope = scope;
  }

  protected AbstractBase(T data, String uniqueId) {
    this(uniqueId);
    Objects.requireNonNull(data, "data cannot be null");
    this.data = data;
    initializeProperties(data);
  }

  protected AbstractBase(T data, String uniqueId, Scope scope) {
    this(uniqueId, scope);
    Objects.requireNonNull(data, "data cannot be null");
    this.data = data;
    initializeProperties(data);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    AbstractBase<?> that = (AbstractBase<?>) obj;

    return Objects.equals(id(), that.id())
        && Objects.equals(data, that.data)
        && Objects.equals(name, that.name)
        && Objects.equals(comment, that.comment);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  public String getURLHash() {
    try {
      return HashUtils.calculateMD5Hash(getName());
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalArgumentException("MD5 algorithm not found", e);
    }
  }

  @Override
  public Scope getScope() {
    return scope;
  }

  /**
   * @param data
   */
  @Override
  public void setData(T data) {
    super.setData(data);
    initializeProperties(data);
  }

  public final void setScope(Scope scope) {
    this.scope = scope;
  }

  public @Nullable String getScopeName() {
    if (scope == null) {
      return null;
    }
    return scope.getName();
  }

  public String getComment() {
    return this.comment;
  }

  public final void setComment(String comment) {
    this.comment = comment;
  }

  public String getName() {
    return this.name;
  }

  public final void setName(String name) {
    this.name = name;
  }

  public void setSecurityJustification(String securityJustification) {
    this.securityJustification = securityJustification;
  }

  public @Nullable String getSecurityJustification() {
    return this.securityJustification;
  }

  public String getProviderSpecific() {
    return this.providerSpecific;
  }

  public @Nullable Map<String, Object> getProviderSpecificAsMap() {
    if (this.providerSpecific == null || this.providerSpecific.isEmpty()) {
      return null;
    }
    try {
      return mapper.readValue(this.providerSpecific, new TypeReference<>() {});
    } catch (JsonProcessingException e) {
      Log.warnf(
          "Failed to parse provider specific data: %s. Error: %s",
          this.providerSpecific, e.getMessage());
    }
    return null;
  }

  public static @Nullable Map<String, Object> deepMergeProviderSpecific(
      @Nullable String base, @Nullable String override) {
    boolean hasBase = base != null && !base.isEmpty();
    boolean hasOverride = override != null && !override.isEmpty();
    if (!hasBase && !hasOverride) {
      return null;
    }
    try {
      ObjectNode merged = hasBase ? (ObjectNode) mapper.readTree(base) : mapper.createObjectNode();
      if (hasOverride) {
        ObjectNode overrideNode = (ObjectNode) mapper.readTree(override);
        merged.setAll(overrideNode);
      }
      Map<String, Object> result = mapper.convertValue(merged, new TypeReference<>() {});
      return result.isEmpty() ? null : result;
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(
          "Invalid JSON in provider_specific. base=" + base + ", override=" + override, e);
    }
  }

  private void initializeProperties(T data) {

    metadata = data.getMetadata();

    if (metadata instanceof V1Metadata v1Metadata) {
      Map<String, String> labels = v1Metadata.getLabels();
      if (labels != null) {
        // We suspect that Jackson is reusing some object, so we need to create a new object
        // In the past, the labels were changed outside the normal business flow.
        this.tagsAsMap = new HashMap<>(labels);
      }
      this.tags = parseLabels(this.tagsAsMap);
      this.name = v1Metadata.getName();
      this.comment = v1Metadata.getComment();
      this.providerSpecific = v1Metadata.getProviderSpecific();
    }
  }

  private static Set<Map<String, String>> parseLabels(Map<String, String> labels) {
    Set<Map<String, String>> result = new HashSet<>();
    if (labels != null) {
      for (Map.Entry<String, String> entry : labels.entrySet()) {
        String key = entry.getKey();
        String value = entry.getValue();
        if (key == null || value == null) {
          continue;
        }

        result.add(
            Map.of("key", key, "value", value, "is_inherited", "false", "is_generated", "false"));
      }
    }
    return result;
  }

  public String getType() {
    return getClass().getSimpleName().toLowerCase(Locale.ROOT);
  }

  public Map<String, String> getTagsAsMap() {
    return tagsAsMap;
  }

  public Set<Map<String, String>> getTags() {
    return tags;
  }

  public void setTags(Set<Map<String, String>> tags) {
    this.tags = tags;
  }

  public void addTag(Map<String, String> tag) {
    if (tags == null) {
      tags = new HashSet<>();
    }
    tags.add(tag);

    String key = tag.get("key");
    if (key != null) {
      if (tagsAsMap == null) {
        tagsAsMap = new HashMap<>();
      }
      tagsAsMap.put(key, tag.get("value"));
    }
  }

  public void addTags(Iterable<Map<String, String>> tags) {
    for (Map<String, String> tag : tags) {
      addTag(tag);
    }
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

  public void setVersion(String version) {
    this.version = VersionUtils.toLong(version);
  }

  public String getReadableVersion() {
    return VersionUtils.toString(this.version);
  }

  public Map<String, String> getSimpleJsonRepresentation() {
    Map<String, String> result = new HashMap<>();
    result.put("type", getType());
    result.put("name", getName());
    return result;
  }

  public static <R extends AbstractBase<?>>
      @Nullable List<Map<String, String>> getSimpleJsonRepresentation(@Nullable Set<R> set) {
    if (set == null) {
      return null;
    }

    List<Map<String, String>> result = new ArrayList<>();
    for (R t : set) {
      result.add(t.getSimpleJsonRepresentation());
    }
    return result;
  }

  public final V1Metadata getMetadata() {
    return metadata;
  }

  /**
   * @see AbstractModelEntity
   */
  @Override
  public String generateInstanceUniqueId() {
    return getName();
  }
}
