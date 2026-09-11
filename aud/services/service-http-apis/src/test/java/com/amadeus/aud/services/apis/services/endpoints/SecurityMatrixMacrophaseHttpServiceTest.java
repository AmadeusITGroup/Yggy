package com.amadeus.aud.services.apis.services.endpoints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.MacroPhasesRepository;
import com.amadeus.aud.libraries.models.core.db.ConnectionPhase;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
import com.amadeus.aud.libraries.yaml.models.SecurityMatrixMacrophaseResponse;
import com.amadeus.aud.libraries.yaml.models.V1ConnectionPhaseSpec;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.SAME_THREAD)
class SecurityMatrixMacrophaseHttpServiceTest {

  @Mock private AudService audService;

  @Mock private AudApiConfig audApiConfig;

  @Mock private UriInfo uriInfo;

  @Mock private Version version;

  private SecurityMatrixMacrophaseHttpService service;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() throws Exception {
    service = new SecurityMatrixMacrophaseHttpService(audService, audApiConfig);

    objectMapper = EndpointTestObjectMapper.get();
    Field objectMapperField = BaseService.class.getDeclaredField("objectMapper");
    objectMapperField.setAccessible(true);
    objectMapperField.set(service, objectMapper);

    when(audApiConfig.version()).thenReturn("v1");
    when(version.getVersionNumber()).thenReturn("1.0.0");
    when(uriInfo.getAbsolutePath())
        .thenReturn(new URI("http://localhost:8080/api/security-matrix-macrophases"));
    when(uriInfo.getRequestUri())
        .thenReturn(new URI("http://localhost:8080/api/security-matrix-macrophases"));
  }

  @Test
  @DisplayName("should return security matrix macrophases response")
  void getSecurityMatrixMacrophasesWithValidVersion() throws Exception {
    MacroPhase phase1 = mock(MacroPhase.class);
    when(phase1.getName()).thenReturn("phase1");
    when(phase1.getConnectionPhases()).thenReturn(Set.of());

    MacroPhase phase2 = mock(MacroPhase.class);
    when(phase2.getName()).thenReturn("phase2");
    when(phase2.getConnectionPhases()).thenReturn(Set.of());

    ConcurrentHashMap<String, MacroPhase> phasesData = new ConcurrentHashMap<>();
    phasesData.put("phase1", phase1);
    phasesData.put("phase2", phase2);

    MacroPhasesRepository macroPhasesRepository = mock(MacroPhasesRepository.class);
    when(macroPhasesRepository.getData()).thenReturn(phasesData);
    when(macroPhasesRepository.isReady()).thenReturn(true);
    when(version.getMacroPhasesRepository()).thenReturn(macroPhasesRepository);

    Response response =
        service.getSecurityMatrixMacrophases(version, uriInfo).await().indefinitely();

    assertThat(response.getStatus()).isEqualTo(200);

    byte[] bodyBytes = (byte[]) response.getEntity();
    SecurityMatrixMacrophaseResponse body =
        objectMapper.readValue(bodyBytes, SecurityMatrixMacrophaseResponse.class);

    assertThat(body).isNotNull();
    assertThat(body.getVersion()).isEqualTo("1.0.0");
    assertThat(body.getApiVersion()).isEqualTo("v1");
    assertThat(body.getMatrices()).isNotNull();

    @SuppressWarnings("unchecked")
    Map<String, Map<String, String>> matrix = (Map<String, Map<String, String>>) body.getMatrices();

    assertThat(matrix.get("phase1")).containsEntry("phase1", "red");
    assertThat(matrix.get("phase1")).containsEntry("phase2", "red");
    assertThat(matrix.get("phase2")).containsEntry("phase1", "red");
    assertThat(matrix.get("phase2")).containsEntry("phase2", "red");
  }

  @Test
  @DisplayName("should apply override controls from connection phases")
  void securityMatrixMacrophaseWithOverrides() throws Exception {
    // ✅ Nouveau test avec ConnectionPhases (overrides)
    MacroPhase source = mock(MacroPhase.class);
    when(source.getName()).thenReturn("source");

    MacroPhase target = mock(MacroPhase.class);
    when(target.getName()).thenReturn("target");

    ConnectionPhase connectionPhase = mock(ConnectionPhase.class);
    when(connectionPhase.getSourcePhase()).thenReturn(source);
    when(connectionPhase.getDestinationPhase()).thenReturn(target);
    when(connectionPhase.getControl()).thenReturn(V1ConnectionPhaseSpec.ControlEnum.GREEN);

    when(source.getConnectionPhases()).thenReturn(Set.of(connectionPhase));
    when(target.getConnectionPhases()).thenReturn(Set.of());

    ConcurrentHashMap<String, MacroPhase> phasesData = new ConcurrentHashMap<>();
    phasesData.put("source", source);
    phasesData.put("target", target);

    MacroPhasesRepository macroPhasesRepository = mock(MacroPhasesRepository.class);
    when(macroPhasesRepository.getData()).thenReturn(phasesData);
    when(macroPhasesRepository.isReady()).thenReturn(true);
    when(version.getMacroPhasesRepository()).thenReturn(macroPhasesRepository);

    Response response =
        service.getSecurityMatrixMacrophases(version, uriInfo).await().indefinitely();

    byte[] bodyBytes = (byte[]) response.getEntity();
    SecurityMatrixMacrophaseResponse body =
        objectMapper.readValue(bodyBytes, SecurityMatrixMacrophaseResponse.class);

    assertThat(body.getMatrices()).isNotNull();

    @SuppressWarnings("unchecked")
    Map<String, Map<String, String>> matrix = (Map<String, Map<String, String>>) body.getMatrices();

    assertThat(matrix.get("source")).containsEntry("target", "green");
    assertThat(matrix.get("source")).containsEntry("source", "red");
    assertThat(matrix.get("target")).containsEntry("target", "red");
    assertThat(matrix.get("target")).containsEntry("source", "red");
  }

  @Test
  @DisplayName("should retrieve version from audService when using string version")
  void getSecurityMatrixMacrophasesWithStringVersion() throws Exception {
    MacroPhase phase = mock(MacroPhase.class);
    when(phase.getName()).thenReturn("phase1");
    when(phase.getConnectionPhases()).thenReturn(Set.of());

    ConcurrentHashMap<String, MacroPhase> phasesData = new ConcurrentHashMap<>();
    phasesData.put("phase1", phase);

    MacroPhasesRepository macroPhasesRepository = mock(MacroPhasesRepository.class);
    when(macroPhasesRepository.getData()).thenReturn(phasesData);
    when(macroPhasesRepository.isReady()).thenReturn(true);
    when(version.getMacroPhasesRepository()).thenReturn(macroPhasesRepository);

    when(audService.getVersion("1.0.0")).thenReturn(Uni.createFrom().item(version));

    Response response =
        service.getSecurityMatrixMacrophases("1.0.0", uriInfo).await().indefinitely();

    assertThat(response.getStatus()).isEqualTo(200);

    byte[] bodyBytes = (byte[]) response.getEntity();
    SecurityMatrixMacrophaseResponse body =
        objectMapper.readValue(bodyBytes, SecurityMatrixMacrophaseResponse.class);

    assertThat(body).isNotNull();
    assertThat(body.getVersion()).isEqualTo("1.0.0");

    @SuppressWarnings("unchecked")
    Map<String, Map<String, String>> matrix = (Map<String, Map<String, String>>) body.getMatrices();

    assertThat(matrix.get("phase1")).containsEntry("phase1", "red");
  }
}
