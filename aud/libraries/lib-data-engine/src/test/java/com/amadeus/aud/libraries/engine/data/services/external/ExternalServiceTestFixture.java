package com.amadeus.aud.libraries.engine.data.services.external;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.engine.data.configs.AudDataConfig;
import com.amadeus.aud.libraries.engine.data.configs.AudModelConfig;
import com.amadeus.aud.libraries.engine.data.configs.AudScopesConfig;
import com.amadeus.aud.libraries.engine.data.configs.DumpConfig;
import com.amadeus.aud.libraries.engine.data.configs.YamlValidationConfig;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

final class ExternalServiceTestFixture {
  static final Duration TIMEOUT = Duration.ofSeconds(3);

  static final String SCOPE_YAML =
      """
      ---
      apiVersion: v1
      kind: Scope
      metadata:
        name: Governance
        scope: Governance
      spec:
        roles:
          - Governance
      ---
      apiVersion: v1
      kind: Scope
      metadata:
        name: Application
        scope: Governance
      spec:
        roles:
          - Application
      """;

  static final String DUPLICATE_SCOPE_YAML =
      """
      ---
      apiVersion: v1
      kind: Scope
      metadata:
        name: Application
        scope: Governance
      spec:
        roles:
          - first
      ---
      apiVersion: v1
      kind: Scope
      metadata:
        name: Application
        scope: Governance
      spec:
        roles:
          - second
      """;

  static final String SCOPE_JSON =
      """
      [
        {
          "apiVersion": "v1",
          "kind": "Scope",
          "metadata": {"name": "JsonScope", "scope": "JsonScope"},
          "spec": {"roles": ["Application"]}
        }
      ]
      """;

  static final String CORE_MAPPING_YAML =
      """
      ---
      api_version: v1
      kind: ScopeVersionMapping
      metadata:
        name: application-1.2.3
        scope: Application
      spec:
        scope_version: 1.2.3
      """;

  static final String CORE_RELEASE_YAML =
      """
      ---
      api_version: v1
      kind: ScopeReleaseNote
      metadata:
        name: application-release
        scope: Application
      spec:
        scope_version: 1.2.3
        prs:
          - "42"
      """;

  static final String CORE_MAPPING_JSON =
      """
      [
        {
          "apiVersion": "v1",
          "kind": "ScopeVersionMapping",
          "metadata": {"name": "json-mapping", "scope": "JsonScope"},
          "spec": {"scope_version": "2.0.0"}
        }
      ]
      """;

  static final String CORE_RELEASE_JSON =
      """
      [
        {
          "apiVersion": "v1",
          "kind": "ScopeReleaseNote",
          "metadata": {"name": "json-release", "scope": "JsonScope"},
          "spec": {"scope_version": "2.0.0", "prs": ["17"]}
        }
      ]
      """;

  static final String REQUIREMENTS_YAML =
      """
      ---
      requirements:
        - name: Application
          scope: Application
          comment: application data
          repo: projects/AUD/repos/application/raw/
          files:
            - Application.yml
          version: 1.2.3
      """;

  static final String REQUIREMENTS_JSON =
      """
      {
        "requirements": [
          {
            "name": "JsonScope",
            "scope": "JsonScope",
            "comment": "json data",
            "repo": "projects/AUD/repos/json/raw/",
            "files": ["JsonScope.yml"],
            "version": "2.0.0"
          }
        ]
      }
      """;

  private ExternalServiceTestFixture() {}

  static AudModelConfig modelConfig() {
    AudModelConfig config = mock(AudModelConfig.class);
    YamlValidationConfig validationConfig = mock(YamlValidationConfig.class);
    when(config.yamlValidation()).thenReturn(validationConfig);
    when(validationConfig.enableWarning()).thenReturn(false);
    when(validationConfig.enableOnTheFlyFixes()).thenReturn(false);
    return config;
  }

  static AudDataConfig dataConfig(Path localDirectory) {
    AudDataConfig config = mock(AudDataConfig.class);
    AudDataConfig.CloudStorageConfig cloudStorageConfig =
        mock(AudDataConfig.CloudStorageConfig.class);
    AudDataConfig.LocalFileConfig localFileConfig = mock(AudDataConfig.LocalFileConfig.class);
    when(config.cloudStorage()).thenReturn(cloudStorageConfig);
    when(cloudStorageConfig.dataContainerName()).thenReturn("data-container");
    when(cloudStorageConfig.requirementsContainerName()).thenReturn("requirements-container");
    when(cloudStorageConfig.versionContainerName()).thenReturn("version-container");
    when(config.localFile()).thenReturn(localFileConfig);
    when(localFileConfig.localDataDirectory()).thenReturn(localDirectory.toString());
    return config;
  }

  static AudScopesConfig scopesConfig(boolean enabled, boolean associateRequirements) {
    AudScopesConfig config = mock(AudScopesConfig.class);
    AudScopesConfig.ScopesBlobConfig blobConfig = mock(AudScopesConfig.ScopesBlobConfig.class);
    DumpConfig dumpConfig = mock(DumpConfig.class);
    when(config.enabled()).thenReturn(enabled);
    when(config.associateScopeWithRequirements()).thenReturn(associateRequirements);
    when(config.maxLoadDuration()).thenReturn(TIMEOUT);
    when(config.blob()).thenReturn(blobConfig);
    when(blobConfig.dump()).thenReturn(dumpConfig);
    when(dumpConfig.enabled()).thenReturn(false);
    when(dumpConfig.basePath()).thenReturn(Optional.empty());
    return config;
  }
}
