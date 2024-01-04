package io.mvnpm.maven.locker.pom;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;

import com.google.common.io.Resources;

import io.mvnpm.maven.locker.model.Artifacts;
import io.mvnpm.maven.locker.model.GAV;
import io.mvnpm.maven.locker.model.ParentPom;
import io.quarkus.qute.Qute;

public final class DefaultLockerPom implements LockerPom {

    private final LockerPomFileAccessor lockerPom;
    private final GAV gav;
    private final Log log;

    private DefaultLockerPom(
            LockerPomFileAccessor lockerPom, GAV gav, Log log) {
        this.lockerPom = lockerPom;
        this.gav = gav;
        this.log = log;
    }

    public static LockerPom from(
            LockerPomFileAccessor dependenciesLockFile, GAV gav, Log log) {
        return new DefaultLockerPom(
                requireNonNull(dependenciesLockFile), requireNonNull(gav), requireNonNull(log));
    }

    @Override
    public void write(ParentPom parent, Artifacts artifacts) {
        try {
            URL url = Resources.getResource(this.getClass(), "pom.xml");
            String template = Resources.toString(url, StandardCharsets.UTF_8);
            final String fmted = Qute.fmt(template, makeDataModel(gav, parent, artifacts));
            try (Writer writer = lockerPom.writer()) {
                writer.write(fmted);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Map<String, Object> makeDataModel(GAV gav, ParentPom parent, Artifacts artifacts) {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("pom", gav);
        dataModel.put("dependencies", artifacts);
        dataModel.put("parent", parent);
        return dataModel;
    }

    @Override
    public Artifacts read() {
        return Artifacts.fromArtifacts(LockerPomReader.read(lockerPom.file));
    }
}
