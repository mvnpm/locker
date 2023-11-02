package io.mvnpm.maven.locker.model;

import static java.util.Objects.requireNonNull;

import org.apache.maven.project.MavenProject;

public final class GAV {

  public final String groupId;
  public final String artifactId;
  public final String version;

  public static GAV from(MavenProject mavenProject) {
    return new GAV(
        mavenProject.getGroupId(), mavenProject.getArtifactId(), mavenProject.getVersion());
  }

  GAV(String groupId, String artifactId, String version) {
    this.groupId = requireNonNull(groupId);
    this.artifactId = requireNonNull(artifactId);
    this.version = requireNonNull(version);
  }
}
