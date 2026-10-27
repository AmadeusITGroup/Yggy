package com.amadeus.aud.libraries.engine.data.configs;

import com.amadeus.aud.libraries.engine.data.configs.converters.CIDRConverter;
import com.google.common.collect.Range;
import io.smallrye.config.WithConverter;
import java.math.BigInteger;

public interface CidrRangeConfig {
  @WithConverter(CIDRConverter.class)
  Range<BigInteger> cidr();
}
