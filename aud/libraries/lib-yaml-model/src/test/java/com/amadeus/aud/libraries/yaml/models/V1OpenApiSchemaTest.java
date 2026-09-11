package com.amadeus.aud.libraries.yaml.models;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.junit.jupiter.api.Test;

class V1OpenApiSchemaTest {

  private static final String MODEL_PACKAGE = V1Metadata.class.getPackageName();
  private static final String MODEL_PACKAGE_PATH = MODEL_PACKAGE.replace('.', '/');

  @Test
  void allV1ModelsAndPropertiesExposeDescriptions()
      throws IOException, URISyntaxException, ClassNotFoundException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    List<Class<?>> modelClasses = findV1ModelClasses(classLoader);
    List<String> missingDescriptions = new ArrayList<>();

    assertThat(modelClasses).isNotEmpty();
    for (Class<?> modelClass : modelClasses) {
      for (Method method : modelClass.getMethods()) {
        if (!isPropertyGetter(method)) {
          continue;
        }
        Schema propertySchema = method.getAnnotation(Schema.class);
        if (propertySchema == null || propertySchema.description().isBlank()) {
          missingDescriptions.add(modelClass.getSimpleName() + "." + method.getName());
        }
      }
    }

    assertThat(missingDescriptions).as("V1 OpenAPI descriptions must not be empty").isEmpty();
  }

  private static List<Class<?>> findV1ModelClasses(ClassLoader classLoader)
      throws IOException, URISyntaxException, ClassNotFoundException {
    Set<String> classNames = new TreeSet<>();
    Enumeration<URL> resources = classLoader.getResources(MODEL_PACKAGE_PATH);
    while (resources.hasMoreElements()) {
      URL resource = resources.nextElement();
      if ("file".equals(resource.getProtocol())) {
        collectFileClasses(classNames, resource.toURI());
      } else if ("jar".equals(resource.getProtocol())) {
        collectJarClasses(classNames, resource);
      }
    }

    List<Class<?>> classes = new ArrayList<>();
    for (String className : classNames) {
      classes.add(Class.forName(className, false, classLoader));
    }
    return classes;
  }

  private static void collectFileClasses(Set<String> classNames, URI packageUri)
      throws IOException {
    Path packageDirectory = Path.of(packageUri);
    try (DirectoryStream<Path> entries = Files.newDirectoryStream(packageDirectory, "V1*.class")) {
      for (Path entry : entries) {
        String fileName = entry.getFileName().toString();
        if (!fileName.contains("$") && !fileName.endsWith("Test.class")) {
          classNames.add(MODEL_PACKAGE + "." + fileName.substring(0, fileName.length() - 6));
        }
      }
    }
  }

  private static void collectJarClasses(Set<String> classNames, URL resource) throws IOException {
    JarURLConnection connection = (JarURLConnection) resource.openConnection();
    try (JarFile jarFile = connection.getJarFile()) {
      String prefix = MODEL_PACKAGE_PATH + "/";
      Enumeration<JarEntry> entries = jarFile.entries();
      while (entries.hasMoreElements()) {
        String entryName = entries.nextElement().getName();
        if (entryName.startsWith(prefix)
            && entryName.endsWith(".class")
            && !entryName.substring(prefix.length()).contains("/$")
            && !entryName.endsWith("Test.class")) {
          String relativeName = entryName.substring(0, entryName.length() - 6);
          if (!relativeName.contains("$")) {
            classNames.add(relativeName.replace('/', '.'));
          }
        }
      }
    }
  }

  private static boolean isPropertyGetter(Method method) {
    String methodName = method.getName();
    return method.getParameterCount() == 0
        && method.getReturnType() != void.class
        && !method.isBridge()
        && !method.isSynthetic()
        && method.isAnnotationPresent(JsonProperty.class)
        && !"getClass".equals(methodName)
        && !"getSpec".equals(methodName)
        && !"getMetadata".equals(methodName)
        && ((methodName.startsWith("get") && methodName.length() > 3)
            || (methodName.startsWith("is")
                && methodName.length() > 2
                && (method.getReturnType() == boolean.class
                    || method.getReturnType() == Boolean.class)));
  }
}
