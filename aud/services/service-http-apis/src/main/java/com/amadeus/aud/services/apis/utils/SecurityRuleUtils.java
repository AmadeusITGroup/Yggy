package com.amadeus.aud.services.apis.utils;

import com.amadeus.aud.libraries.engine.graph.models.dto.Edge;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.models.core.internals.Direction;
import com.amadeus.aud.libraries.models.core.utils.PortUtils;
import com.amadeus.aud.libraries.models.core.utils.StringUtils;
import com.amadeus.aud.libraries.yaml.models.ObjectGroup;
import com.amadeus.aud.libraries.yaml.models.SecurityRule;

public final class SecurityRuleUtils {
  public static final String DEFAULT_UID = "uid";

  private SecurityRuleUtils() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static SecurityRule createSecurityRule(
      DependencyInstance dependencyInstance,
      Edge edge,
      Service service,
      ComponentInstance sourceInstance,
      ComponentInstance destinationInstance) {
    String securityRuleName = buildSecurityRuleName(edge, service);
    ObjectGroup srcObjectGroup = sourceInstance.getObjectGroups(false).get(Direction.OUT);
    ObjectGroup dstObjectGroup = destinationInstance.getObjectGroups(false).get(Direction.IN);

    SecurityRule securityRule = new SecurityRule();
    securityRule.setName(securityRuleName);
    securityRule.setUid(DEFAULT_UID);
    securityRule.setScope(dependencyInstance.getScopeName());
    securityRule.setOwner(dependencyInstance.getScope().getAssigneeGroup());
    securityRule.setPorts(PortUtils.toStrings(service.getPortsRange()));
    securityRule.setProtocol(SecurityRule.ProtocolEnum.valueOf(service.getProtocol().name()));
    securityRule.setSrcObjectGroup(ObjectGroupExtractUtils.toObjectGroupExtract(srcObjectGroup));
    securityRule.setDstObjectGroup(ObjectGroupExtractUtils.toObjectGroupExtract(dstObjectGroup));
    securityRule.setDependencyInstanceName(dependencyInstance.getName());
    securityRule.setDependencyInstanceHash(dependencyInstance.getURLHash());
    securityRule.setDependencyInstanceId(dependencyInstance.id());

    return securityRule;
  }

  private static String buildSecurityRuleName(Edge edge, Service service) {
    return new StringBuilder()
        .append("ar-from-")
        .append(StringUtils.normalize(edge.getSourceNodeId()))
        .append("-to-")
        .append(StringUtils.normalize(edge.getTargetNodeId()))
        .append("-")
        .append(StringUtils.normalize(service.getName()))
        .toString();
  }
}
