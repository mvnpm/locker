import static org.assertj.core.api.Assertions.assertThat

import java.nio.file.Files

import org.xmlunit.assertj3.XmlAssert

import io.mvnpm.maven.locker.XmlUnitTestSupport

buildLog = Files.readAllLines(basedir.toPath().resolve("build.log"))
assertThat(buildLog)
    .containsSubsequence(
        "[INFO] Configured with locker BOM Mode",
        "[INFO] No changes to the project pom.xml"
    )
expectedLockerPomPath = basedir.toPath().resolve("input/locker/pom.xml").toAbsolutePath().toString()
assertThat(buildLog)
    .anyMatch { it.startsWith("[INFO] Creating ") && it.endsWith(expectedLockerPomPath) }

lockedPom = basedir.toPath().resolve("input/pom.xml")
expectedPom = basedir.toPath().resolve("expected/pom.xml")

lockerBom = basedir.toPath().resolve("input/locker/pom.xml")
expectedLockerBom = basedir.toPath().resolve("expected/locker/pom.xml")

assertThat(lockedPom).exists()

XmlAssert.assertThat(Files.readString(lockedPom))
    .and(Files.readString(expectedPom))
    .withNodeFilter(XmlUnitTestSupport.ignoreMvnpmDependencyVersions())
    .ignoreWhitespace()
    .areIdentical()

XmlAssert.assertThat(Files.readString(lockerBom))
    .and(Files.readString(expectedLockerBom))
    .withNodeFilter(XmlUnitTestSupport.ignoreMvnpmDependencyVersions())
    .ignoreWhitespace()
    .areIdentical()

true
