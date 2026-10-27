package com.amadeus.aud.libraries.common;

import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({
  ElementType.TYPE,
  ElementType.METHOD,
  ElementType.CONSTRUCTOR,
  ElementType.ANNOTATION_TYPE
})
@Inherited
public @interface LogExecutionTime {}
