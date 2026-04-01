import static org.assertj.core.api.Assertions.assertThat

import java.nio.file.Files

import org.xmlunit.assertj3.XmlAssert

import io.mvnpm.maven.locker.XmlUnitTestSupport

buildLog = Files.readAllLines(basedir.toPath().resolve("build.log"))
assertThat(buildLog)
    .anyMatch { it.contains("Your pom.xml contains other profiles with 'activeByDefault=true'.") }

lockedPom = basedir.toPath().resolve("input/pom.xml")
expectedPom = basedir.toPath().resolve("expected/pom.xml")

lockerBom = basedir.toPath().resolve("input/locker/pom.xml")
expectedLockerBom = basedir.toPath().resolve("expected/locker/pom.xml")

assertThat(lockedPom).exists()
assertThat(lockerBom).exists()

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
