package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.common.LogExecutionTime;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.RolesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopesRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.models.core.db.Role;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.yaml.models.V1Role;
import com.amadeus.aud.libraries.yaml.models.V1ScopeSpec;
import io.quarkus.logging.Log;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

@Singleton
public class RoleService extends BaseService<Role, V1Role> {

  @Override
  protected RolesRepository getRepository(Version version) {
    RolesRepository rolesRepository = new RolesRepository();
    version.setRolesRepository(rolesRepository);
    return rolesRepository;
  }

  @LogExecutionTime
  public void init(Version version) {
    ScopesRepository scopesRepository = version.getScopesRepository();
    if (scopesRepository == null) {
      Log.errorf("Scope Repository not found for version %s", version);
      return;
    }
    ConcurrentRepository<Role> repository =
        init(
            version,
            Role.class,
            V1Role.class,
            (Role role, Scope scope) -> {
              if (scope == null) {
                return;
              }

              scope.addDefinedRole(role);
            });

    associateScopesWithRoles(repository, scopesRepository);
  }

  private void associateScopesWithRoles(
      ConcurrentRepository<Role> rolesRepository, ConcurrentRepository<Scope> scopesRepository) {
    Log.debugf("Associating Scopes with Roles...");

    ConcurrentMap<String, Role> roleMap = rolesRepository.getData();

    Map<String, Scope> scopeMap = scopesRepository.getData();

    scopeMap.forEach(
        (String uniqueId, Scope scope) -> {
          V1ScopeSpec spec = scope.getData().getSpec();
          if (spec == null) {
            return;
          }

          List<String> roles = spec.getRoles();
          if (roles == null) {
            return;
          }

          roles.forEach(
              (String roleName) -> {
                Role role = roleMap.get(Role.generateUniqueId(roleName));
                if (role == null) {
                  logModelWarning("Scope %s references a non-existing Role %s", uniqueId, roleName);
                } else {
                  role.addScope(scope);
                  scope.addRole(role);
                }
              });
        });

    Log.debugf("Completed associating Scopes with Roles");
  }
}
