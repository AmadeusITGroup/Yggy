package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.ApplicationId;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.yaml.models.V1ServiceSpec;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ApplicationIdFilterTest {

  private StringFilter sf(String value) {
    StringFilter sf = new StringFilter();
    sf.setLike(value);
    return sf;
  }

  private ApplicationId appId(String name) {
    ApplicationId a = mock(ApplicationId.class);
    when(a.id()).thenReturn("appid-" + name.hashCode());
    when(a.getName()).thenReturn(name);

    Service svc = mock(Service.class);
    when(svc.getName()).thenReturn("svc-http");
    when(svc.getComponentName()).thenReturn("comp-api");
    when(svc.getApplicationIdName()).thenReturn(name);
    when(svc.getServiceNowId()).thenReturn(null);
    when(svc.getProtocol()).thenReturn(V1ServiceSpec.ProtocolEnum.TCP);
    when(svc.getApprovedProtocol()).thenReturn(null);
    when(svc.getDeprecatedStatus()).thenReturn(null);
    when(svc.isPrivate()).thenReturn(false);
    when(svc.isPci()).thenReturn(false);
    when(svc.isPii()).thenReturn(false);
    when(svc.isEncrypted()).thenReturn(false);
    when(svc.isDbSynchro()).thenReturn(false);
    when(svc.getPorts()).thenReturn("80");
    when(a.getServices()).thenReturn(Set.of(svc));

    return a;
  }

  @Test
  void matchesNull_returnsFalse() {
    assertThat(new ApplicationIdFilter().matches(null)).isFalse();
  }

  @Test
  void emptyFilter_matchesAll() {
    assertThat(new ApplicationIdFilter().matches(appId("app-portal"))).isTrue();
  }

  @Test
  void filterByName_match() {
    ApplicationIdFilter f = new ApplicationIdFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("portal");
    f.setName(stringFilter);
    assertThat(f.matches(appId("app-portal"))).isTrue();
  }

  @Test
  void filterByName_noMatch() {
    ApplicationIdFilter f = new ApplicationIdFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("xyz");
    f.setName(stringFilter);
    assertThat(f.matches(appId("app-portal"))).isFalse();
  }

  @Test
  void filterByServices_match() {
    ServiceFilter sf = new ServiceFilter();
    StringFilter protocolFilter = new StringFilter();
    protocolFilter.setLike("Tcp");
    sf.setProtocol(protocolFilter);

    ApplicationIdFilter f = new ApplicationIdFilter();
    f.setServices(List.of(sf));
    assertThat(f.matches(appId("app-portal"))).isTrue();
  }

  @Test
  void filterByServices_noMatch() {
    ServiceFilter sf = new ServiceFilter();
    StringFilter protocolFilter = new StringFilter();
    protocolFilter.setLike("Udp");
    sf.setProtocol(protocolFilter);

    ApplicationIdFilter f = new ApplicationIdFilter();
    f.setServices(List.of(sf));
    assertThat(f.matches(appId("app-portal"))).isFalse();
  }

  @Test
  void filterByServices_nullServices_noMatch() {
    ApplicationId a = mock(ApplicationId.class);
    when(a.getName()).thenReturn("app-1");
    when(a.getServices()).thenReturn(null);

    ServiceFilter sf = new ServiceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("svc");
    sf.setName(stringFilter);

    ApplicationIdFilter f = new ApplicationIdFilter();
    f.setServices(List.of(sf));
    assertThat(f.matches(a)).isFalse();
  }

  @Test
  void getters_returnSetValues() {
    ServiceFilter sf = new ServiceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("n");

    ApplicationIdFilter f = new ApplicationIdFilter();
    f.setName(stringFilter);
    f.setServices(List.of(sf));

    assertThat(f.getName()).isEqualTo(stringFilter);
    assertThat(f.getServices()).containsExactly(sf);
  }

  @Test
  void filterById_match() {
    ApplicationIdFilter f = new ApplicationIdFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("appid-");
    f.setId(idFilter);
    assertThat(f.matches(appId("app-portal"))).isTrue();
  }

  @Test
  void filterById_noMatch() {
    ApplicationIdFilter f = new ApplicationIdFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("xyz");
    f.setId(idFilter);
    assertThat(f.matches(appId("app-portal"))).isFalse();
  }

  @Test
  void id_getterSetter() {
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("123");

    ApplicationIdFilter f = new ApplicationIdFilter();
    f.setId(idFilter);

    assertThat(f.getId()).isEqualTo(idFilter);
  }
}
