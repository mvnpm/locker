package io.mvnpm.maven.locker.model;

import static java.util.Objects.requireNonNull;

public final class ParentPom {

    GAV gav;
    public final String relativePath;

    public ParentPom(GAV gav, String relativePath) {
        this.gav = requireNonNull(gav);
        this.relativePath = requireNonNull(relativePath);
    }

    public String getGroupId() {
        return gav.groupId;
    }

    public String getArtifactId() {
        return gav.artifactId;
    }

    public String getVersion() {
        return gav.version;
    }
}
