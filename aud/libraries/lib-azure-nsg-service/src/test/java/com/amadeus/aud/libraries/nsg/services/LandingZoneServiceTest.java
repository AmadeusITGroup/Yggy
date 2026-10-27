package com.amadeus.aud.libraries.nsg.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.DependencyInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZonesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.SubnetInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ZoneInstancesRepository;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.nsg.models.LandingZoneNotFound;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import jakarta.ws.rs.ServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LandingZoneServiceTest {

  private NsgService nsgService;
  private AudApiConfig audApiConfig;
  private AudService audService;
  private LandingZoneService service;

  @BeforeEach
  void setUp() {
    nsgService = mock(NsgService.class);
    audApiConfig = mock(AudApiConfig.class);
    audService = mock(AudService.class);
    service =
        new LandingZoneService(
            nsgService, audApiConfig, audService, OpenTelemetry.noop().getTracer("test"));
  }

  @Test
  void getNetworkSecurity_throwsLandingZoneNotFound_whenLandingZoneIsNull() {
    Version version = mock(Version.class);
    doReturn("1.0.0").when(version).getVersionNumber();

    LandingZonesRepository landingZonesRepository = mock(LandingZonesRepository.class);
    doReturn(true).when(landingZonesRepository).isReady();
    doReturn(null).when(landingZonesRepository).get("unknown-lz");
    doReturn(landingZonesRepository).when(version).getLandingZonesRepository();

    assertThatThrownBy(() -> service.getNetworkSecurity(version, "unknown-lz"))
        .isInstanceOf(LandingZoneNotFound.class)
        .hasMessageContaining("unknown-lz");
  }

  @Test
  void getNetworkSecurity_throwsIllegalStateException_whenProviderIsNotAzure() {
    Version version = mock(Version.class);
    doReturn("1.0.0").when(version).getVersionNumber();

    LandingZonesRepository landingZonesRepository = mock(LandingZonesRepository.class);
    doReturn(true).when(landingZonesRepository).isReady();
    doReturn(landingZonesRepository).when(version).getLandingZonesRepository();

    LandingZone landingZone = mock(LandingZone.class);
    doReturn(landingZone).when(landingZonesRepository).get("aws-lz");

    Target target = mock(Target.class);
    doReturn("AWS").when(target).getProviderName();
    doReturn(target).when(landingZone).getTarget();

    assertThatThrownBy(() -> service.getNetworkSecurity(version, "aws-lz"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("not in Azure");
  }

  @Test
  void buildNetworkSecurityRecordsSuccessfulSpan() {
    InMemorySpanExporter spanExporter = InMemorySpanExporter.create();
    LandingZoneService tracedService =
        new LandingZoneService(
            nsgService, audApiConfig, audService, tracerExportingTo(spanExporter));
    Version version = readyVersionForNsg("1.0.0");
    LandingZone landingZone = mock(LandingZone.class);
    doReturn("lz-1").when(landingZone).getName();
    Target target = mock(Target.class);

    tracedService.buildNetworkSecurity(version, landingZone, target);

    assertThat(spanExporter.getFinishedSpanItems())
        .singleElement()
        .satisfies(
            (SpanData span) -> {
              assertThat(span.getName()).isEqualTo("nsg.build-network-security");
              assertThat(span.getAttributes().get(AttributeKey.stringKey("aud.version")))
                  .isEqualTo("1.0.0");
              assertThat(span.getAttributes().get(AttributeKey.stringKey("aud.landing_zone")))
                  .isEqualTo("lz-1");
              assertThat(span.getStatus().getStatusCode()).isNotEqualTo(StatusCode.ERROR);
            });
  }

  @Test
  void buildNetworkSecurityRecordsErrorStatusAndExceptionOnFailure() {
    InMemorySpanExporter spanExporter = InMemorySpanExporter.create();
    LandingZoneService tracedService =
        new LandingZoneService(
            nsgService, audApiConfig, audService, tracerExportingTo(spanExporter));
    Version version = mock(Version.class);
    doReturn("1.0.0").when(version).getVersionNumber();
    doReturn(null).when(version).getDependencyInstancesRepository();
    LandingZone landingZone = mock(LandingZone.class);
    doReturn("lz-1").when(landingZone).getName();
    Target target = mock(Target.class);

    assertThatThrownBy(() -> tracedService.buildNetworkSecurity(version, landingZone, target))
        .isInstanceOf(ServiceUnavailableException.class);

    assertThat(spanExporter.getFinishedSpanItems())
        .singleElement()
        .satisfies(
            (SpanData span) -> {
              assertThat(span.getName()).isEqualTo("nsg.build-network-security");
              assertThat(span.getStatus().getStatusCode()).isEqualTo(StatusCode.ERROR);
              assertThat(span.getEvents())
                  .anySatisfy(event -> assertThat(event.getName()).isEqualTo("exception"));
            });
  }

  private static Tracer tracerExportingTo(InMemorySpanExporter spanExporter) {
    return OpenTelemetrySdk.builder()
        .setTracerProvider(
            SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
                .build())
        .build()
        .getTracer(LandingZoneServiceTest.class.getName());
  }

  private static Version readyVersionForNsg(String versionNumber) {
    Version version = mock(Version.class);
    doReturn(versionNumber).when(version).getVersionNumber();
    DependencyInstancesRepository dependencyInstancesRepository =
        mock(DependencyInstancesRepository.class);
    doReturn(true).when(dependencyInstancesRepository).isReady();
    doReturn(dependencyInstancesRepository).when(version).getDependencyInstancesRepository();
    ZoneInstancesRepository zoneInstancesRepository = mock(ZoneInstancesRepository.class);
    doReturn(true).when(zoneInstancesRepository).isReady();
    doReturn(zoneInstancesRepository).when(version).getZoneInstancesRepository();
    SubnetInstancesRepository subnetInstancesRepository = mock(SubnetInstancesRepository.class);
    doReturn(true).when(subnetInstancesRepository).isReady();
    doReturn(subnetInstancesRepository).when(version).getSubnetInstancesRepository();
    ComponentsRepository componentsRepository = mock(ComponentsRepository.class);
    doReturn(true).when(componentsRepository).isReady();
    doReturn(componentsRepository).when(version).getComponentsRepository();
    doReturn(mock(LandingZonesRepository.class)).when(version).getLandingZonesRepository();
    return version;
  }
}
