package io.mvnpm.maven.locker.model;

import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class Artifacts implements Iterable<Artifact> {

    public final List<Artifact> artifacts;

    private Artifacts(Collection<Artifact> artifacts) {
        ArrayList<Artifact> copy = new ArrayList<>(artifacts);
        sort(copy);
        this.artifacts = unmodifiableList(copy);
    }

    public static Artifacts fromMavenArtifacts(
            Collection<org.apache.maven.artifact.Artifact> artifacts) {
        return new Artifacts(artifacts.stream().map(a -> Artifact.from(a)).collect(toList()));
    }

    public static Artifacts fromArtifacts(Collection<Artifact> artifacts) {
        return new Artifacts(artifacts);
    }

    public Optional<Artifact> find(ArtifactIdentifier identifier) {
        for (Artifact artifact : artifacts) {
            if (identifier.equals(artifact.identifier)) {
                return Optional.of(artifact);
            }
        }
        return Optional.empty();
    }

    public Artifacts filter(List<String> filters) {
        return Artifacts.fromArtifacts(artifacts.stream().filter(a -> isArtifactMatched(filters, a))
                .collect(Collectors.toList()));
    }

    private static boolean isArtifactMatched(List<String> filters, Artifact a) {
        return filters.stream().anyMatch(f -> {
            if (f == null || f.isBlank()) {
                return false;
            }
            final String[] split = f.split(":");
            if (split.length > 2) {
                throw new IllegalArgumentException("Invalid artifact filter: " + f);
            }
            if (matchesWildcard(a.identifier.groupId, split[0])) {
                return true;
            }
            return split.length == 2 && matchesWildcard(a.identifier.artifactId, split[1]);
        });
    }

    static boolean matchesWildcard(String input, String wildcard) {
        String[] wildcardParts = wildcard.split("\\*");
        for (int i = 0; i < wildcardParts.length; i++) {
            wildcardParts[i] = Pattern.quote(wildcardParts[i]);
        }
        String regex = String.join(".*", wildcardParts);
        if (wildcard.startsWith("*")) {
            regex = ".*" + regex;
        }
        if (wildcard.endsWith("*")) {
            regex = regex + ".*";
        }
        regex = String.format("^%s$", regex);
        return Pattern.compile(regex).matcher(input).matches();
    }

    @Override
    public Iterator<Artifact> iterator() {
        return artifacts.iterator();
    }
}
