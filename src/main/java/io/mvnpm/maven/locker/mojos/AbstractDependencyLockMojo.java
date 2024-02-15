package io.mvnpm.maven.locker.mojos;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import io.mvnpm.maven.locker.model.Artifacts;
import io.mvnpm.maven.locker.model.GAV;
import io.mvnpm.maven.locker.pom.LockerPomFileAccessor;

public abstract class AbstractDependencyLockMojo extends AbstractMojo {

    @Parameter(defaultValue = "${basedir}", required = true, readonly = true)
    protected File basedir;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject project;

    protected LockerPomFileAccessor lockFile() {
        return LockerPomFileAccessor.fromBasedir(basedir.toPath());
    }

    protected Artifacts projectDependencies() {
        return Artifacts.fromMavenArtifacts(project.getArtifacts());
    }

    protected GAV pomMinimums() {
        return GAV.from(project);
    }

    protected String projectVersion() {
        return project.getVersion();
    }
}
