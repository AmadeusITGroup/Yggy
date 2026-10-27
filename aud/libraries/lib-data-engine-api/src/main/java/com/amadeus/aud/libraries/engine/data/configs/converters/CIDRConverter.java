package com.amadeus.aud.libraries.engine.data.configs.converters;

import com.amadeus.aud.libraries.models.core.utils.IPv4Utils;
import com.google.common.collect.Range;
import java.math.BigInteger;
import org.eclipse.microprofile.config.spi.Converter;

@SuppressWarnings("java:S2057")
public class CIDRConverter implements Converter<Range<BigInteger>> {
  @Override
  public Range<BigInteger> convert(final String value) {
    return IPv4Utils.toRange(value);
  }
}
