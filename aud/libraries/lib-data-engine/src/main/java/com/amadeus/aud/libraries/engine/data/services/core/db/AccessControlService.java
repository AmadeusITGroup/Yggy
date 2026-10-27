package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.AccessControlEntitiesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.AccessRightsEntryEntitiesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopesRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseModelEntityService;
import com.amadeus.aud.libraries.models.core.db.AccessControlEntity;
import com.amadeus.aud.libraries.models.core.db.AccessRightsEntryEntity;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.base.AbstractModelEntity;
import com.amadeus.aud.libraries.yaml.models.AccessControl;
import com.amadeus.aud.libraries.yaml.models.AccessControlAllow;
import com.amadeus.aud.libraries.yaml.models.AccessControlDeny;
import com.amadeus.aud.libraries.yaml.models.AccessControlDenyAnyOf;
import com.amadeus.aud.libraries.yaml.models.AccessRightsEntry;
import com.amadeus.aud.libraries.yaml.models.AccessRightsEntryScopes;
import com.amadeus.aud.libraries.yaml.models.V1Component;
import io.quarkus.logging.Log;
import jakarta.inject.Singleton;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

@Singleton
public class AccessControlService
    extends BaseModelEntityService<AccessControlEntity, AccessControl> {

  @Override
  protected ConcurrentRepository<AccessControlEntity> getRepository(Version version) {
    return Objects.requireNonNullElseGet(
        version.getAccessControlEntitiesRepository(),
        new AccessControlEntitiesRepositorySupplier(version));
  }

  protected ConcurrentRepository<AccessRightsEntryEntity> getAccessRightsRepository(
      Version version) {
    return Objects.requireNonNullElseGet(
        version.getAccessRightsEntryEntitiesRepository(),
        new AccessRightsEntriesRepositorySupplier(version));
  }

  @Override
  public void init(Version version) {
    init(version, AccessControlEntity.class, AccessControl.class);
  }

  @Override
  protected void init(
      Version version, Class<AccessControlEntity> toClazz, Class<AccessControl> fromClazz) {

    AudDataRepository dataRepository = version.getAudDataRepository();
    if (dataRepository == null) {
      Log.errorf("Data Repository not found for version %s", version);
      return;
    }
    @SuppressWarnings("unchecked")
    Collection<V1Component> edgeList =
        (Collection<V1Component>) dataRepository.getData().get(V1Component.class);

    initFromComponents(version, Objects.requireNonNullElse(edgeList, List.of()));
  }

  private void initFromComponents(Version version, Collection<V1Component> edgeList) {

    if (edgeList.isEmpty()) {
      return;
    }

    ConcurrentRepository<Component> componentRepository = version.getComponentsRepository();

    ConcurrentRepository<AccessControlEntity> accessControlRepository = getRepository(version);

    for (V1Component edge : edgeList) {

      AccessControl accessControl =
          Objects.requireNonNull(
                  edge.getSpec(), "initFromComponents : panic! Component without spec!")
              .getAccessControl();
      if (accessControl == null) {
        continue;
      }
      Component component =
          componentRepository.get(
              AbstractModelEntity.generateUniqueId(edge.getMetadata().getName()));

      logModelDebug(
          "AccessControlService.initFromComponents : ACL found on component : %s!",
          String.valueOf(component != null ? component.getMetadata().getName() : null));

      getAccessRightsRepository(version);
      AccessControlEntity aclEntity = new AccessControlEntity(accessControl);
      initAccessRightsEntries(version, aclEntity);

      String aclEntityUniqId = aclEntity.id();
      logModelDebug(
          "AccessControlService.initFromComponents : local ACL entity : uniqueID:\n\n%s;\nin repository (%d): %b!",
          String.valueOf(aclEntityUniqId),
          accessControlRepository.getDataSize(),
          (accessControlRepository.get(aclEntityUniqId) != null));

      AccessControlEntity aclEntityInRepository =
          Objects.requireNonNullElseGet(
              accessControlRepository.get(aclEntityUniqId),
              new AccessControlEntitySupplier(
                  version, accessControlRepository, aclEntityUniqId, aclEntity));

      logModelDebug(
          "AccessControlService.initFromComponents : repos ACL entity : uniqueID\n\n: %s;\nin repository (%d): %b;\n%s!",
          String.valueOf(aclEntityInRepository.id()),
          accessControlRepository.getDataSize(),
          (version.getAccessControlEntitiesRepository().get(aclEntityInRepository.id()) != null),
          String.valueOf(aclEntityInRepository.getData().toString()));

      aclEntityInRepository.addControlledComponent(component);
    }
  }

  private static void initAccessRightsEntries(Version version, AccessControlEntity aclEntity) {

    AccessControl data = aclEntity.getData();
    if (data == null) {
      Log.debugf(
          "AccessControlService.initAccessRightsEntries : seen AccessControlEntity without data in version %s",
          version);
      return;
    }

    ConcurrentRepository<AccessRightsEntryEntity> accessRightEntriesRepository =
        version.getAccessRightsEntryEntitiesRepository();
    AccessControlDeny deny = data.getDeny();
    if (deny != null && deny.getActualInstance() instanceof AccessControlDenyAnyOf) {
      AccessControlDenyAnyOf denyBody = deny.getAccessControlDenyAnyOf();
      aclEntity.addDenyAccessRights(
          initAccessRightsList(
              version,
              Objects.requireNonNullElse(denyBody.getAccessRights(), Collections.emptyList()),
              accessRightEntriesRepository));
    }
    AccessControlAllow allowBody = data.getAllow();
    if (allowBody != null) {
      aclEntity.addAllowAccessRights(
          initAccessRightsList(version, allowBody.getAccessRights(), accessRightEntriesRepository));
    }
  }

  private static Set<AccessRightsEntryEntity> initAccessRightsList(
      Version version,
      List<AccessRightsEntry> accessRights,
      ConcurrentRepository<AccessRightsEntryEntity> accessRightEntriesRepository) {

    Set<AccessRightsEntryEntity> accessRightsSet = new LinkedHashSet<>();
    ScopesRepository scopeRepo = version.getScopesRepository();
    if (scopeRepo == null) {
      Log.errorf("Scopes Repository not found for version %s", version);
      return accessRightsSet;
    }

    for (AccessRightsEntry accessRightsEntry : accessRights) {
      AccessRightsEntryEntity accessRightsEntryEntity =
          new AccessRightsEntryEntity(accessRightsEntry);

      AccessRightsEntryScopes applyToScopes = accessRightsEntry.getApplyTo();
      List<String> scopeNames =
          Objects.requireNonNullElse(applyToScopes.getScopes(), Collections.emptyList());
      Set<Scope> scopes = new HashSet<>();
      for (String scopeName : scopeNames) {
        Scope scope = scopeRepo.get(AbstractModelEntity.generateUniqueId(scopeName));
        if (scope != null) {
          scopes.add(scope);
        }
      }
      accessRightsEntryEntity.addApplyToScopes(scopes);

      String accessRightsEntryUniqId = accessRightsEntryEntity.id();
      AccessRightsEntryEntity accessRightsEntryEntityInRepository =
          Objects.requireNonNullElseGet(
              accessRightEntriesRepository.get(accessRightsEntryUniqId),
              new AccessEntitySupplier<>(
                  accessRightEntriesRepository, accessRightsEntryUniqId, accessRightsEntryEntity));

      accessRightsSet.add(accessRightsEntryEntityInRepository);
    }
    return accessRightsSet;
  }

  private static class AccessControlEntitySupplier
      extends AccessEntitySupplier<AccessControlEntity> {

    private final Version version;

    public AccessControlEntitySupplier(
        Version version,
        ConcurrentRepository<AccessControlEntity> repository,
        String entityId,
        AccessControlEntity entity) {
      super(repository, entityId, entity);
      this.version = version;
    }

    /** Initializes before get */
    @Override
    protected void init() {
      super.init();
      initAccessRightsEntries(version, entity);
    }
  }

  private static class AccessEntitySupplier<T> implements Supplier<T> {

    private final ConcurrentRepository<T> repository;
    private final String entityId;
    protected T entity;

    public AccessEntitySupplier(ConcurrentRepository<T> repository, String entityId, T entity) {
      this.repository = repository;
      this.entityId = entityId;
      this.entity = entity;
    }

    /**
     * Gets a result.
     *
     * @return a result
     */
    @Override
    public T get() {
      init();
      return repository.get(entityId);
    }

    protected void init() {
      repository.put(entityId, entity);
    }
  }

  private static class AccessControlEntitiesRepositorySupplier
      extends AbstractACLRepositorySupplier<AccessControlEntity> {

    public AccessControlEntitiesRepositorySupplier(Version version) {
      super(version);
    }

    /** Initializes AccessControlEntitiesRepository in version */
    @Override
    protected void init() {
      repository = new AccessControlEntitiesRepository();
      version.setAccessControlEntitiesRepository((AccessControlEntitiesRepository) repository);
    }
  }

  private static class AccessRightsEntriesRepositorySupplier
      extends AbstractACLRepositorySupplier<AccessRightsEntryEntity> {
    public AccessRightsEntriesRepositorySupplier(Version version) {
      super(version);
    }

    @Override
    protected void init() {
      repository = new AccessRightsEntryEntitiesRepository();
      version.setAccessRightsEntryEntitiesRepository(
          (AccessRightsEntryEntitiesRepository) repository);
    }
  }

  private abstract static class AbstractACLRepositorySupplier<T extends AbstractModelEntity<?>>
      implements Supplier<ConcurrentRepository<T>> {

    protected Version version;
    protected ConcurrentRepository<T> repository;

    protected AbstractACLRepositorySupplier(Version version) {
      this.version = version;
      this.repository = null;
    }

    /**
     * Gets a new repository.
     *
     * @return a result
     */
    @Override
    public ConcurrentRepository<T> get() {
      init();
      return repository;
    }

    protected abstract void init();
  }
}
