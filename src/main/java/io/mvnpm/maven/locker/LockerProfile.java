package io.mvnpm.maven.locker;

import static io.mvnpm.maven.locker.LockerConstants.LOCKER_PROFILE;

import java.util.List;
import java.util.Optional;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;

public final class LockerProfile {

    private LockerProfile() {
    }

    public static Optional<Profile> findLockerProfile(Model model) {
        return model.getProfiles().stream()
                .filter(p -> p.getId().equals(LOCKER_PROFILE)).findFirst();
    }

    public static boolean usesLockerBom(Optional<Profile> profile) {
        if (profile.isEmpty()) {
            return false;
        }
        final List<Dependency> dependencies = profile.get().getDependencyManagement().getDependencies();
        return dependencies.size() == 1 && dependencies.get(0).getArtifactId().endsWith("-locker");
    }
}
