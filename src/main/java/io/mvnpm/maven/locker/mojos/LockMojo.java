package io.mvnpm.maven.locker.mojos;

import static io.mvnpm.maven.locker.LockerConstants.LOCKER_PROFILE;
import static io.mvnpm.maven.locker.LockerProfile.findLockerProfile;
import static io.mvnpm.maven.locker.LockerProfile.usesLockerBom;
import static io.mvnpm.maven.locker.mojos.LockerMode.IN_PROFILE;
import static io.mvnpm.maven.locker.mojos.LockerMode.LOCKER_BOM;
import static java.util.Locale.ROOT;
import static org.apache.maven.plugins.annotations.ResolutionScope.TEST;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
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
import io.mvnpm.maven.locker.model.Artifacts;
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

    @Parameter(property = "locker.mode")
    private LockerMode mode;

    @Override
    public void execute() throws MojoExecutionException {
        if (project.getActiveProfiles().stream().map(Profile::getId).anyMatch(LOCKER_PROFILE::equals)) {
            throw new MojoExecutionException(
                    "Locking is not possible with '" + LOCKER_PROFILE
                            + "' profile enabled. Use '-Dunlocked' when locking or add the 'locker-maven-plugin' extension to '.mvn/extensions.xml'.");
        }

        LockerPomFileAccessor lockFile = lockFile();
        final Artifacts lockedDependencies = projectDependencies().filter(filters);
        final boolean lockFileExists = lockFile.exists();
        final Model model = project.getModel();
        final Optional<Profile> existingLockerProfile = findLockerProfile(model);
        final boolean alreadyConfiguredWithLockerBom = usesLockerBom(existingLockerProfile) || lockFileExists;
        if (alreadyConfiguredWithLockerBom) {
            getLog().info("Configured with locker BOM Mode");
            if (IN_PROFILE.equals(mode)) {
                getLog().warn("Ignoring 'locker.mode' parameter, the project is already configured with Locker BOM Mode");
            }
        }

        boolean lockerBomModeEnabled = LOCKER_BOM.equals(mode) || alreadyConfiguredWithLockerBom;
        if (existingLockerProfile.isEmpty() && mode == null) {
            lockerBomModeEnabled = true;
        }
        if (lockerBomModeEnabled) {
            getLog().info(String.format(ROOT, "%s %s", lockFileExists ? "Updating" : "Creating", lockFile.absolutePath()));
            final LockerPom lockerPom = DefaultLockerPom.from(lockFile, pomMinimums(), getLog());
            final ParentPom parentPom = getParentPom(lockFile.file);
            lockerPom.write(parentPom, lockedDependencies);
        }

        boolean addLockerProfile = false;
        if (existingLockerProfile.isEmpty()) {
            if (lockerBomModeEnabled) {
                getLog().info(
                        "Adding '" + LOCKER_PROFILE + "' profile with the Locker BOM to the pom.xml...");
            } else {
                getLog().info(
                        "Adding '" + LOCKER_PROFILE + "' profile with " + lockedDependencies.artifacts.size()
                                + " locked dependencies to the pom.xml...");
            }
            addLockerProfile = true;
        } else {
            if (existingLockerProfile.get().getActivation().isActiveByDefault()) {
                getLog().info("Switching to '!unlocked' property activation in '" + LOCKER_PROFILE + "' profile");
                addLockerProfile = true;
            }
            if (!lockerBomModeEnabled) {
                getLog().info("Updating '" + LOCKER_PROFILE + "' profile with locked dependencies");
                addLockerProfile = true;
            } else if (!usesLockerBom(existingLockerProfile)) {
                addLockerProfile = true;
                getLog().info("Switching to locker BOM in '" + LOCKER_PROFILE + "' profile");
            }
        }
        Model newModel = model;
        if (addLockerProfile) {
            newModel = addProfileToPom(lockerBomModeEnabled ? null : lockedDependencies);
        } else {
            getLog().info("No changes to the project pom.xml");
        }
        final boolean hasActiveByDefaultProfiles = newModel.getProfiles().stream()
                .anyMatch(p -> p.getActivation().isActiveByDefault());
        if (hasActiveByDefaultProfiles) {
            getLog().warn(
                    "\n\nThe locker profile uses a negated property '!unlocked' for activation, this disables other profiles with 'activeByDefault'.\n\n"
                            +
                            "Your pom.xml contains other profiles with 'activeByDefault=true'.\n" +
                            "Consider replacing 'activeByDefault=true' from your profiles by property activation (i.e use !foo to be active unless -Dfoo).\n\n");
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

    private Model addProfileToPom(Artifacts lockedDependencies) throws MojoExecutionException {
        final Model model = Maven.readModel(project.getFile().toPath());
        final Model locker;
        try {
            final String content = getLockerProfile(lockedDependencies);
            locker = Maven.readModel(new StringReader(content));
        } catch (IOException e) {
            throw new MojoExecutionException(e);
        }
        ModelMerger merger = new SmartModelMerger();
        findLockerProfile(model).ifPresent(model::removeProfile);
        merger.merge(model, locker, false, Map.of());
        Maven.writeModel(model);
        return model;
    }

    private String getLockerProfile(Artifacts lockedDependencies) throws IOException {
        final String tpl = Resources.toString(Resources.getResource(LockMojo.class, "locker-profile.xml"),
                StandardCharsets.UTF_8);
        final Map<String, Object> data = new HashMap<>();
        data.put("lockedDependencies", lockedDependencies);
        data.put("useNegatedProp", true);
        data.put("lockerProfile", LOCKER_PROFILE);
        data.put("groupId", project.getGroupId());
        data.put("artifactId", project.getArtifactId());
        return Qute.fmt(tpl, data);
    }
}
