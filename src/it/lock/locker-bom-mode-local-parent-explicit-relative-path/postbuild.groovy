import static org.assertj.core.api.Assertions.assertThat

import java.nio.file.Files

import org.xmlunit.assertj3.XmlAssert

import io.mvnpm.maven.locker.XmlUnitTestSupport

buildLog = Files.readAllLines(basedir.toPath().resolve("build.log"))
assertThat(buildLog)
    .contains("[INFO] Adding 'locker' profile with the Locker BOM to the pom.xml...")
expectedLockerPomPath = basedir.toPath().resolve("input/child/locker/pom.xml").toAbsolutePath().toString()
assertThat(buildLog)
    .anyMatch { it.startsWith("[INFO] Creating ") && it.endsWith(expectedLockerPomPath) }

lockedPom = basedir.toPath().resolve("input/child/pom.xml")
expectedPom = basedir.toPath().resolve("expected/child/pom.xml")

lockerBom = basedir.toPath().resolve("input/child/locker/pom.xml")
expectedLockerBom = basedir.toPath().resolve("expected/child/locker/pom.xml")

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
