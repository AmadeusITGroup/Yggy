package com.amadeus.aud.libraries.engine.data.fallbackhandlers;

import com.amadeus.aud.libraries.yaml.models.Statistics;
import com.amadeus.aud.libraries.yaml.models.StatisticsScope;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import java.util.List;
import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;

public class UniStatisticsFallback implements FallbackHandler<Uni<Statistics>> {

  @Override
  public Uni<Statistics> handle(ExecutionContext context) {
    Log.warnf("Fallback called due to failure %s", context.getFailure());
    var emptyStats = new Statistics();
    var scopeStats = new StatisticsScope();
    emptyStats.setScopes(List.of(scopeStats));
    return Uni.createFrom().item(emptyStats);
  }
}
