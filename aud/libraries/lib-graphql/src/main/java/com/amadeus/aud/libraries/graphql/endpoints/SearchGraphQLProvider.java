package com.amadeus.aud.libraries.graphql.endpoints;

import static com.amadeus.aud.libraries.graphql.utils.GraphQLUtils.DEFAULT_PAGE_NUMBER;
import static com.amadeus.aud.libraries.graphql.utils.GraphQLUtils.DEFAULT_PAGE_SIZE;
import static com.amadeus.aud.libraries.graphql.utils.GraphQLUtils.DEFAULT_VERSION;
import static com.amadeus.aud.libraries.graphql.utils.GraphQLUtils.PARAM_PAGE_NUMBER;
import static com.amadeus.aud.libraries.graphql.utils.GraphQLUtils.PARAM_PAGE_SIZE;
import static com.amadeus.aud.libraries.graphql.utils.GraphQLUtils.PARAM_VERSION;
import static com.amadeus.aud.libraries.graphql.utils.GraphQLUtils.toPage;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.graphql.dtos.SearchResultGraphQL;
import com.amadeus.aud.libraries.graphql.utils.PageGraphQL;
import com.amadeus.aud.libraries.models.core.db.ScopeVersionMapping;
import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.models.core.db.base.BomElement;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Size;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class SearchGraphQLProvider {

  private static final Map<String, String> repositoryKeyToEnumName;
  private final AudService audService;

  static {
    repositoryKeyToEnumName = new java.util.HashMap<>();
    for (ObjectTypeListNamesEnum enumValue : ObjectTypeListNamesEnum.values()) {
      repositoryKeyToEnumName.put(enumValue.getRepositoryKey(), enumValue.getUri());
    }
  }

  @Inject
  public SearchGraphQLProvider(AudService audService) {
    this.audService = audService;
  }

  private static String resolveEntityType(Version version, Class<?> runtimeClass) {
    String runtimeSimpleName = runtimeClass.getSimpleName();

    String repositoryKey = version.getRepositoryNameFromSingleType(runtimeClass);
    if (repositoryKey != null && !repositoryKey.isBlank()) {
      return repositoryKeyToEnumName.getOrDefault(repositoryKey, runtimeSimpleName);
    }

    Class<?> parentClass = runtimeClass.getSuperclass();
    if (parentClass != null) {
      String parentRepositoryKey = version.getRepositoryNameFromSingleType(parentClass);
      if (parentRepositoryKey != null && !parentRepositoryKey.isBlank()) {
        return repositoryKeyToEnumName.getOrDefault(parentRepositoryKey, runtimeSimpleName);
      }
    }

    return runtimeSimpleName;
  }

  @Timeout(value = 30, unit = ChronoUnit.SECONDS)
  @Query("search")
  public PageGraphQL<SearchResultGraphQL> search(
      @Name(PARAM_VERSION) @DefaultValue(DEFAULT_VERSION) String versionNumber,
      @Name(AbstractBaseMixin.JSON_PROPERTY_NAME)
          @Size(min = 2, message = "Name must be at least 2 characters")
          String name,
      @Name("type") String type,
      @Name(PARAM_PAGE_NUMBER) @DefaultValue(DEFAULT_PAGE_NUMBER) int pageNumber,
      @Name(PARAM_PAGE_SIZE) @DefaultValue(DEFAULT_PAGE_SIZE) int pageSize) {
    Version version = audService.getVersion(versionNumber).await().indefinitely();

    String query = name != null ? name.toLowerCase(Locale.ROOT) : "";
    String typeFilter = type != null ? type.toLowerCase(Locale.ROOT) : null;
    int queryLength = query.length();
    List<SearchResultGraphQL> filteredSearchResults = new ArrayList<>();

    Iterator<BomElement> it = version.all();
    while (it.hasNext()) {
      BomElement element = it.next();
      if (!(element instanceof AbstractBase<?> base)) {
        continue;
      }

      String entityName = base.getName();
      if (entityName == null) {
        continue;
      }

      String lowerName = entityName.toLowerCase(Locale.ROOT);

      // Exact match → relevance 0 (best)
      // startsWith → small positive value (index position in the name)
      // contains   → index + length difference, so deeper/longer matches rank lower
      int matchIndex = lowerName.indexOf(query);
      if (matchIndex < 0) {
        continue;
      }

      String entityType = resolveEntityType(version, base.getClass());

      boolean typeMatches =
          !entityType.equals(ObjectTypeListNamesEnum.SCOPERELEASENOTES.getUri())
              && !entityType.equals(ScopeVersionMapping.class.getSimpleName())
              && (typeFilter == null || entityType.toLowerCase(Locale.ROOT).equals(typeFilter));

      if (typeMatches) {
        int relevance;
        boolean exactMatch = lowerName.length() == queryLength;
        if (exactMatch) {
          relevance = 0;
        } else {
          // matchIndex gives "how far into the string" the match starts
          // lengthDiff penalises longer names that only partially match
          relevance = matchIndex + (lowerName.length() - queryLength);
        }

        com.amadeus.aud.libraries.models.core.db.Scope s = base.getScope();
        String scopeName = s != null ? s.getName() : null;
        filteredSearchResults.add(
            new SearchResultGraphQL(entityType, entityName, base.id(), scopeName, relevance));
      }
    }

    // natural order by  relevance
    filteredSearchResults.sort(null);

    return toPage(filteredSearchResults, pageNumber, pageSize, f -> f);
  }
}
