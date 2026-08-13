package com.amadeus.aud.libraries.graphql.endpoints;

import static com.amadeus.aud.libraries.graphql.utils.GraphQLUtils.DEFAULT_PAGE_NUMBER;
import static com.amadeus.aud.libraries.graphql.utils.GraphQLUtils.DEFAULT_PAGE_SIZE;
import static com.amadeus.aud.libraries.graphql.utils.GraphQLUtils.DEFAULT_VERSION;
import static com.amadeus.aud.libraries.graphql.utils.GraphQLUtils.PARAM_PAGE_NUMBER;
import static com.amadeus.aud.libraries.graphql.utils.GraphQLUtils.PARAM_PAGE_SIZE;
import static com.amadeus.aud.libraries.graphql.utils.GraphQLUtils.PARAM_VERSION;
import static com.amadeus.aud.libraries.graphql.utils.GraphQLUtils.toPage;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.graphql.dtos.ScopeReleaseNoteGraphQL;
import com.amadeus.aud.libraries.graphql.filters.DeviceFilter;
import com.amadeus.aud.libraries.graphql.filters.ScopeReleaseNoteFilter;
import com.amadeus.aud.libraries.graphql.utils.PageGraphQL;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.ScopeReleaseNote;
import com.amadeus.aud.libraries.models.core.db.ScopeVersionMapping;
import com.amadeus.aud.libraries.models.core.utils.VersionUtils;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class ScopeReleaseNoteGraphQLProvider {

  private final AudService audService;

  @Inject
  public ScopeReleaseNoteGraphQLProvider(AudService audService) {
    this.audService = audService;
  }

  @Timeout(value = 30, unit = ChronoUnit.SECONDS)
  @Query(ScopeReleaseNoteFilter.PROVIDER)
  public PageGraphQL<ScopeReleaseNoteGraphQL> getScopeReleaseNotes(
      @Name(PARAM_VERSION) @DefaultValue(DEFAULT_VERSION) String versionNumber,
      @Name(AbstractBaseMixin.JSON_PROPERTY_ID) StringFilter id,
      @Name("scopeName") StringFilter scopeName,
      @Name("scopeVersion") StringFilter scopeVersion,
      @Name("impactedDevices") DeviceFilter impactedDevices,
      @Name(PARAM_PAGE_NUMBER) @DefaultValue(DEFAULT_PAGE_NUMBER) int pageNumber,
      @Name(PARAM_PAGE_SIZE) @DefaultValue(DEFAULT_PAGE_SIZE) int pageSize) {

    Version version = audService.getVersion(versionNumber).await().indefinitely();

    ScopeReleaseNoteFilter filter =
        new ScopeReleaseNoteFilter(scopeName, scopeVersion, impactedDevices);
    Map<String, Scope> scopesByName = version.getScopesRepository().getData();

    List<ScopeReleaseNote> filteredNotes = new ArrayList<>();
    for (ScopeReleaseNote notes : version.getScopeReleaseNotesRepository().getData().values()) {
      if (filter.matches(notes)) {
        filteredNotes.add(notes);
      }
    }

    filteredNotes.sort(
        (ScopeReleaseNote leftScopeReleaseNote, ScopeReleaseNote rightScopeReleaseNote) -> {
          int versionCompare =
              compareScopeVersionsDesc(leftScopeReleaseNote, rightScopeReleaseNote);
          if (versionCompare != 0) {
            return versionCompare;
          }
          return Comparator.comparing(
                  ScopeReleaseNote::getVersionDateTime,
                  Comparator.nullsLast(Comparator.reverseOrder()))
              .compare(leftScopeReleaseNote, rightScopeReleaseNote);
        });

    return toPage(
        filteredNotes,
        pageNumber,
        pageSize,
        releaseNote ->
            ScopeReleaseNoteGraphQL.from(
                releaseNote,
                resolveAudDataVersion(scopesByName.get(releaseNote.getScopeName()), releaseNote)));
  }

  private String resolveAudDataVersion(@Nullable Scope scope, ScopeReleaseNote releaseNote) {
    if (scope == null
        || scope.getScopeVersionMapping() == null
        || releaseNote.getScopeVersion() == null) {
      return null;
    }

    for (ScopeVersionMapping mapping : scope.getScopeVersionMapping()) {
      if (releaseNote.getScopeVersion().equals(mapping.getScopeVersion())) {
        return mapping.getAudVersion();
      }
    }

    return null;
  }

  private int compareScopeVersionsDesc(
      ScopeReleaseNote leftScopeReleaseNote, ScopeReleaseNote rightScopeReleaseNote) {
    String leftVersion = leftScopeReleaseNote.getScopeVersion();
    String rightVersion = rightScopeReleaseNote.getScopeVersion();

    if (leftVersion == null && rightVersion == null) {
      return 0;
    }
    if (leftVersion == null) {
      return 1;
    }
    if (rightVersion == null) {
      return -1;
    }

    boolean isLeftScopeReleaseNoteSemver = VersionUtils.isSemver(leftVersion);
    boolean isRightScopeReleaseNoteSemver = VersionUtils.isSemver(rightVersion);

    if (isLeftScopeReleaseNoteSemver && isRightScopeReleaseNoteSemver) {
      return Long.compare(VersionUtils.toLong(rightVersion), VersionUtils.toLong(leftVersion));
    }

    return rightVersion.compareTo(leftVersion);
  }
}
