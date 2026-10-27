package com.amadeus.aud.libraries.v2.objects.service;

import static com.amadeus.aud.libraries.v2.objects.service.PaginationDefaults.DEFAULT_PAGE_NUM;
import static com.amadeus.aud.libraries.v2.objects.service.PaginationDefaults.DEFAULT_PAGE_SIZE;
import static com.amadeus.aud.libraries.v2.objects.service.PaginationDefaults.MAX_PAGE_SIZE;

import com.amadeus.aud.libraries.backend.models.v2.Metadata;
import com.amadeus.aud.libraries.backend.models.v2.Pagination;
import com.amadeus.aud.libraries.backend.models.v2.Response;
import com.amadeus.aud.libraries.backend.models.v2.ScopeVersionDiffItem;
import com.amadeus.aud.libraries.backend.models.v2.ScopeVersionDiffItem.ChangeType;
import com.amadeus.aud.libraries.backend.models.v2.ScopeVersionDiffItem.Message;
import com.amadeus.aud.libraries.backend.models.v2.ScopeVersionDiffItem.PullRequest;
import com.amadeus.aud.libraries.common.Constants;
import com.amadeus.aud.libraries.engine.data.AudVersionsService;
import com.amadeus.aud.libraries.engine.data.configs.AudModelConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.SingleVersion;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.base.BaseRepository;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudRequirementsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopesRepository;
import com.amadeus.aud.libraries.models.core.db.Requirement;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.ScopeReleaseNote;
import com.amadeus.aud.libraries.models.core.utils.VersionUtils;
import com.amadeus.aud.libraries.yaml.models.V1Commit;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ServiceUnavailableException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class ScopeVersionDiffService {
  private final AudVersionsService audVersionService;
  private final AudModelConfig audModelConfig;
  private final Tracer tracer;

  @Inject
  public ScopeVersionDiffService(
      AudVersionsService audVersionService, AudModelConfig audModelConfig, Tracer tracer) {
    this.audVersionService = audVersionService;
    this.audModelConfig = audModelConfig;
    this.tracer = tracer;
  }

  @CacheResult(cacheName = "getScopeVersionDiffItemsV2")
  public Uni<List<ScopeVersionDiffItem>> getScopeVersionDiffItems(
      Version oldVersion, Version newVersion) {

    return audVersionService
        .listAllVersions()
        .onItem()
        .transformToUni(
            (List<SingleVersion> availableVersions) -> {
              validateVersionOrder(oldVersion, newVersion, availableVersions);

              return Uni.createFrom()
                  .item(Unchecked.supplier(() -> computeDiffItemsTraced(oldVersion, newVersion)))
                  .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                  .emitOn(Infrastructure.getDefaultWorkerPool());
            });
  }

  // @WithSpan would silently no-op here: this is called from a lambda, which bypasses the CDI
  // proxy.
  private List<ScopeVersionDiffItem> computeDiffItemsTraced(
      Version oldVersion, Version newVersion) {
    Span span =
        tracer
            .spanBuilder("scope-version-diff.compute")
            .setAttribute("aud.old_version", oldVersion.getVersionNumber())
            .setAttribute("aud.new_version", newVersion.getVersionNumber())
            .startSpan();
    io.opentelemetry.context.Scope scope = span.makeCurrent();
    try {
      return computeDiffItems(oldVersion, newVersion);
    } catch (RuntimeException e) {
      span.recordException(e);
      span.setStatus(StatusCode.ERROR);
      throw e;
    } finally {
      scope.close();
      span.end();
    }
  }

  public Uni<Response<List<ScopeVersionDiffItem>>> getScopeVersionDiffResponse(
      Version oldVersion,
      Version newVersion,
      @Nullable Integer pageNum,
      @Nullable Integer pageSize) {
    validatePagination(pageNum, pageSize);
    return getScopeVersionDiffItems(oldVersion, newVersion)
        .onItem()
        .transform(items -> paginate(items, pageNum, pageSize));
  }

  private static void validateVersionOrder(
      Version oldVersion, Version newVersion, List<SingleVersion> availableVersions) {
    OffsetDateTime oldVersionTimestamp = findVersionTimestamp(oldVersion, availableVersions);
    OffsetDateTime newVersionTimestamp = findVersionTimestamp(newVersion, availableVersions);

    if (oldVersionTimestamp != null && newVersionTimestamp != null) {
      if (newVersionTimestamp.isBefore(oldVersionTimestamp)) {
        throw new BadRequestException("newVersion must be greater than or equal to oldVersion");
      }
      return;
    }

    if (newVersion.getVersionLong() < oldVersion.getVersionLong()) {
      throw new BadRequestException("newVersion must be greater than or equal to oldVersion");
    }
  }

  private static @Nullable OffsetDateTime findVersionTimestamp(
      Version version, List<SingleVersion> availableVersions) {
    if (Constants.LATEST.equalsIgnoreCase(version.getVersionNumber())) {
      OffsetDateTime latestTimestamp = null;

      for (SingleVersion availableVersion : availableVersions) {
        OffsetDateTime currentTimestamp = availableVersion.timestamp();
        if (currentTimestamp == null) {
          continue;
        }

        if (latestTimestamp == null || currentTimestamp.isAfter(latestTimestamp)) {
          latestTimestamp = currentTimestamp;
        }
      }

      return latestTimestamp;
    }

    for (SingleVersion availableVersion : availableVersions) {
      if (Objects.equals(availableVersion.version(), version.getVersionNumber())) {
        return availableVersion.timestamp();
      }
    }
    return null;
  }

  private static void validatePagination(@Nullable Integer pageNum, @Nullable Integer pageSize) {
    if (pageSize != null && pageSize < 1) {
      throw new BadRequestException("pageSize must be >= 1");
    }
    if (pageSize != null && pageSize > MAX_PAGE_SIZE) {
      throw new BadRequestException("pageSize must be <= %s".formatted(MAX_PAGE_SIZE));
    }
    if (pageNum != null && pageNum < 1) {
      throw new BadRequestException("pageNum must be >= 1");
    }
  }

  private Response<List<ScopeVersionDiffItem>> paginate(
      List<ScopeVersionDiffItem> items, @Nullable Integer pageNum, @Nullable Integer pageSize) {
    int effectivePageSize = pageSize != null ? pageSize : DEFAULT_PAGE_SIZE;
    int effectivePageNum = pageNum != null ? pageNum : DEFAULT_PAGE_NUM;
    int totalCount = items.size();
    int lastPage = Math.max(1, (int) Math.ceil((double) totalCount / effectivePageSize));

    if (effectivePageNum > lastPage) {
      throw new BadRequestException(
          "pageNum (" + effectivePageNum + ") exceeds last page (" + lastPage + ")");
    }

    int offset = (effectivePageNum - 1) * effectivePageSize;
    List<ScopeVersionDiffItem> pagedItems =
        items.subList(offset, Math.min(offset + effectivePageSize, totalCount));
    Metadata metadata =
        new Metadata()
            .withPagination(new Pagination(totalCount, effectivePageNum, effectivePageSize));

    return new Response<>(pagedItems, metadata);
  }

  private List<ScopeVersionDiffItem> computeDiffItems(Version oldVersion, Version newVersion) {
    String oldVersionNum = oldVersion.getVersionNumber();
    String newVersionNum = newVersion.getVersionNumber();

    AudRequirementsRepository oldRequirementsRepository = oldVersion.getAudRequirementsRepository();
    checkRepository(oldRequirementsRepository, oldVersionNum, "Requirements");

    AudRequirementsRepository newRequirementsRepository = newVersion.getAudRequirementsRepository();
    checkRepository(newRequirementsRepository, newVersionNum, "Requirements");

    ScopesRepository oldScopesRepository = oldVersion.getScopesRepository();
    checkRepository(oldScopesRepository, oldVersionNum, "Scopes");

    ScopesRepository newScopesRepository = newVersion.getScopesRepository();
    checkRepository(newScopesRepository, newVersionNum, "Scopes");

    Map<String, Requirement> oldRequirementsMap = toRequirementsMap(oldRequirementsRepository);
    Map<String, Requirement> newRequirementsMap = toRequirementsMap(newRequirementsRepository);

    Set<String> allScopes = new HashSet<>();
    allScopes.addAll(oldRequirementsMap.keySet());
    allScopes.addAll(newRequirementsMap.keySet());

    List<ScopeVersionDiffItem> items = new ArrayList<>();

    for (String scopeName : allScopes) {
      Requirement oldRequirement = oldRequirementsMap.get(scopeName);
      Requirement newRequirement = newRequirementsMap.get(scopeName);

      createDiffItem(
              scopeName, oldRequirement, newRequirement, oldScopesRepository, newScopesRepository)
          .ifPresent(items::add);
    }
    return items;
  }

  private static Map<String, Requirement> toRequirementsMap(
      AudRequirementsRepository requirementsRepository) {
    Map<String, Requirement> requirementsMap = new HashMap<>();
    for (Requirement requirement : requirementsRepository.getData().requirements()) {
      requirementsMap.put(requirement.getScope(), requirement);
    }
    return requirementsMap;
  }

  private Optional<ScopeVersionDiffItem> createDiffItem(
      String scopeName,
      Requirement oldRequirement,
      Requirement newRequirement,
      ScopesRepository oldScopesRepository,
      ScopesRepository newScopesRepository) {

    if (oldRequirement == null && newRequirement != null) {
      return Optional.of(
          createItem(
              scopeName,
              ChangeType.ADDED,
              null,
              newRequirement.getVersion(),
              newScopesRepository.getData().get(scopeName)));
    }

    if (oldRequirement != null && newRequirement == null) {
      return Optional.of(
          createItem(
              scopeName,
              ChangeType.REMOVED,
              oldRequirement.getVersion(),
              null,
              oldScopesRepository.getData().get(scopeName)));
    }

    if (oldRequirement != null
        && newRequirement != null
        && !Objects.equals(oldRequirement.getVersion(), newRequirement.getVersion())) {
      return Optional.of(
          createItem(
              scopeName,
              ChangeType.CHANGED,
              oldRequirement.getVersion(),
              newRequirement.getVersion(),
              newScopesRepository.getData().get(scopeName)));
    }

    return Optional.empty();
  }

  private ScopeVersionDiffItem createItem(
      String scopeName,
      ChangeType changeType,
      @Nullable String fromVersion,
      @Nullable String toVersion,
      @Nullable Scope scope) {
    List<PullRequest> pullRequests = new ArrayList<>();
    List<Message> messages = new ArrayList<>();

    String releaseNotesFromVersion = changeType == ChangeType.REMOVED ? null : fromVersion;
    String releaseNotesToVersion = changeType == ChangeType.REMOVED ? fromVersion : toVersion;

    addScopeReleaseNotes(
        scopeName, scope, pullRequests, messages, releaseNotesFromVersion, releaseNotesToVersion);

    return new ScopeVersionDiffItem(
        scopeName, changeType, fromVersion, toVersion, pullRequests, messages);
  }

  private void addScopeReleaseNotes(
      String scopeName,
      @Nullable Scope scope,
      List<PullRequest> pullRequests,
      List<Message> messages,
      @Nullable String fromVersion,
      @Nullable String toVersion) {
    if (scope == null || scope.getScopeReleaseNotes() == null) {
      return;
    }

    Long fromVersionValue = fromVersion != null ? VersionUtils.toLong(fromVersion) : null;
    Long toVersionValue = toVersion != null ? VersionUtils.toLong(toVersion) : null;
    String bitbucketPrefix = this.audModelConfig.defaultScopesBitbucketUrlPrefix();

    for (ScopeReleaseNote scopeReleaseNote : scope.getScopeReleaseNotes()) {
      if (!isReleaseNoteInRange(scopeReleaseNote, fromVersionValue, toVersionValue)) {
        continue;
      }

      addPullRequests(scopeName, scopeReleaseNote, pullRequests, bitbucketPrefix);
      addMessages(scopeName, scopeReleaseNote, messages, bitbucketPrefix);
    }
  }

  private static boolean isReleaseNoteInRange(
      ScopeReleaseNote scopeReleaseNote,
      @Nullable Long fromVersionValue,
      @Nullable Long toVersionValue) {
    if (fromVersionValue == null && toVersionValue == null) {
      return true;
    }

    long scopeVersion = VersionUtils.toLong(scopeReleaseNote.getScopeVersion());
    if (fromVersionValue != null && scopeVersion <= fromVersionValue) {
      return false;
    }
    if (toVersionValue != null && scopeVersion > toVersionValue) {
      return false;
    }
    return true;
  }

  private static void addPullRequests(
      String scopeName,
      ScopeReleaseNote scopeReleaseNote,
      List<PullRequest> pullRequests,
      String bitbucketPrefix) {
    String prPrefix = bitbucketPrefix + scopeName + "/pull-requests/";
    for (String prNumber : scopeReleaseNote.getPrs()) {
      pullRequests.add(new PullRequest(prNumber, prPrefix + prNumber + "/overview"));
    }
  }

  private static void addMessages(
      String scopeName,
      ScopeReleaseNote scopeReleaseNote,
      List<Message> messages,
      String bitbucketPrefix) {
    String commitPrefix = bitbucketPrefix + scopeName + "/commits/";
    for (V1Commit commit : scopeReleaseNote.getCommits()) {
      messages.add(
          new Message(
              commit.getMessage() != null ? commit.getMessage() : "",
              scopeReleaseNote.getVersionDateTime() != null
                  ? OffsetDateTime.parse(scopeReleaseNote.getVersionDateTime())
                  : null,
              commitPrefix + commit.getCommitid()));
    }
  }

  private static void checkRepository(
      @Nullable ConcurrentRepository<?> repository, String versionNumber, String repoName) {
    if (repository == null || !repository.isReady()) {
      throw new ServiceUnavailableException(
          repoName + " repository is not ready for version: " + versionNumber);
    }
  }

  private static void checkRepository(
      @Nullable BaseRepository<?> repository, String versionNumber, String repoName) {
    if (repository == null || !repository.isReady()) {
      throw new ServiceUnavailableException(
          repoName + " repository is not ready for version: " + versionNumber);
    }
  }
}
