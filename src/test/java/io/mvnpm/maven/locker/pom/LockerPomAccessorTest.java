package io.mvnpm.maven.locker.pom;

import static java.lang.String.format;
import static java.util.Locale.ROOT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class LockerPomAccessorTest {

    private static Random random = new Random();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void parentFoldersShouldBeCreated() throws IOException {
        Path basedir = folder.newFolder().toPath();
        String folderName1 = randomEnoughString();
        String folderName2 = randomEnoughString();
        String filename = randomEnoughString();
        Path lockFile = basedir.resolve(folderName1).resolve(folderName2).resolve(filename);
        assertFalse(Files.isRegularFile(lockFile));
        LockerPomFileAccessor.fromBasedir(
                basedir, format(ROOT, "%s/%s/%s", folderName1, folderName2, filename))
                .writer()
                .close();
        assertTrue(Files.isRegularFile(lockFile));
    }

    private static String randomEnoughString() {
        long l1 = random.nextInt(Integer.MAX_VALUE);
        long l2 = random.nextInt(Integer.MAX_VALUE);
        return (Long.toString(l1, 36) + Long.toString(l2, 36)).toUpperCase(ROOT);
    }
}
