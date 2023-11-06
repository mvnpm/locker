package io.mvnpm.maven.locker.extensions;

import io.fabric8.maven.Maven;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.codehaus.plexus.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static io.mvnpm.maven.locker.InstallLocker.installLocker;
import static io.mvnpm.maven.locker.LockerConstants.LOCKER_POM_PATH;
import static io.mvnpm.maven.locker.LockerConstants.LOCKER_PROFILE;
import static io.mvnpm.maven.locker.LockerConstants.LOCK_GOAL_PREDICATE;

@Singleton
@Named("locker")
public class LockerExtension extends AbstractMavenLifecycleParticipant {

    private final Logger logger;

    @Inject
    public LockerExtension(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void afterSessionStart(MavenSession session)
            throws MavenExecutionException {

        final Path lockerPom = Path.of(session.getRequest().getBaseDirectory(), LOCKER_POM_PATH);

        if (session.getGoals().stream().anyMatch(LOCK_GOAL_PREDICATE)) {
            prepareForLocking(session);
        } else {
            // build
            prepareForBuilding(session, lockerPom);
        }
    }

    private void prepareForBuilding(MavenSession session, Path lockerPom) throws MavenExecutionException {
        final Model model = Maven.readModel(session.getRequest().getPom().toPath());
        final Optional<Profile> lockerProfile = model.getProfiles().stream()
                .filter(p -> p.getId().equals(LOCKER_PROFILE)).findFirst();



        if (Files.exists(lockerPom)) {
            if (lockerProfile.isEmpty()) {
                logger.warn("'" + LOCKER_PROFILE + "' profile not found in the pom.xml, remove the Locker BOM if it is not used.");
                return;
            }

            installLocker(session.getLocalRepository(), lockerPom, logger);
        } else {
            if(lockerProfile.isPresent()) {
                throw new MavenExecutionException("'" + LOCKER_PROFILE + "' profile found in the pom.xml but no Locker BOM found in: " + lockerPom, session.getRequest()
                        .getPom());
            }
        }
    }


    private void prepareForLocking(MavenSession session) throws MavenExecutionException {
        session.getRequest().getInactiveProfiles().add(LOCKER_PROFILE);
    }

}