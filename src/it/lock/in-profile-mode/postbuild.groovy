import static org.assertj.core.api.Assertions.assertThat

import java.nio.file.Files

import org.xmlunit.assertj3.XmlAssert

import io.mvnpm.maven.locker.XmlUnitTestSupport

buildLog = Files.readAllLines(basedir.toPath().resolve("build.log"))
assertThat(buildLog)
    .contains("[INFO] Adding 'locker' profile with 6 locked dependencies to the pom.xml...")

lockedPom = basedir.toPath().resolve("pom.xml")
expectedPom = basedir.toPath().resolve("expected-pom.xml")

XmlAssert.assertThat(Files.readString(lockedPom))
    .and(Files.readString(expectedPom))
    .withNodeFilter(XmlUnitTestSupport.ignoreMvnpmDependencyVersions())
    .ignoreWhitespace()
    .areIdentical()

true
