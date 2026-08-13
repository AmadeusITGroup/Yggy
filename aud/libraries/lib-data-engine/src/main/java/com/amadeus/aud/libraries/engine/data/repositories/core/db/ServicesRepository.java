package com.amadeus.aud.libraries.engine.data.repositories.core.db;

import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.models.core.db.Service;

public class ServicesRepository extends ConcurrentRepository<Service> {
  public static final String UDP_SERVICE_NAME = "<!>*UDP_SERVICE*<!>";
  public static final String TCP_SERVICE_NAME = "<!>*TCP_SERVICE*<!>";
  public static final String ALL_SERVICE_NAME = "<!>*ALL_SERVICES*<!>";

  public ServicesRepository() {
    super();
  }

  public Service getUdpService() {
    return this.get(UDP_SERVICE_NAME);
  }

  public void setUdpService(Service udpService) {
    this.put(UDP_SERVICE_NAME, udpService);
  }

  public Service getTcpService() {
    return this.get(TCP_SERVICE_NAME);
  }

  public void setTcpService(Service tcpService) {
    this.put(TCP_SERVICE_NAME, tcpService);
  }

  public Service getAllService() {
    return this.get(ALL_SERVICE_NAME);
  }

  public void setAllService(Service allService) {
    this.put(ALL_SERVICE_NAME, allService);
  }
}
