package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.models.core.utils.PortUtils;
import com.amadeus.aud.libraries.yaml.models.ProtocolEnum;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ServiceFilterTest {

  private StringFilter sf(String value) {
    StringFilter sf = new StringFilter();
    sf.setLike(value);
    return sf;
  }

  private Service svc(String name) {
    return svc(name, "443");
  }

  private Service svc(String name, String ports) {
    Service s = mock(Service.class);
    when(s.getName()).thenReturn(name);
    when(s.id()).thenReturn("svc-" + name.hashCode());
    when(s.getComponentName()).thenReturn("comp-api");
    when(s.getApplicationIdName()).thenReturn("app-portal");
    when(s.getServiceNowId()).thenReturn("RITM0001");
    when(s.getProtocol()).thenReturn(ProtocolEnum.TCP);
    when(s.getApprovedProtocol()).thenReturn(ProtocolEnum.TCP);
    when(s.isPrivate()).thenReturn(false);
    when(s.isPci()).thenReturn(false);
    when(s.isPii()).thenReturn(false);
    when(s.isEncrypted()).thenReturn(true);
    when(s.isDbSynchro()).thenReturn(false);
    when(s.getPorts()).thenReturn(ports);
    when(s.getPortsRange()).thenReturn(PortUtils.toRangeSet(ports));
    return s;
  }

  @Nested
  class Matches {
    @Test
    void nullService_returnsFalse() {
      assertThat(new ServiceFilter().matches(null)).isFalse();
    }

    @Test
    void emptyFilter_matchesAll() {
      assertThat(new ServiceFilter().matches(svc("svc-http"))).isTrue();
    }
  }

  @Nested
  class ByName {
    @Test
    void match() {
      ServiceFilter f = new ServiceFilter();
      StringFilter stringFilter = new StringFilter();
      stringFilter.setLike("http");
      f.setName(stringFilter);
      assertThat(f.matches(svc("svc-http"))).isTrue();
    }

    @Test
    void noMatch() {
      ServiceFilter f = new ServiceFilter();
      StringFilter stringFilter = new StringFilter();
      stringFilter.setLike("grpc");
      f.setName(stringFilter);
      assertThat(f.matches(svc("svc-http"))).isFalse();
    }
  }

  @Nested
  class ByComponentName {
    @Test
    void match() {
      ServiceFilter f = new ServiceFilter();
      f.setComponentName(sf("COMP-API"));
      assertThat(f.matches(svc("svc-http"))).isTrue();
    }

    @Test
    void noMatch() {
      ServiceFilter f = new ServiceFilter();
      f.setComponentName(sf("other"));
      assertThat(f.matches(svc("svc-http"))).isFalse();
    }

    @Test
    void nullOnEntity_noMatch() {
      Service s = mock(Service.class);
      when(s.getName()).thenReturn("svc-1");
      when(s.getComponentName()).thenReturn(null);

      ServiceFilter f = new ServiceFilter();
      f.setComponentName(sf("comp-api"));
      assertThat(f.matches(s)).isFalse();
    }
  }

  @Nested
  class ByApplicationIdName {
    @Test
    void match() {
      ServiceFilter f = new ServiceFilter();
      f.setApplicationIdName(sf("APP-PORTAL"));
      assertThat(f.matches(svc("svc-http"))).isTrue();
    }

    @Test
    void noMatch() {
      ServiceFilter f = new ServiceFilter();
      f.setApplicationIdName(sf("other"));
      assertThat(f.matches(svc("svc-http"))).isFalse();
    }

    @Test
    void nullOnEntity_noMatch() {
      Service s = mock(Service.class);
      when(s.getName()).thenReturn("svc-1");
      when(s.getApplicationIdName()).thenReturn(null);

      ServiceFilter f = new ServiceFilter();
      f.setApplicationIdName(sf("app-portal"));
      assertThat(f.matches(s)).isFalse();
    }
  }

  @Nested
  class ByServiceNowId {
    @Test
    void match() {
      ServiceFilter f = new ServiceFilter();
      f.setServiceNowId(sf("RITM0001"));
      assertThat(f.matches(svc("svc-http"))).isTrue();
    }

    @Test
    void noMatch() {
      ServiceFilter f = new ServiceFilter();
      f.setServiceNowId(sf("RITM9999"));
      assertThat(f.matches(svc("svc-http"))).isFalse();
    }

    @Test
    void nullOnEntity_noMatch() {
      Service s = mock(Service.class);
      when(s.getName()).thenReturn("svc-1");
      when(s.getServiceNowId()).thenReturn(null);

      ServiceFilter f = new ServiceFilter();
      f.setServiceNowId(sf("RITM0001"));
      assertThat(f.matches(s)).isFalse();
    }
  }

  @Nested
  class ByProtocol {
    @Test
    void match() {
      ServiceFilter f = new ServiceFilter();
      f.setProtocol(sf("Tcp"));
      assertThat(f.matches(svc("svc-http"))).isTrue();
    }

    @Test
    void noMatch() {
      ServiceFilter f = new ServiceFilter();
      f.setProtocol(sf("Udp"));
      assertThat(f.matches(svc("svc-http"))).isFalse();
    }

    @Test
    void nullOnEntity_noMatch() {
      Service s = mock(Service.class);
      when(s.getName()).thenReturn("svc-1");
      when(s.getProtocol()).thenReturn(null);

      ServiceFilter f = new ServiceFilter();
      f.setProtocol(sf("Tcp"));
      assertThat(f.matches(s)).isFalse();
    }
  }

  @Nested
  class ByApprovedProtocol {
    @Test
    void match() {
      ServiceFilter f = new ServiceFilter();
      f.setApprovedProtocol(sf("TCP"));
      assertThat(f.matches(svc("svc-http"))).isTrue();
    }

    @Test
    void noMatch() {
      ServiceFilter f = new ServiceFilter();
      f.setApprovedProtocol(sf("Udp"));
      assertThat(f.matches(svc("svc-http"))).isFalse();
    }

    @Test
    void nullOnEntity_noMatch() {
      Service s = mock(Service.class);
      when(s.getName()).thenReturn("svc-1");
      when(s.getApprovedProtocol()).thenReturn(null);

      ServiceFilter f = new ServiceFilter();
      f.setApprovedProtocol(sf("Tcp"));
      assertThat(f.matches(s)).isFalse();
    }
  }

  @Nested
  class ByDeprecatedStatus {
    @Test
    void noMatch() {
      ServiceFilter f = new ServiceFilter();
      f.setDeprecatedStatus(sf("Error"));
      assertThat(f.matches(svc("svc-http"))).isFalse();
    }

    @Test
    void nullOnEntity_noMatch() {
      Service s = mock(Service.class);
      when(s.getName()).thenReturn("svc-1");
      when(s.getDeprecatedStatus()).thenReturn(null);

      ServiceFilter f = new ServiceFilter();
      f.setDeprecatedStatus(sf("Warning"));
      assertThat(f.matches(s)).isFalse();
    }
  }

  @Nested
  class ByIsPrivate {
    @Test
    void match() {
      ServiceFilter f = new ServiceFilter();
      f.setIsPrivate(false);
      assertThat(f.matches(svc("svc-http"))).isTrue();
    }

    @Test
    void noMatch() {
      ServiceFilter f = new ServiceFilter();
      f.setIsPrivate(true);
      assertThat(f.matches(svc("svc-http"))).isFalse();
    }
  }

  @Nested
  class ByIsPci {
    @Test
    void noMatch() {
      ServiceFilter f = new ServiceFilter();
      f.setIsPci(true);
      assertThat(f.matches(svc("svc-http"))).isFalse();
    }
  }

  @Nested
  class ByIsPii {
    @Test
    void noMatch() {
      ServiceFilter f = new ServiceFilter();
      f.setIsPii(true);
      assertThat(f.matches(svc("svc-http"))).isFalse();
    }
  }

  @Nested
  class ByIsEncrypted {
    @Test
    void match() {
      ServiceFilter f = new ServiceFilter();
      f.setIsEncrypted(true);
      assertThat(f.matches(svc("svc-http"))).isTrue();
    }

    @Test
    void noMatch() {
      ServiceFilter f = new ServiceFilter();
      f.setIsEncrypted(false);
      assertThat(f.matches(svc("svc-http"))).isFalse();
    }
  }

  @Nested
  class ByIsDbSynchro {
    @Test
    void match() {
      ServiceFilter f = new ServiceFilter();
      f.setIsDbSynchro(false);
      assertThat(f.matches(svc("svc-http"))).isTrue();
    }

    @Test
    void noMatch() {
      ServiceFilter f = new ServiceFilter();
      f.setIsDbSynchro(true);
      assertThat(f.matches(svc("svc-http"))).isFalse();
    }
  }

  @Nested
  class ByPort {
    @Test
    void matchWhenAnyPortExistsInCommaSeparatedPorts() {
      ServiceFilter f = new ServiceFilter();
      f.setPort(sf("80"));

      assertThat(f.matches(svc("svc-http", "80,443"))).isTrue();
    }

    @Test
    void noMatchWhenPortIsNotInCommaSeparatedPorts() {
      ServiceFilter f = new ServiceFilter();
      f.setPort(sf("8080"));

      assertThat(f.matches(svc("svc-http", "80,443"))).isFalse();
    }

    @Test
    void matchWhenPortIsInsideRange() {
      ServiceFilter f = new ServiceFilter();
      f.setPort(sf("80"));

      assertThat(f.matches(svc("svc-http", "1-1024"))).isTrue();
    }

    @Test
    void matchWhenPortIsInsideWildcard() {
      ServiceFilter f = new ServiceFilter();
      f.setPort(sf("8080"));

      assertThat(f.matches(svc("svc-http", "*"))).isTrue();
    }

    @Test
    void matchWhenPortIsInsideWhitespaceSeparatedRange() {
      ServiceFilter f = new ServiceFilter();
      f.setPort(sf("81"));

      assertThat(f.matches(svc("svc-http", " 80 , 81-82 "))).isTrue();
    }

    @Test
    void noMatchWhenPortFallsOutsideRange() {
      ServiceFilter f = new ServiceFilter();
      f.setPort(sf("8080"));

      assertThat(f.matches(svc("svc-http", "1-1024"))).isFalse();
    }

    @Test
    void exactPortUsesContainmentLogic() {
      ServiceFilter f = new ServiceFilter();
      StringFilter portFilter = new StringFilter();
      portFilter.setEq("443");
      f.setPort(portFilter);

      assertThat(f.matches(svc("svc-http", "443-444"))).isTrue();
    }

    @Test
    void nonNumericLikeFallsBackToStringMatching() {
      ServiceFilter f = new ServiceFilter();
      f.setPort(sf("443-44"));

      assertThat(f.matches(svc("svc-http", "443-444"))).isTrue();
    }
  }

  @Nested
  class Getters {
    @Test
    void returnSetValues() {
      StringFilter stringFilter = new StringFilter();
      stringFilter.setLike("n");

      ServiceFilter f = new ServiceFilter();
      f.setName(stringFilter);
      f.setComponentName(sf("c"));
      f.setApplicationIdName(sf("a"));
      f.setServiceNowId(sf("s"));
      f.setProtocol(sf("Tcp"));
      f.setApprovedProtocol(sf("Udp"));
      f.setDeprecatedStatus(sf("Warning"));
      f.setIsPrivate(true);
      f.setIsPci(false);
      f.setIsPii(true);
      f.setIsEncrypted(false);
      f.setIsDbSynchro(true);

      assertThat(f.getName()).isEqualTo(stringFilter);
      assertThat(f.getComponentName()).isNotNull();
      assertThat(f.getApplicationIdName()).isNotNull();
      assertThat(f.getServiceNowId()).isNotNull();
      assertThat(f.getProtocol()).isNotNull();
      assertThat(f.getApprovedProtocol()).isNotNull();
      assertThat(f.getDeprecatedStatus()).isNotNull();
      assertThat(f.getIsPrivate()).isTrue();
      assertThat(f.getIsPci()).isFalse();
      assertThat(f.getIsPii()).isTrue();
      assertThat(f.getIsEncrypted()).isFalse();
      assertThat(f.getIsDbSynchro()).isTrue();
    }
  }

  @Nested
  class ById {
    @Test
    void getterSetter() {
      StringFilter idFilter = new StringFilter();
      idFilter.setLike("123");

      ServiceFilter f = new ServiceFilter();
      f.setId(idFilter);

      assertThat(f.getId()).isEqualTo(idFilter);
    }

    @Test
    void match() {
      ServiceFilter f = new ServiceFilter();
      StringFilter idFilter = new StringFilter();
      idFilter.setLike("svc-");
      f.setId(idFilter);
      assertThat(f.matches(svc("svc-http"))).isTrue();
    }

    @Test
    void noMatch() {
      ServiceFilter f = new ServiceFilter();
      StringFilter idFilter = new StringFilter();
      idFilter.setLike("xyz");
      f.setId(idFilter);
      assertThat(f.matches(svc("svc-http"))).isFalse();
    }
  }
}
