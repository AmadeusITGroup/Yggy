package com.amadeus.aud.libraries.v2.objects.service;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.backend.models.v2.BreakdownCount;
import com.amadeus.aud.libraries.backend.models.v2.InstanceBreakdowns;
import com.amadeus.aud.libraries.backend.models.v2.InstanceRepartition;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.producers.VersionCacheKeyGenerator;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.base.BomElement;
import com.amadeus.aud.libraries.models.core.internals.Direction;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class InstanceRepartitionService {

  @Inject
  public InstanceRepartitionService() {}

  public Uni<InstanceRepartition> getInstanceRepartition(
      Version version, ObjectTypeListNamesEnum objectType, String entityId, String selector) {
    return Uni.createFrom()
        .item(() -> computeInstanceRepartition(version, objectType, entityId, selector))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
  }

  @CacheResult(
      cacheName = "getInstanceRepartitionResponse",
      keyGenerator = VersionCacheKeyGenerator.class)
  public InstanceRepartition computeInstanceRepartition(
      Version version, ObjectTypeListNamesEnum objectType, String entityId, String selector) {
    InstanceRepartitionSelector instanceSelector =
        InstanceRepartitionSelector.fromQueryValue(selector);

    BomElement targetEntity = findTargetEntityById(version, objectType, entityId);
    if (targetEntity == null) {
      throw new NotFoundException("Entity not found: " + entityId);
    }

    return switch (objectType) {
      case COMPONENTS -> computeForComponent((Component) targetEntity, instanceSelector);
      default ->
          throw new BadRequestException(
              "Instance repartition not supported for object type: " + objectType);
    };
  }

  private InstanceRepartition computeForComponent(
      Component component, InstanceRepartitionSelector selector) {
    Set<ComponentInstance> directInstances = component.getComponentInstances();
    if (directInstances == null) {
      directInstances = Set.of();
    }
    Set<ComponentInstance> allInstances = component.getAllComponentInstances();
    if (allInstances == null) {
      allInstances = Set.of();
    }

    Collection<ComponentInstance> instances =
        switch (selector) {
          case DIRECT -> directInstances;
          case BOTH -> allInstances;
          case INDIRECT -> {
            List<ComponentInstance> indirect = new ArrayList<>();
            for (ComponentInstance ci : allInstances) {
              if (!directInstances.contains(ci)) {
                indirect.add(ci);
              }
            }
            yield indirect;
          }
        };

    List<BreakdownCount> macrophase =
        aggregate(
            instances,
            ci ->
                (ci.getLandingZone() != null && ci.getLandingZone().getMacroPhaseName() != null)
                    ? ci.getLandingZone().getMacroPhaseName()
                    : "N/A");

    List<BreakdownCount> securityZone =
        aggregate(
            instances,
            (ComponentInstance ci) -> {
              String sz = ci.getInternalZoneTypeNames(Direction.OUT);
              return (sz == null || sz.isEmpty()) ? "N/A" : sz;
            });

    List<BreakdownCount> continent =
        aggregate(
            instances,
            ci ->
                (ci.getLandingZone() != null
                        && ci.getLandingZone().getTarget() != null
                        && ci.getLandingZone().getTarget().getContinentName() != null)
                    ? ci.getLandingZone().getTarget().getContinentName()
                    : "N/A");

    List<BreakdownCount> target =
        aggregate(
            instances,
            ci ->
                (ci.getLandingZone() != null && ci.getLandingZone().getTargetName() != null)
                    ? ci.getLandingZone().getTargetName()
                    : "N/A");

    List<BreakdownCount> landingZone =
        aggregate(
            instances, ci -> ci.getLandingZoneName() != null ? ci.getLandingZoneName() : "N/A");

    List<BreakdownCount> paas =
        aggregate(
            instances,
            ci ->
                ci.getDeployComponentInstanceName() != null
                    ? ci.getDeployComponentInstanceName()
                    : "N/A");

    List<BreakdownCount> namespace =
        aggregate(
            instances,
            (ComponentInstance ci) -> {
              String ns = ci.getNamespaceName();
              return (ns == null || ns.isEmpty()) ? "N/A" : ns;
            });

    return new InstanceRepartition(
        instances.size(),
        new InstanceBreakdowns(
            continent, landingZone, macrophase, namespace, paas, securityZone, target));
  }

  private static BomElement findTargetEntityById(
      Version version, ObjectTypeListNamesEnum targetObjectType, String entityId) {
    ConcurrentRepository<? extends BomElement> targetRepository =
        version.getRepository(targetObjectType.getRepositoryKey());
    if (targetRepository == null || targetRepository.getData() == null) {
      return null;
    }
    return targetRepository.get(entityId);
  }

  private List<BreakdownCount> aggregate(
      Collection<ComponentInstance> instances,
      java.util.function.Function<ComponentInstance, String> mapper) {
    Map<String, Integer> counts = new LinkedHashMap<>();
    for (ComponentInstance ci : instances) {
      String key = mapper.apply(ci);
      if (key == null || key.isEmpty()) {
        key = "N/A";
      }
      counts.put(key, counts.getOrDefault(key, 0) + 1);
    }
    List<BreakdownCount> result = new ArrayList<>();
    for (Map.Entry<String, Integer> entry : counts.entrySet()) {
      result.add(new BreakdownCount(entry.getKey(), entry.getValue()));
    }
    return result;
  }
}
