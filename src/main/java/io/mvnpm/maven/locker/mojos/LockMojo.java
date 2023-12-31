package io.mvnpm.maven.locker.mojos;

import static io.mvnpm.maven.locker.LockerConstants.LOCKER_PROFILE;
import static java.util.Locale.ROOT;
import static org.apache.maven.plugins.annotations.ResolutionScope.TEST;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.apache.maven.model.merge.ModelMerger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.google.common.io.Resources;

import io.fabric8.maven.Maven;
import io.fabric8.maven.merge.SmartModelMerger;
import io.mvnpm.maven.locker.model.GAV;
import io.mvnpm.maven.locker.model.ParentPom;
import io.mvnpm.maven.locker.pom.DefaultLockerPom;
import io.mvnpm.maven.locker.pom.LockerPom;
import io.mvnpm.maven.locker.pom.LockerPomFileAccessor;
import io.quarkus.qute.Qute;

@Mojo(name = "lock", requiresDependencyResolution = TEST)
public final class LockMojo extends AbstractDependencyLockMojo {

    @Parameter(property = "locker.filter", defaultValue = "org.mvnpm*,org.webjars*")
    private List<String> filters;

    @Override
    public void execute() throws MojoExecutionException {
        if (project.getActiveProfiles().stream().map(Profile::getId).anyMatch(LOCKER_PROFILE::equals)) {
            throw new MojoExecutionException(
                    "Locking is not possible with '" + LOCKER_PROFILE + "' profile enabled. Use '-P\\!" + LOCKER_PROFILE
                            + "' when locking or add the 'locker-maven-plugin' extension to '.mvn/extensions.xml'.");
        }

        LockerPomFileAccessor lockFile = lockFile();
        final ParentPom parentPom = getParentPom(lockFile.file);
        if (lockFile.exists()) {
            getLog().info(String.format(ROOT, "Updating %s", lockFile.absolutePath()));
        } else {
            getLog().info(String.format(ROOT, "Creating %s", lockFile.absolutePath()));
        }
        final LockerPom lockerPom = DefaultLockerPom.from(lockFile, pomMinimums(), getLog());
        lockerPom.write(parentPom, projectDependencies().filter(filters));
        final Model model = project.getModel();
        final Optional<Profile> existingLockerProfile = model.getProfiles().stream()
                .filter(p -> p.getId().equals(LOCKER_PROFILE)).findFirst();
        final boolean hasActiveByDefaultProfiles = model.getProfiles().stream()
                .anyMatch(p -> p.getActivation().isActiveByDefault());
        if (hasActiveByDefaultProfiles) {
            getLog().warn(
                    "\n\nThe locker profile uses a NEGATED PROPERTY '!unlocked'. This OVERRIDES the 'activeByDefault' option in other profiles.\n\n"
                            +
                            "Your pom.xml contains profiles with 'activeByDefault=true'.\n" +
                            "For CONSISTENT BEHAVIOR, consider REMOVING 'activeByDefault=true' from your profiles or use NEGATED PROPERTIES instead.\n\n");
        }
        if (existingLockerProfile.isEmpty()) {
            getLog().info(
                    "Adding '" + LOCKER_PROFILE + "' profile to the pom.xml...");
            addProfileToPom();
        } else {
            getLog().info("'" + LOCKER_PROFILE + "' profile is present in the pom.xml");
        }

    }

    private ParentPom getParentPom(Path lockerPom) {
        if (project.getParent() != null) {
            final String relativeParentPath = getRelativeParentPath(lockerPom);
            if (relativeParentPath != null) {
                return new ParentPom(GAV.from(project.getParent()), relativeParentPath);
            }
        }
        return null;
    }

    public String getRelativeParentPath(Path lockerPom) {
        final Path parentPath = project.getParent().getFile().toPath().toAbsolutePath();
        Path parentDirPath = parentPath.getParent();

        if (lockerPom.startsWith(parentDirPath)) {
            return lockerPom.getParent().relativize(parentPath).toString();
        } else {
            return null;
        }
    }

    private void addProfileToPom() throws MojoExecutionException {
        final Model model = Maven.readModel(project.getFile().toPath());
        final Model locker;
        try {
            final String content = getLockerProfile();
            locker = Maven.readModel(new StringReader(content));
        } catch (IOException e) {
            throw new MojoExecutionException(e);
        }
        ModelMerger merger = new SmartModelMerger();
        merger.merge(model, locker, false, Map.of());
        Maven.writeModel(model);
    }

    private String getLockerProfile() throws IOException {
        final String tpl = Resources.toString(Resources.getResource(LockMojo.class, "locker-profile.xml"),
                StandardCharsets.UTF_8);
        final Map<String, Object> data = Map.of(
                "useNegatedProp", true,
                "lockerProfile", LOCKER_PROFILE,
                "groupId", project.getGroupId(),
                "artifactId", project.getArtifactId());
        return Qute.fmt(tpl, data);
    }
}
