package com.amadeus.aud.libraries.audopen.service;

import com.amadeus.aud.libraries.yaml.models.ObjectGroup;
import com.amadeus.aud.libraries.yaml.models.ObjectGroupExtract;

public final class ObjectGroupExtractUtils {

  private ObjectGroupExtractUtils() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static ObjectGroupExtract toObjectGroupExtract(ObjectGroup objectGroup) {
    ObjectGroupExtract objectGroupExtract = new ObjectGroupExtract();
    objectGroupExtract.setName(objectGroup.getName());
    objectGroupExtract.setUid(objectGroup.getUid());
    objectGroupExtract.setZoneType(objectGroup.getZoneType());
    objectGroupExtract.setType(
        ObjectGroupExtract.TypeEnum.fromValue(objectGroup.getType().getValue()));
    objectGroupExtract.setEndpoints(objectGroup.getEndpoints());

    return objectGroupExtract;
  }
}
