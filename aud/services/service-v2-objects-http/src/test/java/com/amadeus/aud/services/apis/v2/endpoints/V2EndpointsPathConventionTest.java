package com.amadeus.aud.services.apis.v2.endpoints;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.ws.rs.Path;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("v2 endpoints – @Path annotation conventions")
class V2EndpointsPathConventionTest {

  private static final String PACKAGE_NAME = V2EndpointsPathConventionTest.class.getPackageName();
  private static final Set<String> VERSION_PATH_EXCEPTIONS =
      Set.of("v2/versions/latest/connectivity");

  @Test
  @DisplayName("All @Path methods must start with 'v2/' and parameterise any 'versions/' segment")
  void allPathAnnotationsMustFollowConventions() throws Exception {
    List<Method> pathMethods = findPathAnnotatedMethods();

    assertThat(pathMethods)
        .as(
            "No @Path-annotated methods found in package '%s' – check classpath scanning",
            PACKAGE_NAME)
        .isNotEmpty();

    for (Method method : pathMethods) {
      String path = method.getAnnotation(Path.class).value();
      String location = method.getDeclaringClass().getSimpleName() + "#" + method.getName();

      assertThat(path)
          .as("'%s' has path '%s': all v2 endpoint paths must start with 'v2/'", location, path)
          .startsWith("v2/");

      if (path.contains("versions/") && !VERSION_PATH_EXCEPTIONS.contains(path)) {
        assertThat(path)
            .as(
                """
                '%s' has path '%s': any 'versions/' segment must use '{version}' as a path\
                 parameter, not a hard-coded version string""",
                location, path)
            .matches(".*versions/[{]version[}].*");
      }
    }
  }

  /**
   * Finds all methods annotated with {@link Path} in every class belonging to this package.
   *
   * <p>Uses {@link ClassLoader#getResources(String)} (plural) so that both {@code target/classes}
   * and {@code target/test-classes} are scanned — the two separate output directories that Maven
   * uses for production and test sources respectively.
   */
  private List<Method> findPathAnnotatedMethods() throws Exception {
    String packagePath = PACKAGE_NAME.replace('.', '/');
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    List<Method> result = new ArrayList<>();
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
        String className = PACKAGE_NAME + "." + file.getName().replace(".class", "");
        Class<?> clazz = Class.forName(className);
        if (clazz.equals(V2EndpointsPathConventionTest.class)) {
          continue; // skip the test class itself
        }
        Arrays.stream(clazz.getDeclaredMethods())
            .filter(m -> m.isAnnotationPresent(Path.class))
            .forEach(result::add);
      }
    }
    return result;
  }
}
