package com.amadeus.aud.services.ipreport.http;

import com.google.common.net.InetAddresses;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Objects;

@ApplicationScoped
public class IPv4Validator implements ConstraintValidator<ValidIPv4, String> {
  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    try {
      InetAddress inetAddress = InetAddresses.forString(value);
      return inetAddress instanceof Inet4Address
          && Objects.equals(value, inetAddress.getHostAddress());
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
