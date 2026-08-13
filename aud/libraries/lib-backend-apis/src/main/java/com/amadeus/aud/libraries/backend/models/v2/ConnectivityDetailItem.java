package com.amadeus.aud.libraries.backend.models.v2;

import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import jakarta.annotation.Nullable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConnectivityDetailItem(
    @JsonProperty("name") String name,
    @JsonProperty("source") ConnectivityDetailSide source,
    @JsonProperty("target") ConnectivityDetailSide target,
    @JsonProperty("dependency_instance") DependencyInstance dependencyInstance,
    @JsonProperty("service") Service service,
    @JsonProperty("pci") boolean pci) {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record ConnectivityDetailSide(
      @JsonUnwrapped ComponentInstance componentInstance,
      @JsonProperty("subnet") List<String> subnet) {

    public @Nullable String getName() {
      return componentInstance != null ? componentInstance.getName() : null;
    }
  }
}
