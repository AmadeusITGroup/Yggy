package com.amadeus.aud.libraries.models.core;

import org.apache.commons.net.util.SubnetUtils;

public final class Constants {
  public static final int DB_STRING_LENGTH = 8192;

  /**
   * Source: <a href="https://datatracker.ietf.org/doc/html/rfc1918#autoid-3">Private IPv4
   * addresses</a>
   */
  public static final class PrivateIpRanges {
    public static final String CIDR_10 = "10.0.0.0/8";
    public static final String CIDR_172 = "172.16.0.0/12";
    public static final String CIDR_192 = "192.168.0.0/16";
    public static final SubnetUtils.SubnetInfo SUBNET_10 = new SubnetUtils(CIDR_10).getInfo();
    public static final SubnetUtils.SubnetInfo SUBNET_172 = new SubnetUtils(CIDR_172).getInfo();
    public static final SubnetUtils.SubnetInfo SUBNET_192 = new SubnetUtils(CIDR_192).getInfo();

    private PrivateIpRanges() {}
  }

  private Constants() {
    throw new UnsupportedOperationException("Utility class");
  }
}
