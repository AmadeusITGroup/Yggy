package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ConnectionsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ZoneTypesRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.engine.data.utils.LogExecutionTime;
import com.amadeus.aud.libraries.models.core.db.Connection;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.yaml.models.V1Connection;
import io.quarkus.logging.Log;
import jakarta.inject.Singleton;

@Singleton
public class ConnectionService extends BaseService<Connection, V1Connection> {

  @Override
  protected ConnectionsRepository getRepository(Version version) {
    ConnectionsRepository connectionsRepository = new ConnectionsRepository();
    version.setConnectionsRepository(connectionsRepository);
    return connectionsRepository;
  }

  @LogExecutionTime
  public void init(Version version) {
    ZoneTypesRepository zoneTypesRepository = version.getZoneTypesRepository();
    if (zoneTypesRepository == null) {
      Log.errorf("ZoneType Repository not found for version %s", version);
      return;
    }

    init(
        version,
        Connection.class,
        V1Connection.class,
        (Connection connection, Scope scope) -> {
          if (scope == null) {
            return;
          }

          scope.addConnection(connection);
          linkZoneTypes(connection, zoneTypesRepository);
        });
  }

  private void linkZoneTypes(Connection connection, ZoneTypesRepository zoneTypesRepository) {
    String sourceZoneTypeName = connection.getData().getSpec().getSourceZoneType();
    if (sourceZoneTypeName != null && !sourceZoneTypeName.isEmpty()) {
      ZoneType sourceZoneType = zoneTypesRepository.get(sourceZoneTypeName);
      if (sourceZoneType == null) {
        logModelWarning(
            "Connection %s references a non-existing source ZoneType %s",
            connection.id(), sourceZoneTypeName);
      } else {
        connection.setSourceZoneType(sourceZoneType);
        sourceZoneType.addConnection(connection);
      }
    }

    String destinationZoneTypeName = connection.getData().getSpec().getDestinationZoneType();
    if (destinationZoneTypeName != null && !destinationZoneTypeName.isEmpty()) {
      ZoneType destinationZoneType = zoneTypesRepository.get(destinationZoneTypeName);
      if (destinationZoneType == null) {
        logModelWarning(
            "Connection %s references a non-existing destination ZoneType %s",
            connection.id(), destinationZoneTypeName);
      } else {
        connection.setDestinationZoneType(destinationZoneType);
        destinationZoneType.addConnection(connection);
      }
    }
  }
}
