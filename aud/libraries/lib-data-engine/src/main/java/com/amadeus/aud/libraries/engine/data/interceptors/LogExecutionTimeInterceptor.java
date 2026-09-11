package com.amadeus.aud.libraries.engine.data.interceptors;

import com.amadeus.aud.libraries.engine.data.utils.LogExecutionTime;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;

@LogExecutionTime
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class LogExecutionTimeInterceptor {

  public void logExecutionTime(Temporal startTime, InvocationContext context) {
    Log.debugf(
        "%s::%s executed in %s",
        context.getMethod().getDeclaringClass().getSimpleName(),
        context.getMethod().getName(),
        Duration.between(startTime, Instant.now()).toString());
  }

  @AroundInvoke
  public Object logMethodExecutionTime(InvocationContext context) throws Exception {
    Temporal startTime = Instant.now();
    Object ret = context.proceed();
    if (ret instanceof Uni<?> uni) {
      return uni.onItem().invoke(() -> logExecutionTime(startTime, context));
    } else {
      logExecutionTime(startTime, context);
    }
    return ret;
  }
}
