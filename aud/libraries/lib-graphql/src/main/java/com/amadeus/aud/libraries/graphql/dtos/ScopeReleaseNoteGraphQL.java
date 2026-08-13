package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.models.core.db.ScopeReleaseNote;
import com.amadeus.aud.libraries.yaml.models.V1Commit;
import com.amadeus.aud.libraries.yaml.models.V1Device;
import jakarta.annotation.Nullable;
import java.util.List;
import org.eclipse.microprofile.graphql.Type;

@Type("ScopeReleaseNoteDTO")
public record ScopeReleaseNoteGraphQL(
    String id,
    String type,
    String name,
    String scopeName,
    String scopeVersion,
    String audDataVersion,
    String dateTime,
    List<V1Commit> commits,
    List<String> prs,
    List<V1Device> impactedDevices) {

  public static @Nullable ScopeReleaseNoteGraphQL from(
      @Nullable ScopeReleaseNote releaseNote, @Nullable String audDataVersion) {
    if (releaseNote == null) {
      return null;
    }
    return new ScopeReleaseNoteGraphQL(
        releaseNote.id(),
        ObjectTypeListNamesEnum.SCOPERELEASENOTES.getUri(),
        releaseNote.getName(),
        releaseNote.getScopeName(),
        releaseNote.getScopeVersion(),
        audDataVersion,
        releaseNote.getVersionDateTime(),
        releaseNote.getCommits(),
        releaseNote.getPrs(),
        releaseNote.getImpactedDevices());
  }
}
