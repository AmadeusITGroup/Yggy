package com.amadeus.aud.libraries.models.core.db;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import java.io.File;
import java.util.List;
import java.util.Objects;

@Entity
public class Requirement extends PanacheEntity {
  private int hashCode;

  private String name;
  private String scope;
  private String comment;
  private String repo;
  @Transient private List<File> files;
  private String version;

  public Requirement() {}

  public Requirement(
      String name, String scope, String comment, String repo, List<File> files, String version) {
    this.name = name;
    this.scope = scope;
    this.comment = comment;
    this.repo = repo;
    this.files = files;
    this.version = version;

    this.hashCode = Objects.hash(name, scope, comment, repo, files, version);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    return (o instanceof Requirement r)
        && Objects.equals(r.name, this.name)
        && Objects.equals(r.scope, this.scope)
        && Objects.equals(r.comment, this.comment)
        && Objects.equals(r.repo, this.repo)
        && Objects.equals(r.files, this.files)
        && Objects.equals(r.version, this.version);
  }

  @Override
  public int hashCode() {
    if (this.hashCode == 0) {
      this.hashCode = Objects.hash(name, scope, comment, repo, files, version);
    }
    return this.hashCode;
  }

  // Getters and setters
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getRepo() {
    return repo;
  }

  public void setRepo(String repo) {
    this.repo = repo;
  }

  public List<File> getFiles() {
    return files;
  }

  public void setFiles(List<File> files) {
    this.files = files;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }
}
