package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.Service;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Type;

@Type("ServiceDTO")
public record ServiceGraphQL(
    String id,
    String type,
    String name,
    String description,
    ScopeGraphQL scope,
    String componentName,
    String componentDeprecatedStatus,
    String applicationIdName,
    String serviceNowId,
    String protocol,
    String ports,
    String approvedProtocol,
    String deprecatedStatus,
    String deprecatedMessage,
    boolean isPrivate,
    boolean isPci,
    boolean isPii,
    boolean isEncrypted,
    boolean isDbSynchro) {
  public static @Nullable ServiceGraphQL from(@Nullable Service s) {
    if (s == null) {
      return null;
    }

    Component component = s.getComponent();

    return new ServiceGraphQL(
        s.id(),
        ObjectTypeListNamesEnum.SERVICES.getUri(),
        s.getName(),
        s.getComment(),
        ScopeGraphQL.from(s.getScope()),
        s.getComponentName(),
        component != null && component.getDeprecatedStatus() != null
            ? component.getDeprecatedStatus().name()
            : null,
        s.getApplicationIdName(),
        s.getServiceNowId(),
        s.getProtocol() != null ? s.getProtocol().getValue() : null,
        s.getPorts(),
        s.getApprovedProtocol() != null ? s.getApprovedProtocol().getValue() : null,
        s.getDeprecatedStatus() != null ? s.getDeprecatedStatus().getValue() : null,
        s.getDeprecatedMessage(),
        s.isPrivate(),
        s.isPci(),
        s.isPii(),
        s.isEncrypted(),
        s.isDbSynchro());
  }
}
