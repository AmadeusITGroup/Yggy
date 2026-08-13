package com.amadeus.aud.libraries.engine.data.unistrategies;

import com.amadeus.aud.libraries.engine.data.interfaces.ModelElementsProducerContext;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import java.nio.file.Path;
import java.util.Collection;

public class ModelElementsProducerStrategy<
        T extends ModelElement, U extends ModelElementsProducerContext<? extends T>>
    extends BaseProducerStrategy<T, U> {

  @Override
  public Uni<Collection<T>> withStrategy(Uni<Collection<T>> executable, U context) {
    Uni<Collection<T>> execution = super.withStrategy(executable, context);
    if (context.getAudScopesConfig().blob().dump().enabled()) {
      execution =
          execution
              .onItem()
              .invoke(
                  Unchecked.consumer(
                      (Collection<T> fromBlob) -> {
                        String extension = context.getBlobFormat().name().toLowerCase();
                        Path path =
                            Path.of(
                                context.getAudScopesConfig().blob().dump().basePath().orElse("."),
                                context.getModelElementClass().getSimpleName()
                                    + "."
                                    + context.getVersion()
                                    + "."
                                    + extension);
                        context.getParsingService().dump(fromBlob.toString(), path);
                      }));
    }
    return execution
        .onFailure()
        .invoke(
            (Throwable throwable) -> {
              if (throwable != null) {
                Log.errorf(
                    throwable,
                    "Error receiving data from client "
                        + context.getModelElementClass().getSimpleName()
                        + " : "
                        + throwable.getMessage());
              }
            });
  }
}
