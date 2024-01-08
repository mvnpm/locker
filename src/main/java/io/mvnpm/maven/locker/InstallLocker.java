package io.mvnpm.maven.locker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Model;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.plexus.logging.Logger;

import com.google.common.hash.Hashing;

import io.fabric8.maven.Maven;

public final class InstallLocker {

    public static void installLocker(ArtifactRepository localRepository, Path lockerPom, Logger log)
            throws MavenExecutionException {
        try {
            final Path lockerPomInRepo = pathOfLockPomInLocalRepo(localRepository, lockerPom);
            if (Files.exists(lockerPomInRepo)) {
                final byte[] lockerHash = Hashing.sha512().hashBytes(Files.readAllBytes(lockerPom)).asBytes();
                final byte[] lockerHashRepo = Hashing.sha512().hashBytes(Files.readAllBytes(lockerPomInRepo)).asBytes();
                if (Arrays.equals(lockerHash, lockerHashRepo)) {
                    log.info("Locker BOM is installed in Maven local repository and up-to-date.");
                    return;
                }
                log.info("Locker BOM is out-of-date in Maven local repository, installing...");
            } else {
                log.info("Locker BOM is not in Maven local repository, installing...");
            }
            InvocationRequest request = new DefaultInvocationRequest();
            request.setPomFile(lockerPom.toFile());
            request.setBatchMode(true);
            request.setGoals(Collections.singletonList("clean install"));
            request.setOutputHandler(log::debug);
            request.setErrorHandler(log::error);
            Invoker invoker = new DefaultInvoker();
            final InvocationResult result = invoker.execute(request);
            if (result.getExitCode() != 0) {
                throw new MavenExecutionException("Error while installing Locker BOM.", lockerPom.toFile());
            }
            log.info("Locker BOM has been installed.");
        } catch (MavenInvocationException | IOException e) {
            throw new MavenExecutionException("Error while installing Locker BOM.", e);
        }
    }

    private static Path pathOfLockPomInLocalRepo(ArtifactRepository localRepository, Path lockerPom) {
        final Model lockerModel = Maven.readModel(lockerPom);
        String groupId = lockerModel.getGroupId();
        String version = lockerModel.getVersion();
        if (groupId == null && lockerModel.getParent() != null) {
            groupId = lockerModel.getParent().getGroupId();
        }
        if (version == null && lockerModel.getParent() != null) {
            version = lockerModel.getParent().getVersion();
        }
        final DefaultArtifact lockerArtifact = new DefaultArtifact(groupId, lockerModel.getArtifactId(),
                version, "import", "pom", null, new DefaultArtifactHandler());
        return Path.of(localRepository.getBasedir(), localRepository.pathOf(lockerArtifact) + ".pom");
    }

}
