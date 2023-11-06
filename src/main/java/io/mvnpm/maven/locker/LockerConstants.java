package io.mvnpm.maven.locker;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public interface LockerConstants {

    Predicate<String> LOCK_GOAL_PREDICATE = Pattern.compile(
            "^(io\\.mvnpm:)?locker(-maven-plugin)?(:[^:]+)?:lock$").asMatchPredicate();
    String LOCKER_DIR = ".locker";
    String LOCKER_POM_PATH = LOCKER_DIR + "/pom.xml";
    String LOCKER_PROFILE = "locker";
}
