package io.mvnpm.maven.locker.pom;

import io.mvnpm.maven.locker.model.Artifacts;

public interface LockerPom {

    void write(Artifacts projectDependencies);

    Artifacts read();
}
