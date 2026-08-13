package com.amadeus.aud.libraries.models.core.db.base;

import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import com.amadeus.aud.libraries.yaml.models.V1MetadataInstance;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@MappedSuperclass
public abstract class BaseInstance<T extends ModelElement> extends AbstractBase<T> {
  public static final String ID_SEPARATOR = "***";

  private LocalDateTime creationDate;

  private LocalDateTime updateDate;

  private String creationVersion;

  private String updateVersion;

  @ManyToOne(targetEntity = LandingZone.class, cascade = CascadeType.ALL)
  private LandingZone landingZone;

  @SuppressWarnings("this-escape")
  protected BaseInstance() {
    super();
    initializeProperties();
  }

  @SuppressWarnings("this-escape")
  protected BaseInstance(T data, String uniqueId) {
    super(data, uniqueId);
    initializeProperties();
  }

  @SuppressWarnings("this-escape")
  protected BaseInstance(T data, String objectName, String landingZoneName) {
    super(data, generateUniqueId(landingZoneName, objectName, false));
    initializeProperties();
  }

  private void initializeProperties() {
    Object metadataObject = this.getMetadata();
    if (metadataObject instanceof V1MetadataInstance metadata) {
      this.creationDate = metadata.getCreationDate();
      this.updateDate = metadata.getUpdateDate();
      this.creationVersion = metadata.getCreationVersion();
      this.updateVersion = metadata.getUpdateVersion();
    }
  }

  public LocalDateTime getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(LocalDateTime creationDate) {
    this.creationDate = creationDate;
  }

  public LocalDateTime getUpdateDate() {
    return updateDate;
  }

  public void setUpdateDate(LocalDateTime updateDate) {
    this.updateDate = updateDate;
  }

  public String getCreationVersion() {
    return creationVersion;
  }

  public void setCreationVersion(String creationVersion) {
    this.creationVersion = creationVersion;
  }

  public String getUpdateVersion() {
    return updateVersion;
  }

  public void setUpdateVersion(String updateVersion) {
    this.updateVersion = updateVersion;
  }

  public LandingZone getLandingZone() {
    return landingZone;
  }

  public void setLandingZone(LandingZone landingZone) {
    this.landingZone = landingZone;
  }

  public @Nullable String getLandingZoneName() {
    if (landingZone == null) {
      return null;
    }
    return landingZone.getName();
  }

  private static String generateUniqueId(
      String landingZoneName, String objectName, boolean normalize) {
    String uniqueId =
        String.join(
            ID_SEPARATOR,
            Objects.requireNonNullElse(landingZoneName, "!!NULL!!"),
            Objects.requireNonNullElse(objectName, "!!NULL!!"));
    return normalize ? normalizeUniqueId(uniqueId) : uniqueId;
  }

  public static String generateUniqueId(String landingZoneName, String objectName) {
    return generateUniqueId(landingZoneName, objectName, true);
  }

  @Override
  public Map<String, String> getSimpleJsonRepresentation() {
    Map<String, String> result = super.getSimpleJsonRepresentation();
    result.put("landing_zone", getLandingZoneName());
    LocalDateTime cDate = getCreationDate();
    result.put("creation_date", cDate == null ? null : cDate.toString());
    result.put("creation_version", getCreationVersion());
    LocalDateTime uDate = getUpdateDate();
    result.put("update_date", uDate == null ? null : uDate.toString());
    result.put("update_version", getUpdateVersion());
    return result;
  }
}
