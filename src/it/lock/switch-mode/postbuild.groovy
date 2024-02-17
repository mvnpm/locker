import org.hamcrest.io.FileMatchers

import java.nio.file.Files

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.both
import static org.hamcrest.Matchers.containsInRelativeOrder
import static org.hamcrest.Matchers.endsWith
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.startsWith

buildLog = Files.readAllLines(basedir.toPath().resolve("build.log"))
assertThat(buildLog, containsInRelativeOrder("[INFO] Switching to locker BOM in 'locker' profile"))
assertThat(buildLog, hasItem(both(startsWith("[INFO] Creating ")).and(endsWith("/mvnpm-locker/target/its/lock/switch-mode/locker/pom.xml"))))

lockedPom = basedir.toPath().resolve("pom.xml")
expectedPom = basedir.toPath().resolve("expected-pom.xml")

assertThat(Files.readString(lockedPom), is(Files.readString(expectedPom)))

lockerBom = basedir.toPath().resolve("locker/pom.xml")
expectedLockerBom = basedir.toPath().resolve("expected-locker/pom.xml")

assertThat(lockedPom.toFile(), FileMatchers.anExistingFile())
assertThat(Files.readString(lockerBom), is(Files.readString(expectedLockerBom)))