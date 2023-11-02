package io.mvnpm.maven.locker;

import com.google.common.hash.Hashing;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.plexus.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public final class InstallLocker {

    public static void installLocker(Path lockerPom, Path lockerHashPath, Logger log) throws MavenExecutionException {
        try {
            final byte[] lockerHashRead = Hashing.sha512().hashBytes(Files.readAllBytes(lockerPom)).asBytes();
            if (Files.exists(lockerHashPath)) {
                final byte[] lockerHashDisk = Files.readAllBytes(lockerHashPath);
                if (Arrays.equals(lockerHashDisk, lockerHashRead)) {
                    log.info("Locker BOM is installed and up-to-date.");
                    return;
                }
            }
            log.info("Locker BOM is out-of-date, installing...");
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
            Files.deleteIfExists(lockerHashPath);
            Files.write(lockerHashPath, lockerHashRead);
        } catch (MavenInvocationException | IOException e) {
            throw new MavenExecutionException("Error while installing Locker BOM.", e);
        }
    }

}
