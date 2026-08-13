package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.Constants;
import com.amadeus.aud.libraries.models.core.db.base.AbstractModelEntity;
import com.amadeus.aud.libraries.models.core.internals.AccessRightsDenialSeverity;
import com.amadeus.aud.libraries.models.core.internals.AccessRightsPermission;
import com.amadeus.aud.libraries.yaml.models.AccessDenialMessage;
import com.amadeus.aud.libraries.yaml.models.AccessRightsEntry;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
public class AccessRightsEntryEntity extends AbstractModelEntity<AccessRightsEntry> {

  @Column(length = Constants.DB_STRING_LENGTH)
  private String permission;

  @Transient AccessRightsPermission permissionEnum;

  @Column(length = Constants.DB_STRING_LENGTH)
  private String denySeverity;

  @Transient AccessRightsDenialSeverity denySeverityEnum;

  @Column(length = Constants.DB_STRING_LENGTH, nullable = true)
  private String denyMessageText;

  @OneToMany(targetEntity = Scope.class, cascade = CascadeType.ALL)
  private Set<Scope> applyToScopes;

  public AccessRightsEntryEntity(AccessRightsEntry accessRightsEntry) {
    super();
    this.data = accessRightsEntry;
    initializeProperties(accessRightsEntry);
  }

  protected AccessRightsEntryEntity() {
    super();
  }

  /**
   * @param adata - YAML object AccessRightsEntry
   */
  private void initializeProperties(AccessRightsEntry adata) {

    this.denyMessageText = null;
    this.denySeverity = AccessDenialMessage.UserMessageSeverityEnum.EMPTY.getValue();
    this.permission = AccessRightsEntry.PermissionEnum.COMPONENT_INHERIT.getValue();

    AccessDenialMessage denyMessage = adata.getReportIfDenied();
    if (denyMessage != null) {
      this.denyMessageText = AccessRightsEntryEntity.fixDenialMessage(denyMessage.getUserMessage());
      this.denySeverity =
          Objects.requireNonNullElse(
                  denyMessage.getUserMessageSeverity(),
                  AccessDenialMessage.UserMessageSeverityEnum.EMPTY)
              .getValue();
    }

    this.permission =
        Objects.requireNonNullElse(
                adata.getPermission(), AccessRightsEntry.PermissionEnum.COMPONENT_INHERIT)
            .getValue();
    this.denySeverityEnum =
        denialSeverityFromDataDenialSeverity(
            AccessDenialMessage.UserMessageSeverityEnum.fromValue(this.denySeverity));
    this.permissionEnum =
        permissionFromDataPermission(AccessRightsEntry.PermissionEnum.fromValue(this.permission));

    String uniqueId =
        commonUniqueId(this.permissionEnum, this.denySeverityEnum, this.denyMessageText);
    this.uniqueId = normalizeUniqueId(uniqueId);
    this.hashCode = this.uniqueId != null ? this.uniqueId.hashCode() : 0;
  }

  public @Nullable String getDenyMessageText() {
    return denyMessageText;
  }

  public AccessRightsDenialSeverity getDenySeverity() {
    return denySeverityEnum;
  }

  public AccessRightsPermission getPermission() {
    return permissionEnum;
  }

  public final Set<Scope> getApplyToScopes() {
    return applyToScopes != null ? Set.copyOf(applyToScopes) : Set.of();
  }

  public void addApplyToScopes(Collection<Scope> scopes) {
    if (this.applyToScopes == null) {
      this.applyToScopes = new LinkedHashSet<>();
    }
    this.applyToScopes.addAll(scopes);
    setUniqueIdFrom(generateInstanceUniqueId());
  }

  /*
   * Prerequisite : initParameters run and sets filled with entities
   * @see com.amadeus.aud.libraries.models.core.db.base.ModelEntity
   * */
  @Override
  public String generateInstanceUniqueId() {

    StringBuilder uniqueIdBuilder =
        new StringBuilder(commonUniqueId(getPermission(), getDenySeverity(), getDenyMessageText()));

    for (Scope scope : getApplyToScopes()) {
      uniqueIdBuilder
          .append(scope.getName())
          .append(Character.toString(Character.PARAGRAPH_SEPARATOR));
    }
    uniqueIdBuilder.append(Character.toString(Character.SPACE_SEPARATOR));

    return uniqueIdBuilder.toString();
  }

  static @Nullable String fixDenialMessage(@Nullable String text) {

    final String placeholderEdgeOld = "{edge}";
    final String placeholderEdgeNew = "$" + placeholderEdgeOld;
    final String placeholderCobjOld = "{checked_object}";
    final String placeholderCobjNew = "$" + placeholderCobjOld;

    if (text != null) {
      text =
          text.replaceAll(
                  Pattern.quote(placeholderEdgeNew), Matcher.quoteReplacement(placeholderEdgeOld))
              .replaceAll(
                  Pattern.quote(placeholderEdgeOld), Matcher.quoteReplacement(placeholderEdgeNew));
      text =
          text.replaceAll(
                  Pattern.quote(placeholderCobjNew), Matcher.quoteReplacement(placeholderCobjOld))
              .replaceAll(
                  Pattern.quote(placeholderCobjOld), Matcher.quoteReplacement(placeholderCobjNew));
    }

    return text;
  }

  public static AccessRightsDenialSeverity denialSeverityFromDataDenialSeverity(
      AccessDenialMessage.UserMessageSeverityEnum denialSeverityEnum) {

    return switch (denialSeverityEnum) {
      case ERROR -> AccessRightsDenialSeverity.ERROR;
      case WARNING -> AccessRightsDenialSeverity.WARNING;
      default -> AccessRightsDenialSeverity.EMPTY;
    };
  }

  public static AccessRightsPermission permissionFromDataPermission(
      AccessRightsEntry.PermissionEnum dataPermissionEnum) {
    return switch (dataPermissionEnum) {
      case AccessRightsEntry.PermissionEnum.COMPONENT_DEPENDENCY_SRC ->
          AccessRightsPermission.COMPONENT_DEPENDENCY_SRC;
      case AccessRightsEntry.PermissionEnum.COMPONENT_DEPENDENCY_DST ->
          AccessRightsPermission.COMPONENT_DEPENDENCY_DST;
      default -> AccessRightsPermission.COMPONENT_INHERIT;
    };
  }

  private static String commonUniqueId(
      final AccessRightsPermission permission,
      final AccessRightsDenialSeverity denySeverity,
      final @Nullable String denyText) {

    StringBuilder uniqueIdBuilder = new StringBuilder();
    uniqueIdBuilder
        .append(permission)
        .append(Character.toString(Character.PARAGRAPH_SEPARATOR))
        .append(denySeverity)
        .append(Character.toString(Character.PARAGRAPH_SEPARATOR))
        .append(denyText)
        .append(Character.toString(Character.SPACE_SEPARATOR));
    return uniqueIdBuilder.toString();
  }
}
