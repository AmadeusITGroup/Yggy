package com.amadeus.aud.services.apis.models.http.responses.prs;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.yaml.models.V1Device;
import java.util.List;
import org.junit.jupiter.api.Test;

class PrStatusTest {

  @Test
  void prStatusGettersAndSettersAndConstructors() {
    // Arrange
    PrStatus.Status status = new PrStatus.Status();
    status.setScopeName("scope-1");
    status.setScopeCurrentVersion("1.0.0");
    status.setAudVersion("2.0.0");
    status.setStatus("SUCCESS");
    status.setSubStatus("MERGED");
    status.setPrNumber("123");
    status.setIntegratedDatetime("2026-09-01T10:00:00Z");
    status.setIntegratedScopeVersion("1.0.1");
    List<V1Device> devices = List.of(new V1Device());
    status.setImpactedDevices(devices);

    // Act
    PrStatus sut = new PrStatus("https://example.com", "v1", "1.0", "v2", status);

    // Assert
    assertThat(sut.getUrl()).isEqualTo("https://example.com");
    assertThat(sut.getUrlVersion()).isEqualTo("v1");
    assertThat(sut.getVersion()).isEqualTo("1.0");
    assertThat(sut.getApiVersion()).isEqualTo("v2");
    assertThat(sut.getStatus()).isEqualTo(status);

    assertThat(status.getScopeName()).isEqualTo("scope-1");
    assertThat(status.getScopeCurrentVersion()).isEqualTo("1.0.0");
    assertThat(status.getAudVersion()).isEqualTo("2.0.0");
    assertThat(status.getStatus()).isEqualTo("SUCCESS");
    assertThat(status.getSubStatus()).isEqualTo("MERGED");
    assertThat(status.getPrNumber()).isEqualTo("123");
    assertThat(status.getIntegratedDatetime()).isEqualTo("2026-09-01T10:00:00Z");
    assertThat(status.getIntegratedScopeVersion()).isEqualTo("1.0.1");
    assertThat(status.getImpactedDevices()).isEqualTo(devices);
  }

  @Test
  void prStatusNoArgConstructorAndSetters() {
    PrStatus sut = new PrStatus();
    PrStatus.Status status = new PrStatus.Status();

    sut.setUrl("https://example.com");
    sut.setUrlVersion("v1");
    sut.setVersion("1.0");
    sut.setApiVersion("v2");
    sut.setStatus(status);

    assertThat(sut.getUrl()).isEqualTo("https://example.com");
    assertThat(sut.getStatus()).isSameAs(status);
  }

  @Test
  void prStatusEqualsAndHashCode() {
    PrStatus.Status status1 = new PrStatus.Status();
    status1.setStatus("OK");
    PrStatus.Status status2 = new PrStatus.Status();
    status2.setStatus("KO");

    PrStatus sut1 = new PrStatus("http://a", "v1", "1.0", "v2", status1);
    PrStatus sutDifferentStatus = new PrStatus("http://a", "v1", "1.0", "v2", status2);
    PrStatus sutDifferentUrl = new PrStatus("http://b", "v1", "1.0", "v2", status1);

    assertThat(sut1).isEqualTo(sut1);
    assertThat(sut1).isNotEqualTo(null);
    assertThat(sut1).isNotEqualTo("other-type");
    assertThat(sut1).isNotEqualTo(sutDifferentUrl);
    assertThat(sut1).isNotEqualTo(sutDifferentStatus);

    int initialHashCode = sut1.hashCode();
    assertThat(sut1.hashCode()).isEqualTo(initialHashCode);
  }

  @Test
  void statusEqualsAndHashCode() {
    PrStatus.Status sut = new PrStatus.Status();
    sut.setScopeName("scope");
    sut.setStatus("PENDING");

    PrStatus.Status different = new PrStatus.Status();
    different.setScopeName("scope");
    different.setStatus("PENDING");

    assertThat(sut).isEqualTo(sut);
    assertThat(sut).isNotEqualTo(null);
    assertThat(sut).isNotEqualTo("other-type");
    assertThat(sut).isNotEqualTo(different);

    int initialHashCode = sut.hashCode();
    assertThat(sut.hashCode()).isEqualTo(initialHashCode);
  }
}
