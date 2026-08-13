package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.Constants;
import com.amadeus.aud.libraries.models.core.db.base.AbstractModelEntity;
import com.amadeus.aud.libraries.models.core.internals.AccessRightsDenialSeverity;
import com.amadeus.aud.libraries.yaml.models.AccessControl;
import com.amadeus.aud.libraries.yaml.models.AccessControlDeny;
import com.amadeus.aud.libraries.yaml.models.AccessDenialMessage;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Access Control entity for managing component inheritance and dependency access rules.
 *
 * <p>Implements BomElement to allow loading via bom.list(AccessControl.class, domain).
 *
 * <p>Note: This is a simplified implementation. The full YAML model integration requires updates to
 * the schema to make AccessControl extend V1Base.
 */
@Entity
public class AccessControlEntity extends AbstractModelEntity<AccessControl> {

  @Column(length = Constants.DB_STRING_LENGTH, nullable = true)
  private String globalDenyMessageText;

  @Column(length = Constants.DB_STRING_LENGTH, nullable = false)
  private String globalDenySeverity;

  @Transient private AccessRightsDenialSeverity globalDenySeverityEnum;

  @OneToMany(targetEntity = AccessRightsEntryEntity.class, cascade = CascadeType.ALL)
  private Set<AccessRightsEntryEntity> denyAccessRights;

  @OneToMany(targetEntity = AccessRightsEntryEntity.class, cascade = CascadeType.ALL)
  private Set<AccessRightsEntryEntity> allowAccessRights;

  @OneToMany(mappedBy = "access_control")
  private Set<Scope> controlledScopes;

  @OneToMany(mappedBy = "access_control")
  private Set<Component> controlledComponents;

  @Transient private AccessControlDeny deny;

  public AccessControlEntity(AccessControl accessControl) {
    super();
    this.data = accessControl;
    initializeProperties(accessControl);
  }

  protected AccessControlEntity() {
    super();
  }

  /**
   * @param adata corresponding yaml model entity
   */
  private void initializeProperties(AccessControl adata) {

    this.globalDenyMessageText = null;
    this.globalDenySeverity = AccessDenialMessage.UserMessageSeverityEnum.EMPTY.getValue();

    deny = adata.getDeny();
    if (deny != null && deny.getActualInstance() instanceof AccessDenialMessage) {
      AccessDenialMessage globalDenyMessage = deny.getAccessDenialMessage();
      if (globalDenyMessage != null && globalDenyMessage.getUserMessageSeverity() != null) {
        this.globalDenyMessageText = globalDenyMessage.getUserMessage();
        this.globalDenySeverity = globalDenyMessage.getUserMessageSeverity().getValue();
      }
    }
    this.globalDenySeverityEnum =
        AccessRightsEntryEntity.denialSeverityFromDataDenialSeverity(
            AccessDenialMessage.UserMessageSeverityEnum.fromValue(this.globalDenySeverity));

    String uniqueId = commonUniqueId(this.globalDenySeverityEnum, this.globalDenyMessageText);
    this.uniqueId = normalizeUniqueId(uniqueId);
    this.hashCode = this.uniqueId != null ? this.uniqueId.hashCode() : 0;
  }

  public @Nullable String getGlobalDenyMessageText() {
    return globalDenyMessageText;
  }

  public AccessRightsDenialSeverity getGlobalDenySeverity() {
    return globalDenySeverityEnum;
  }

  public void setGlobalDenyMessageText(@Nullable String globalDenyText) {

    this.globalDenyMessageText =
        Objects.requireNonNullElse(AccessRightsEntryEntity.fixDenialMessage(globalDenyText), "");
    setUniqueIdFrom(generateInstanceUniqueId());
  }

  public void setGlobalDenySeverity(@Nullable AccessRightsDenialSeverity globalDenyEnum) {

    this.globalDenySeverityEnum =
        Objects.requireNonNullElse(globalDenyEnum, AccessRightsDenialSeverity.EMPTY);
    this.globalDenySeverity = globalDenySeverityEnum.getValue();
    setUniqueIdFrom(generateInstanceUniqueId());
  }

  public void addDenyAccessRights(Set<AccessRightsEntryEntity> denyAccessRights) {
    if (this.denyAccessRights == null) {
      this.denyAccessRights = new LinkedHashSet<>();
    }
    this.denyAccessRights.addAll(denyAccessRights);
    setUniqueIdFrom(generateInstanceUniqueId());
  }

  public void addAllowAccessRights(Set<AccessRightsEntryEntity> allowRights) {
    if (this.allowAccessRights == null) {
      this.allowAccessRights = new LinkedHashSet<>();
    }
    this.allowAccessRights.addAll(allowRights);
    setUniqueIdFrom(generateInstanceUniqueId());
  }

  public final Set<AccessRightsEntryEntity> getDenyAccessRights() {
    return denyAccessRights != null ? Set.copyOf(denyAccessRights) : Set.of();
  }

  public final Set<AccessRightsEntryEntity> getAllowAccessRights() {
    return allowAccessRights != null ? Set.copyOf(allowAccessRights) : Set.of();
  }

  public final Set<Scope> getControlledScopes() {
    return controlledScopes != null ? Set.copyOf(controlledScopes) : Set.of();
  }

  public final Set<Component> getControlledComponents() {
    return controlledComponents != null ? Set.copyOf(controlledComponents) : Set.of();
  }

  public void addControlledComponent(Component component) {
    addControlledComponents(Set.of(component));
  }

  public void addControlledComponents(Collection<Component> components) {
    if (this.controlledComponents == null) {
      this.controlledComponents = new LinkedHashSet<>();
    }
    this.controlledComponents.addAll(components);
  }

  public void addControlledScope(Scope scope) {
    addControlledScopes(Set.of(scope));
  }

  public void addControlledScopes(Collection<Scope> scopes) {
    if (this.controlledScopes == null) {
      this.controlledScopes = new LinkedHashSet<>();
    }
    this.controlledScopes.addAll(scopes);
  }

  /*
   * Prerequisite : initParameters run and sets filled with entitities
   * @see com.amadeus.aud.libraries.models.core.db.base.ModelEntity
   * */
  public String generateInstanceUniqueId() {

    StringBuilder uniqueIdBuilder =
        new StringBuilder(commonUniqueId(getGlobalDenySeverity(), getGlobalDenyMessageText()));

    if (!getDenyAccessRights().isEmpty()) {
      for (AccessRightsEntryEntity entry : getDenyAccessRights()) {
        entry.setUniqueIdFrom(entry.generateInstanceUniqueId());
        uniqueIdBuilder.append(entry.id());
      }
    }
    uniqueIdBuilder.append(Character.toString(Character.LINE_SEPARATOR));

    if (!getAllowAccessRights().isEmpty()) {
      for (AccessRightsEntryEntity entry : getAllowAccessRights()) {
        entry.setUniqueIdFrom(entry.generateInstanceUniqueId());
        uniqueIdBuilder.append(entry.id());
      }
    }
    uniqueIdBuilder.append(Character.toString(Character.LINE_SEPARATOR));

    return uniqueIdBuilder.toString();
  }

  private static String commonUniqueId(
      final AccessRightsDenialSeverity denySeverity, final @Nullable String denyText) {

    StringBuilder uniqueIdBuilder =
        new StringBuilder()
            .append(denySeverity)
            .append(Character.toString(Character.PARAGRAPH_SEPARATOR))
            .append(denyText)
            .append(Character.toString(Character.LINE_SEPARATOR));
    return uniqueIdBuilder.toString();
  }
}
