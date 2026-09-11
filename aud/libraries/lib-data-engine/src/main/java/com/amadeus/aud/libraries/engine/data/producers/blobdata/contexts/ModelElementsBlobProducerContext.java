package com.amadeus.aud.libraries.engine.data.producers.blobdata.contexts;

import com.amadeus.aud.libraries.engine.data.configs.AudDataConfig;
import com.amadeus.aud.libraries.engine.data.configs.AudScopesConfig;
import com.amadeus.aud.libraries.engine.data.interfaces.BlobFormat;
import com.amadeus.aud.libraries.engine.data.interfaces.ModelElementsProducerContext;
import com.amadeus.aud.libraries.engine.data.services.external.parsing.AbstractDocumentParsingService;
import jakarta.annotation.Nullable;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collection;
import java.util.Set;

public record ModelElementsBlobProducerContext<T>(
    String versionString,
    BlobFormat blobFormat,
    AudScopesConfig audScopesConfig,
    AudDataConfig audDataConfig,
    @Nullable String containerName,
    @Nullable String blobNameExpression,
    @Nullable AbstractDocumentParsingService parsingService,
    @Nullable Class<T> modelElementClass,
    @Nullable Collection<String> responses,
    @Nullable Boolean dump,
    @Nullable Path dumpPath,
    @Nullable Duration maxWait)
    implements ModelElementsProducerContext<T> {

  public ModelElementsBlobProducerContext(
      String versionString,
      BlobFormat blobFormat,
      AudScopesConfig audScopesConfig,
      AudDataConfig audDataConfig,
      @Nullable String containerName) {
    this(
        versionString,
        blobFormat,
        audScopesConfig,
        audDataConfig,
        containerName,
        null,
        null,
        null,
        Set.of(),
        Boolean.FALSE,
        null,
        audScopesConfig.maxLoadDuration());
  }

  public ModelElementsBlobProducerContext(
      String versionString,
      BlobFormat blobFormat,
      AudScopesConfig audScopesConfig,
      AudDataConfig audDataConfig,
      @Nullable String containerName,
      @Nullable String blobNameExpression) {
    this(
        versionString,
        blobFormat,
        audScopesConfig,
        audDataConfig,
        containerName,
        blobNameExpression,
        null,
        null,
        Set.of(),
        Boolean.FALSE,
        null,
        audScopesConfig.maxLoadDuration());
  }

  public ModelElementsBlobProducerContext(
      String versionString,
      BlobFormat blobFormat,
      AudScopesConfig audScopesConfig,
      AudDataConfig audDataConfig,
      @Nullable AbstractDocumentParsingService parsingService,
      @Nullable Class<T> modelElementClass,
      @Nullable Collection<String> responses) {
    this(
        versionString,
        blobFormat,
        audScopesConfig,
        audDataConfig,
        null,
        null,
        parsingService,
        modelElementClass,
        responses != null ? responses : Set.of(),
        dumpEnabled(audScopesConfig),
        dumpPath(audScopesConfig),
        audScopesConfig.maxLoadDuration());
  }

  @Override
  public Class<T> getModelElementClass() {
    return modelElementClass;
  }

  @Override
  public AbstractDocumentParsingService getParsingService() {
    return parsingService();
  }

  @Override
  public BlobFormat getBlobFormat() {
    return blobFormat();
  }

  @Override
  public String getContainerName() {
    return containerName();
  }

  @Override
  public String getBlobNameExpression() {
    return blobNameExpression();
  }

  @Override
  public Collection<String> getResponses() {
    return responses();
  }

  @Override
  public AudScopesConfig getAudScopesConfig() {
    return audScopesConfig;
  }

  @Override
  public AudDataConfig getAudDataConfig() {
    return audDataConfig();
  }

  @Override
  public String getVersion() {
    return versionString();
  }

  @Override
  public Duration maxWait() {
    return maxWait;
  }

  private static boolean dumpEnabled(AudScopesConfig audScopesConfig) {
    return audScopesConfig.blob().dump().enabled();
  }

  private static Path dumpPath(AudScopesConfig audScopesConfig) {
    return Path.of(audScopesConfig.blob().dump().basePath().orElse("./"));
  }
}

/*
public class ModelElementsBlobProducerContext<T extends ModelElement> implements ModelElementsProducerContext<T> {

    private String[] parts;
    private Class<T> modelElementClass;

    @Inject Version version;

    @Inject
    AudScopesConfig audScopesConfig;

    @Inject
    public ModelElementsBlobProducerContext(Class<T> modelElementClass, String version, String[] parts, BlobFormat blobFormat, boolean dump) {
        this.parts = parts;
        this.modelElementClass = modelElementClass;
        this.version.setVersionNumber(version);
    }

    @Override
    public Class<T> getModelElementClass() {
        return modelElementClass;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    public String[] getParts() {
        return parts;
    }

    public AudScopesConfig getAudGovernanceConfig() {
        return audScopesConfig;
    }
}

 */
