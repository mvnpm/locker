import java.nio.file.Files

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.contains
import static org.hamcrest.Matchers.containsInRelativeOrder
import static org.hamcrest.Matchers.is

buildLog = Files.readAllLines(basedir.toPath().resolve("build.log"))
assertThat(buildLog, containsInRelativeOrder("[INFO] Updating 'locker' profile with locked dependencies"))

lockedPom = basedir.toPath().resolve("pom.xml")
expectedPom = basedir.toPath().resolve("expected-pom.xml")

assertThat(Files.readString(lockedPom), is(Files.readString(expectedPom)))