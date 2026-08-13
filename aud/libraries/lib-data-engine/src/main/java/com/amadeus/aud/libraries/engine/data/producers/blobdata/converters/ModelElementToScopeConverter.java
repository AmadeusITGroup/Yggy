package com.amadeus.aud.libraries.engine.data.producers.blobdata.converters;

import com.amadeus.aud.libraries.engine.data.producers.blobdata.base.BaseConverter;
import com.amadeus.aud.libraries.engine.data.producers.blobdata.contexts.ScopeProducerContext;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import com.amadeus.aud.libraries.yaml.models.V1Metadata;
import com.amadeus.aud.libraries.yaml.models.V1Scope;
import com.amadeus.aud.libraries.yaml.models.V1ScopeSpec;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
public class ModelElementToScopeConverter
    extends BaseConverter<ModelElement, Scope, ScopeProducerContext> {

  public BaseConverter<ModelElement, Scope, ScopeProducerContext> init(
      ScopeProducerContext producerContext) {
    super.init(producerContext);
    return this;
  }

  @Override
  public Uni<Collection<Scope>> from(Collection<ModelElement> scopes) {
    // temporarily scopes may not be in the version

    return Uni.createFrom().item(initScopes(scopes));
  }

  Collection<Scope> initScopes(Collection<ModelElement> scopes) {
    if (scopes.isEmpty()) {
      return Set.of();
    }

    ConcurrentMap<String, Scope> scopeCollection = new ConcurrentHashMap<>();
    AtomicReference<Integer> duplicatedCount = new AtomicReference<>(0);

    //    /!\ CODE USED WITH THE INTEGRATION TO MICROSOFT GRAPH. DO NOT REMOVE /!\
    //              Map<String, Set<Scope>> groupAdminNames = new HashMap<>();
    //              Map<String, Set<Scope>> groupGovernanceNames = new HashMap<>();

    scopes.forEach(
        (ModelElement governanceScope) -> {
          Scope scope = createScope(governanceScope);
          scopeCollection.put(scope.id(), scope);
        });

    //    /!\ CODE USED WITH THE INTEGRATION TO MICROSOFT GRAPH. DO NOT REMOVE /!\
    //              CompletionStage<Map<String, Group>> adminGroupsAsync =
    // microsoftGraphService.getGroupsUsingBatchAsync(groupAdminNames.keySet());
    //              CompletionStage<Map<String, Group>> governanceGroupsAsync =
    // microsoftGraphService.getGroupsUsingBatchAsync(groupGovernanceNames.keySet());

    associateReferenceScope(scopeCollection);

    //          /!\ CODE USED WITH THE INTEGRATION TO MICROSOFT GRAPH. DO NOT REMOVE /!\
    //          // Handling the result when it is ready
    //              processGroups(adminGroupsAsync, groupAdminNames,
    // Scope.GroupTypes.NORMAL);
    //              processGroups(
    //                  governanceGroupsAsync, groupGovernanceNames,
    // Scope.GroupTypes.GOVERNANCE);

    duplicatedCount.set(scopes.size() - scopeCollection.size());
    Log.debugf("Completed loading %d Scopes", scopeCollection.size());
    if (duplicatedCount.get() > 0) {
      Log.warnf("Found %d Duplicated Scopes", duplicatedCount.get());
    }
    return scopeCollection.values();
  }

  void associateReferenceScope(Map<String, Scope> scopeCollection) {
    for (Scope scope : scopeCollection.values()) {
      V1Metadata metadata = scope.getData().getMetadata();
      if (metadata != null) {
        String referenceScopeName = Objects.requireNonNullElse(metadata.getScope(), "Governance");
        Scope referenceScope = scopeCollection.get(referenceScopeName);
        if (referenceScope == null) {
          Log.warnf(
              "Scope %s references either null or a non-existing Scope %s",
              scope.id(), referenceScopeName);
        } else {
          scope.setScope(referenceScope);
        }
      }
    }
  }

  protected Scope createScope(ModelElement governanceScope) {

    // Create Scope using the uniqueId instead of passing the blobdata object directly
    // This avoids the ClassLoader casting issue
    Scope scope = new Scope();
    scope.setUniqueIdFrom(governanceScope.getMetadata().getName());
    V1Scope data = new V1Scope();
    scope.setData(data);
    data.setMetadata(governanceScope.getMetadata());
    if (governanceScope.getSpec() instanceof V1ScopeSpec scopeSpec) {
      data.setSpec(scopeSpec);

      scope.setName(governanceScope.getMetadata().getName());
      List<String> owners = scopeSpec.getOwners();
      scope.setOwners(owners != null ? Set.copyOf(owners) : Set.of());
    }

    return scope;
  }
}
