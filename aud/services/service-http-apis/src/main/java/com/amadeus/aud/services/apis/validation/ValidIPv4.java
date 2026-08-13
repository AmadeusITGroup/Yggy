package com.amadeus.aud.services.apis.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = IPv4Validator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidIPv4 {

  String message() default
      "Invalid IPv4 address format. Expected format: x.x.x.x (e.g., 192.168.1.1)";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
