package io.mvnpm.maven.locker.pom;

import io.mvnpm.maven.locker.model.Artifacts;
import io.mvnpm.maven.locker.model.ParentPom;

public interface LockerPom {

    void write(ParentPom parent, Artifacts projectDependencies);

    Artifacts read();
}
