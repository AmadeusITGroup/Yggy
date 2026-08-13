package com.amadeus.aud.services.apis.endpoints.v2;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.backend.models.v2.Response;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("v2 services – Response type conventions")
class V2ServicesResponseTypeTest {

  private static final String SERVICES_PACKAGE =
      "com.amadeus.aud.services.apis.services.endpoints.v2";

  @Test
  @DisplayName("All public methods in v2 services returning Uni must use Response<T> v2")
  void allV2ServiceMethodsMustReturnResponseV2() throws Exception {
    List<Class<?>> serviceClasses = findServiceClasses();

    assertThat(serviceClasses)
        .as("No service classes found in package '%s'", SERVICES_PACKAGE)
        .isNotEmpty();

    List<String> violations = new ArrayList<>();

    for (Class<?> serviceClass : serviceClasses) {
      for (Method method : serviceClass.getDeclaredMethods()) {
        if (!java.lang.reflect.Modifier.isPublic(method.getModifiers())) {
          continue;
        }

        Type returnType = method.getGenericReturnType();
        if (!(returnType instanceof ParameterizedType paramType)) {
          continue;
        }

        if (!paramType.getRawType().equals(Uni.class)) {
          continue;
        }

        Type[] typeArgs = paramType.getActualTypeArguments();
        if (typeArgs.length == 0) {
          continue;
        }

        Type uniArg = typeArgs[0];

        if (uniArg instanceof ParameterizedType innerParam) {
          Type rawType = innerParam.getRawType();
          if (rawType.equals(Response.class)) {
            continue;
          }
        }

        if (uniArg.equals(jakarta.ws.rs.core.Response.class)) {
          violations.add(
              String.format(
                  "%s#%s returns Uni<jakarta.ws.rs.core.Response> instead of Uni<Response<T>>",
                  serviceClass.getSimpleName(), method.getName()));
        }
      }
    }

    assertThat(violations)
        .as(
            "v2 service methods must return Uni<Response<T>> (v2), not Uni<jakarta.ws.rs.core.Response>")
        .isEmpty();
  }

  private List<Class<?>> findServiceClasses() throws Exception {
    String packagePath = SERVICES_PACKAGE.replace('.', '/');
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    List<Class<?>> result = new ArrayList<>();
    for (URL url : Collections.list(classLoader.getResources(packagePath))) {
      File directory = new File(url.getFile());
      if (!directory.isDirectory()) {
        continue;
      }
      File[] classFiles =
          directory.listFiles(f -> f.getName().endsWith(".class") && !f.getName().contains("$"));
      if (classFiles == null) {
        continue;
      }
      for (File file : classFiles) {
        String className = SERVICES_PACKAGE + "." + file.getName().replace(".class", "");
        Class<?> clazz = Class.forName(className);
        if (clazz.isAnnotationPresent(Singleton.class)) {
          result.add(clazz);
        }
      }
    }
    return result;
  }
}
