package com.amadeus.aud.libraries.models.core.db;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.yaml.models.AccessControl;
import com.amadeus.aud.libraries.yaml.models.AccessControlAllow;
import com.amadeus.aud.libraries.yaml.models.AccessControlDeny;
import com.amadeus.aud.libraries.yaml.models.AccessControlDenyAnyOf;
import com.amadeus.aud.libraries.yaml.models.AccessDenialMessage;
import com.amadeus.aud.libraries.yaml.models.AccessRightsEntry;
import com.amadeus.aud.libraries.yaml.models.AccessRightsEntryScopes;
import com.amadeus.aud.libraries.yaml.models.V1Component;
import com.amadeus.aud.libraries.yaml.models.V1ComponentSpec;
import com.amadeus.aud.libraries.yaml.models.V1Metadata;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ComponentTest {

  @Nested
  class GetAccessControl {

    @Test
    void returnsNullWhenAccessControlIsMissing() {
      Component component = component("component-a", "scope-a", null);

      Map<String, Object> result = component.getAccessControl();

      assertThat(result).isNull();
    }

    @Test
    void flattensRightsBasedAllowAndDenyAndControlledComponents() {
      AccessControl accessControl =
          new AccessControl()
              .allow(
                  new AccessControlAllow()
                      .addAccessRightsItem(
                          accessRight(
                              AccessRightsEntry.PermissionEnum.COMPONENT_INHERIT,
                              "allow-msg",
                              AccessDenialMessage.UserMessageSeverityEnum.ERROR,
                              "scope-a",
                              "scope-b")))
              .deny(
                  new AccessControlDeny(
                      new AccessControlDenyAnyOf()
                          .addAccessRightsItem(
                              accessRight(
                                  AccessRightsEntry.PermissionEnum.COMPONENT_DEPENDENCY_DST,
                                  "deny-msg",
                                  AccessDenialMessage.UserMessageSeverityEnum.WARNING,
                                  "scope-c"))));

      Component component = component("component-a", "scope-a", accessControl);
      Component siblingWithSameAccessControl = component("component-b", "scope-a", accessControl);
      Component siblingWithoutAccessControl = component("component-c", "scope-a", null);
      attachToScope(
          "scope-a", component, siblingWithSameAccessControl, siblingWithoutAccessControl);

      Map<String, Object> result = component.getAccessControl();

      assertThat(result)
          .containsEntry(
              "allow",
              List.of(
                  Map.of(
                      "permission",
                      "COMPONENT_INHERIT",
                      "user_message",
                      "allow-msg",
                      "user_message_severity",
                      "ERROR",
                      "applied_to_scopes",
                      List.of("scope-a", "scope-b"))))
          .containsEntry(
              "deny",
              List.of(
                  Map.of(
                      "permission",
                      "COMPONENT_DEPENDENCY_DST",
                      "user_message",
                      "deny-msg",
                      "user_message_severity",
                      "WARNING",
                      "applied_to_scopes",
                      List.of("scope-c"))))
          .containsEntry("controlled_components", List.of("component-a", "component-b"));
    }

    @Test
    void flattensMessageBasedDeny() {
      AccessControl accessControl =
          new AccessControl()
              .deny(
                  new AccessControlDeny(
                      new AccessDenialMessage()
                          .userMessage("denied-scope")
                          .userMessageSeverity(AccessDenialMessage.UserMessageSeverityEnum.ERROR)));
      Component component = component("component-a", "scope-a", accessControl);

      Map<String, Object> result = component.getAccessControl();

      assertThat(result)
          .containsEntry(
              "deny", Map.of("user_message", "denied-scope", "user_message_severity", "ERROR"))
          .containsEntry("controlled_components", List.of("component-a"));
    }
  }

  private static AccessRightsEntry accessRight(
      AccessRightsEntry.PermissionEnum permission,
      String userMessage,
      AccessDenialMessage.UserMessageSeverityEnum severity,
      String... appliedToScopes) {
    return new AccessRightsEntry()
        .permission(permission)
        .reportIfDenied(
            new AccessDenialMessage().userMessage(userMessage).userMessageSeverity(severity))
        .applyTo(new AccessRightsEntryScopes().scopes(List.of(appliedToScopes)));
  }

  private static Component component(String name, String scopeName, AccessControl accessControl) {
    V1Metadata metadata = new V1Metadata().name(name).scope(scopeName);
    V1ComponentSpec spec = new V1ComponentSpec().parents(List.of()).accessControl(accessControl);
    V1Component data = new V1Component().metadata(metadata).spec(spec);
    return new Component(data);
  }

  private static void attachToScope(String scopeName, Component... components) {
    Scope scope = new Scope();
    scope.setName(scopeName);
    scope.setComponents(new LinkedHashSet<>(List.of(components)));
    for (Component component : components) {
      component.setScope(scope);
    }
  }
}
