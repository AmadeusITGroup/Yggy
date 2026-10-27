package com.amadeus.aud.services.apis.services.endpoints;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.common.UrlUtils;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.configs.AudModelConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudRequirementsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopesRepository;
import com.amadeus.aud.libraries.models.core.db.Requirement;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.ScopeReleaseNote;
import com.amadeus.aud.libraries.models.core.utils.VersionUtils;
import com.amadeus.aud.libraries.yaml.models.Message;
import com.amadeus.aud.libraries.yaml.models.PR;
import com.amadeus.aud.libraries.yaml.models.ScopeDiff;
import com.amadeus.aud.libraries.yaml.models.ScopeDiffResponse;
import com.amadeus.aud.libraries.yaml.models.ScopeDiffs;
import com.amadeus.aud.libraries.yaml.models.V1Commit;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Singleton
public class ScopeDiffService extends BaseService {
  private final AudService audService;
  private final AudApiConfig audApiConfig;
  private final AudModelConfig audModelConfig;

  @Inject
  public ScopeDiffService(
      AudService audService, AudApiConfig audApiConfig, AudModelConfig audModelConfig) {
    this.audService = audService;
    this.audApiConfig = audApiConfig;
    this.audModelConfig = audModelConfig;
  }

  public Uni<Response> getScopeVersionsDiff(String oldVersion, String newVersion, UriInfo uriInfo) {
    return Uni.combine()
        .all()
        .unis(audService.getVersionData(oldVersion), audService.getVersionData(newVersion))
        .asTuple()
        .onItem()
        .transformToUni(tuple -> getScopeVersionsDiff(tuple.getItem1(), tuple.getItem2(), uriInfo));
  }

  public Uni<Response> getScopeVersionsDiff(
      Version oldVersion, Version newVersion, UriInfo uriInfo) {
    return Uni.createFrom()
        .item(
            Unchecked.supplier(() -> getScopeVersionsDiffResponse(oldVersion, newVersion, uriInfo)))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultExecutor());
  }

  private Response getScopeVersionsDiffResponse(
      Version oldVersion, Version newVersion, UriInfo uriInfo) throws JsonProcessingException {
    String newVersionNumber = newVersion.getVersionNumber();

    AudRequirementsRepository oldRequirementsRepository = oldVersion.getAudRequirementsRepository();
    checkRepository(oldRequirementsRepository, oldVersion.getVersionNumber(), "Requirements");

    AudRequirementsRepository newRequirementsRepository = newVersion.getAudRequirementsRepository();
    checkRepository(newRequirementsRepository, newVersionNumber, "Requirements");

    ScopesRepository scopesRepository = newVersion.getScopesRepository();
    checkRepository(scopesRepository, newVersionNumber, "Scopes");

    ScopeDiffs scopeDiffs = new ScopeDiffs();

    // Create maps for easier lookup and avoid nested loops
    Map<String, Requirement> oldRequirementsMap = new HashMap<>();
    for (Requirement req : oldRequirementsRepository.getData().requirements()) {
      oldRequirementsMap.put(req.getScope(), req);
    }
    Map<String, Requirement> newRequirementsMap = new HashMap<>();
    for (Requirement req : newRequirementsRepository.getData().requirements()) {
      newRequirementsMap.put(req.getScope(), req);
    }

    Set<String> allScopes = new HashSet<>();
    allScopes.addAll(oldRequirementsMap.keySet());
    allScopes.addAll(newRequirementsMap.keySet());

    for (String scopeName : allScopes) {
      Requirement oldRequirement = oldRequirementsMap.get(scopeName);
      Requirement newRequirement = newRequirementsMap.get(scopeName);

      ScopeDiff scopeDiff =
          createScopeDiff(scopeName, oldRequirement, newRequirement, scopesRepository);
      if (scopeDiff != null) {
        scopeDiffs.addDiffItem(scopeDiff);
      }
    }

    ScopeDiffResponse response = new ScopeDiffResponse();
    response.setScopes(scopeDiffs);
    response.setApiVersion(audApiConfig.version());
    response.setUrl(UrlUtils.getUrl(uriInfo));
    return getOkResponse(response);
  }

  private ScopeDiff createScopeDiff(
      String scopeName,
      Requirement oldRequirement,
      Requirement newRequirement,
      ScopesRepository scopesRepository) {

    ScopeDiff result = null;

    if (oldRequirement == null && newRequirement != null) {
      // Scope added
      result = createNewScopeDiff(scopeName, "N/A", newRequirement.getVersion(), scopesRepository);
    } else if (oldRequirement != null && newRequirement == null) {
      // Scope removed
      result = createNewScopeDiff(scopeName, oldRequirement.getVersion(), "N/A", scopesRepository);
    } else if (oldRequirement != null
        && newRequirement != null
        && !Objects.equals(newRequirement.getVersion(), oldRequirement.getVersion())) {
      // Version changed - get release notes
      result = createChangedScopeDiff(scopeName, oldRequirement, newRequirement, scopesRepository);
    }

    return result;
  }

  private ScopeDiff createNewScopeDiff(
      String scopeName, String oldVersion, String newVersion, ScopesRepository scopesRepository) {
    ScopeDiff scopeDiff = new ScopeDiff();
    scopeDiff.setScope(scopeName);
    scopeDiff.setOldVersion(oldVersion);
    scopeDiff.setNewVersion(newVersion);

    Scope scope = scopesRepository.getData().get(scopeName);
    List<PR> prs = new ArrayList<>();
    List<Message> msgs = new ArrayList<>();
    if (scope != null) {
      Set<ScopeReleaseNote> scopeReleaseNotes = scope.getScopeReleaseNotes();
      if (scopeReleaseNotes != null) {
        for (ScopeReleaseNote scopeReleaseNote : scopeReleaseNotes) {
          addScopeReleaseNoteToLists(scopeName, scopeReleaseNote, prs, msgs);
        }
      }
    }
    scopeDiff.setPrs(prs);
    scopeDiff.setMessages(msgs);
    return scopeDiff;
  }

  private ScopeDiff createChangedScopeDiff(
      String scopeName,
      Requirement oldRequirement,
      Requirement newRequirement,
      ScopesRepository scopesRepository) {
    String oldVersion = oldRequirement.getVersion();
    String newVersion = newRequirement.getVersion();

    ScopeDiff scopeDiff = new ScopeDiff();
    scopeDiff.setScope(scopeName);
    scopeDiff.setOldVersion(oldVersion);
    scopeDiff.setNewVersion(newVersion);

    Scope scope = scopesRepository.getData().get(scopeName);
    List<PR> prs = new ArrayList<>();
    List<Message> msgs = new ArrayList<>();
    if (scope != null) {
      Set<ScopeReleaseNote> scopeReleaseNotes = scope.getScopeReleaseNotes();
      if (scopeReleaseNotes != null) {
        for (ScopeReleaseNote scopeReleaseNote : scopeReleaseNotes) {
          long scopeVersion = VersionUtils.toLong(scopeReleaseNote.getScopeVersion());

          if (scopeVersion > VersionUtils.toLong(oldVersion)
              && scopeVersion <= VersionUtils.toLong(newVersion)) {
            addScopeReleaseNoteToLists(scopeName, scopeReleaseNote, prs, msgs);
          }
        }
      }
    }
    scopeDiff.setPrs(prs);
    scopeDiff.setMessages(msgs);
    return scopeDiff;
  }

  private void addScopeReleaseNoteToLists(
      String scopeName, ScopeReleaseNote scopeReleaseNote, List<PR> prs, List<Message> msgs) {
    List<String> prlist = scopeReleaseNote.getPrs();
    for (String prNumber : prlist) {
      PR pr = new PR();
      pr.setNumber(prNumber);
      pr.setUrl(
          new StringBuilder(this.audModelConfig.defaultScopesBitbucketUrlPrefix())
              .append(scopeName)
              .append("/pull-requests/")
              .append(prNumber)
              .append("/overview")
              .toString());
      prs.add(pr);
    }

    List<V1Commit> commits = scopeReleaseNote.getCommits();
    for (V1Commit commit : commits) {
      Message message = new Message();
      message.setMessage(commit.getMessage() != null ? commit.getMessage() : "");
      message.setTimestamp(
          scopeReleaseNote.getVersionDateTime() != null
              ? scopeReleaseNote.getVersionDateTime()
              : "");
      message.setUrl(
          new StringBuilder(this.audModelConfig.defaultScopesBitbucketUrlPrefix())
              .append(scopeName)
              .append("/commits/")
              .append(commit.getCommitid())
              .toString());
      msgs.add(message);
    }
  }
}
