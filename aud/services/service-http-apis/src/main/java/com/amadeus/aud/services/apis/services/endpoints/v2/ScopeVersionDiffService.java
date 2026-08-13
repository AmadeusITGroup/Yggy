package com.amadeus.aud.services.apis.services.endpoints.v2;

import static com.amadeus.aud.services.apis.services.endpoints.v2.PaginationDefaults.DEFAULT_PAGE_NUM;
import static com.amadeus.aud.services.apis.services.endpoints.v2.PaginationDefaults.DEFAULT_PAGE_SIZE;
import static com.amadeus.aud.services.apis.services.endpoints.v2.PaginationDefaults.MAX_PAGE_SIZE;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.backend.models.v2.Metadata;
import com.amadeus.aud.libraries.backend.models.v2.Pagination;
import com.amadeus.aud.libraries.backend.models.v2.Response;
import com.amadeus.aud.libraries.backend.models.v2.ScopeVersionDiffItem;
import com.amadeus.aud.libraries.backend.models.v2.ScopeVersionDiffItem.ChangeType;
import com.amadeus.aud.libraries.backend.models.v2.ScopeVersionDiffItem.Message;
import com.amadeus.aud.libraries.backend.models.v2.ScopeVersionDiffItem.PullRequest;
import com.amadeus.aud.libraries.common.Constants;
import com.amadeus.aud.libraries.engine.data.configs.AudModelConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudRequirementsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopesRepository;
import com.amadeus.aud.libraries.engine.data.services.external.storage.AudVersionsService;
import com.amadeus.aud.libraries.engine.data.services.external.storage.SingleVersion;
import com.amadeus.aud.libraries.models.core.db.Requirement;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.ScopeReleaseNote;
import com.amadeus.aud.libraries.models.core.utils.VersionUtils;
import com.amadeus.aud.libraries.yaml.models.V1Commit;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.BadRequestException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Singleton
public class ScopeVersionDiffService extends BaseService {

  private final AudService audService;
  private final AudVersionsService audVersionService;
  private final AudModelConfig audModelConfig;

  @Inject
  public ScopeVersionDiffService(
      AudService audService, AudVersionsService audVersionService, AudModelConfig audModelConfig) {
    this.audService = audService;
    this.audVersionService = audVersionService;
    this.audModelConfig = audModelConfig;
  }

  @CacheResult(cacheName = "getScopeVersionDiffItemsV2")
  public Uni<List<ScopeVersionDiffItem>> getScopeVersionDiffItems(
      String oldVersionId, String newVersionId) {
    validateVersionIds(oldVersionId, newVersionId);

    return Uni.combine()
        .all()
        .unis(audVersionService.listAllVersions(), audVersionService.getLatestVersion())
        .asTuple()
        .onItem()
        .transformToUni(
            (Tuple2<List<SingleVersion>, String> versionsTuple) -> {
              List<SingleVersion> availableVersions = versionsTuple.getItem1();
              String latestVersionId = versionsTuple.getItem2();

              validateVersionExists(oldVersionId, "oldVersion", availableVersions, latestVersionId);
              validateVersionExists(newVersionId, "newVersion", availableVersions, latestVersionId);

              return Uni.combine()
                  .all()
                  .unis(
                      audService.getVersionData(oldVersionId),
                      audService.getVersionData(newVersionId))
                  .asTuple()
                  .onItem()
                  .transformToUni(
                      (Tuple2<Version, Version> tuple) -> {
                        validateVersionOrder(tuple.getItem1(), tuple.getItem2(), availableVersions);
                        return getScopeVersionDiffItems(tuple.getItem1(), tuple.getItem2());
                      });
            });
  }

  public Uni<List<ScopeVersionDiffItem>> getScopeVersionDiffItems(
      Version oldVersion, Version newVersion) {
    return Uni.createFrom()
        .item(Unchecked.supplier(() -> computeDiffItems(oldVersion, newVersion)))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool());
  }

  public Uni<Response<List<ScopeVersionDiffItem>>> getScopeVersionDiffResponse(
      String oldVersionId,
      String newVersionId,
      @Nullable Integer pageNum,
      @Nullable Integer pageSize) {
    validatePagination(pageNum, pageSize);
    return getScopeVersionDiffItems(oldVersionId, newVersionId)
        .onItem()
        .transform(items -> paginate(items, pageNum, pageSize));
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

  private static void validateVersionIds(String oldVersionId, String newVersionId) {
    if (oldVersionId == null || oldVersionId.isBlank()) {
      throw new BadRequestException("oldVersion query parameter is required");
    }
    if (newVersionId == null || newVersionId.isBlank()) {
      throw new BadRequestException("newVersion query parameter is required");
    }
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

  private static void validateVersionExists(
      String versionId,
      String parameterName,
      List<SingleVersion> availableVersions,
      @Nullable String latestVersionId) {
    if (Constants.LATEST.equalsIgnoreCase(versionId)) {
      return;
    }

    if (Objects.equals(versionId, latestVersionId)) {
      return;
    }

    for (SingleVersion availableVersion : availableVersions) {
      if (Objects.equals(availableVersion.version(), versionId)) {
        return;
      }
    }

    throw new BadRequestException("%s must reference an existing version".formatted(parameterName));
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
}
