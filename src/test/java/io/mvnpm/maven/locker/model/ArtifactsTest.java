package io.mvnpm.maven.locker.model;

import junit.framework.TestCase;
import org.junit.Test;

import static io.mvnpm.maven.locker.model.Artifacts.matchesWildcard;

public class ArtifactsTest extends TestCase {
    @Test
    public void testWildcardMatching() {
        assertTrue(matchesWildcard("org.mvnpm", "org.mvnpm*"));
        assertTrue(matchesWildcard("org.mvnpm.at.foo", "org.mvnpm*"));
        assertTrue(matchesWildcard("foo123.bar", "foo*.bar"));
        assertTrue(matchesWildcard("foobar123.bar", "foo*.bar"));
        assertTrue(matchesWildcard("foo.dddbardd", "foo.*bar*"));
        assertFalse(matchesWildcard("barfoo.test", "foo*.bar"));
        assertFalse(matchesWildcard("test.foo.bar", "foo*.bar"));
    }
}