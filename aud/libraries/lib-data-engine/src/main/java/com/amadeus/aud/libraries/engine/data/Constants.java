package com.amadeus.aud.libraries.engine.data;

import org.apache.commons.net.util.SubnetUtils;

public final class Constants {
  public static final String MICROSOFT_AZURE_PROVIDER = "Microsoft - Azure";
  public static final String EXTERNAL_1A_PROVIDER_NAME = "External 1A";
  public static final String EQUINIX_PROVIDER_NAME = "Equinix";
  public static final String GLOBAL = "Global";
  public static final String LATEST = "latest";
  public static final String N_A = "N/A";
  public static final String TECHNICAL_ZONE = "TechnicalZone";
  public static final String TECHNICAL_ZONE_WITH_NSG = "TechnicalZoneWithNSG";

  public static final String TOP_LEVEL_COMPONENT_NAME = "TopLevel";
  public static final String OFFICES_TARGET_NAME = "Offices";

  public static final int DB_STRING_LENGTH = 8192;

  public static final String INTERNET_URL = "Internet URL";
  public static final String EXTRANET_URL = "Extranet URL";
  public static final String INTERNET_TARGET_NAME = "Internet";
  public static final String EXTRANET_TARGET_NAME = "Extranet";
  public static final String ZONE_TYPE_INTERNET = "internet";
  public static final String ZONE_TYPE_EXTRANET = "extranet";

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

  private Constants() {}
}
