package com.amadeus.aud.services.apis.services.endpoints.v2;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.models.core.db.base.BomElement;
import jakarta.annotation.Nullable;
import java.util.function.Function;

public enum ConnectivityDetailFilter {
  TARGET(ObjectTypeListNamesEnum.TARGETS, ConnectivityDetailFilter::objectName),
  LANDING_ZONE(ObjectTypeListNamesEnum.LANDINGZONES, ConnectivityDetailFilter::objectName),
  SCOPE(ObjectTypeListNamesEnum.SCOPES, ConnectivityDetailFilter::objectName),
  SERVICE(ObjectTypeListNamesEnum.SERVICES, BomElement::id),
  DEPENDENCY(ObjectTypeListNamesEnum.DEPENDENCIES, ConnectivityDetailFilter::objectName),
  DEPENDENCY_INSTANCE(
      ObjectTypeListNamesEnum.DEPENDENCYINSTANCES, ConnectivityDetailFilter::objectName),
  COMPONENT_INSTANCE(
      ObjectTypeListNamesEnum.COMPONENTINSTANCES, ConnectivityDetailFilter::objectName),
  MACRO_PHASE(ObjectTypeListNamesEnum.MACROPHASES, ConnectivityDetailFilter::objectName),
  COMPONENT(ObjectTypeListNamesEnum.COMPONENTS, ConnectivityDetailFilter::objectName),
  NAMESPACE(ObjectTypeListNamesEnum.NAMESPACES, ConnectivityDetailFilter::objectName),
  REGION(ObjectTypeListNamesEnum.REGIONS, ConnectivityDetailFilter::objectName),
  ZONE_TYPE(ObjectTypeListNamesEnum.ZONETYPES, ConnectivityDetailFilter::objectName),
  ZONE_INSTANCE(ObjectTypeListNamesEnum.ZONEINSTANCES, BomElement::id),
  SUBNET_INSTANCE(ObjectTypeListNamesEnum.SUBNETINSTANCES, BomElement::id);

  private final ObjectTypeListNamesEnum objectType;
  private final Function<BomElement, String> valueExtractor;

  ConnectivityDetailFilter(
      ObjectTypeListNamesEnum objectType, Function<BomElement, String> valueExtractor) {
    this.objectType = objectType;
    this.valueExtractor = valueExtractor;
  }

  public ObjectTypeListNamesEnum objectType() {
    return objectType;
  }

  public String repositoryKey() {
    return objectType.getRepositoryKey();
  }

  public String extractValue(BomElement element) {
    return valueExtractor.apply(element);
  }

  public static @Nullable ConnectivityDetailFilter fromObjectTypeUri(String objectTypeUri) {
    for (ConnectivityDetailFilter filter : values()) {
      if (filter.objectType.getUri().equals(objectTypeUri)) {
        return filter;
      }
    }
    return null;
  }

  private static String objectName(BomElement element) {
    return ((AbstractBase<?>) element).getName();
  }
}
