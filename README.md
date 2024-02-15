# mvnpm locker Maven Plugin

[![Build Status](https://img.shields.io/github/actions/workflow/status/mvnpm/locker/build.yml?label=Build&branch=main)](https://github.com/mvnpm/locker/actions/workflows/build.yml)
[![usefulness 100%](https://img.shields.io/badge/usefulness-100%25-success.svg?label=Usefulness)](https://www.google.com/search?q=pasta+machine)
[![Maven Central](https://img.shields.io/maven-central/v/io.mvnpm/locker-maven-plugin.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.mvnpm/locker-maven-plugin)
[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/apache/maven.svg?label=License)](https://www.apache.org/licenses/LICENSE-2.0)

The mvnpm locker Maven Plugin will create a version locker profile for your `org.mvnpm` and `org.webjars` dependencies.
Allowing you to mimick the `package-lock.json` and `yarn.lock` files in a Maven world.

_It is essential as NPM dependencies are typically deployed using version ranges, without locking your builds will use different versions of dependencies between builds if any of your transitive NPM based dependencies are updated._

_In additon when using the locker, the number of files Maven need to download is considerably reduced as it no longer need to check all possible version ranges (better for reproducibility, contributors and CI)._

## Installation

### In-Profile Mode  (for smaller amount of deps)

This command will modify your pom.xml with Locker dependencies directly in a new `locker` profile:
```shell
mvn io.mvnpm:locker-maven-plugin:LATEST:lock -Dlocker.in-profile
```

### Locker BOM Mode

This command will:
- create a distinct Locker BOM file (`./locker/pom.xml`)
- add a `locker` profile in your project pom.xml to use the Locker BOM

```shell
mvn io.mvnpm:locker-maven-plugin:LATEST:lock
```

**Now you need to install the locker BOM in the Maven local repository (It is required before building your project):**

=> On a standalone project:
- Manually `mvn -f locker clean install`. Add it to your install doc and add as a new step in your CI.
- [Using the locker extension](#add-the-locker-extension-for-locker-bom-mode-optional) to automate this.

=> On multi-module project, add the locker bom as a module in the parent pom.xml:
```xml
      <modules>
        ...
        <module>my-module/locker</module>
        <module>my-module</module>
        ...
      </modules>
```

## Update your locked dependencies

To update, you need to add `-Dunlocked` alongside the `lock` goal (to disable the locker profile and find new versions):
```shell
mvn io.mvnpm:locker-maven-plugin:LATEST:lock -Dunlocked
```

NOTE: _You don't need to specify the mode (`-Din-profile` option) as it is auto-detected._

## Switch to Locker BOM Mode (from in-profile locker dependencies)

If the amount of dependencies in your project has grown, you may want to switch to the Locker BOM Mode (to reduce the amount of dependencies in your project pom.xml).
```shell
mvn io.mvnpm:locker-maven-plugin:LATEST:lock -Dunlocked -Dlocker.in-profile=false
```

For the opposite, you can just remove the Locker BOM from your project and the locker profile and use the `in-profile` option to add the locker dependencies to your project pom.xml.

## Add the locker extension for Locker BOM mode (optional)

_This extension is optional, it is important for standalone projects to make sure your BOM is installed before running the project (for example when a new contributor clone the project and runs it or in CI)._

`.mvn/extensions.xml`
```xml
<extensions xmlns="http://maven.apache.org/EXTENSIONS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://maven.apache.org/EXTENSIONS/1.0.0 http://maven.apache.org/xsd/core-extensions-1.0.0.xsd">
    <extension>
        <groupId>io.mvnpm</groupId>
        <artifactId>locker-maven-plugin</artifactId>
        <version>0.0.6</version>
    </extension>
</extensions>
```

Features:
- Install the Locker BOM (`locker/pom.xml`) in the local Maven repository if out-of-date before loading the Maven project (on any goal but `lock`)
- Ignore the `locker` profile when using the `lock` goal.


## Common issues

**Maven resolver can't determine which version to use between two versions?**

In that case, choose which version to use by adding this `dependency` in the pom.xml `dependencyManagement > dependencies` section.

**Dependabot update fails to build because of missing transitive dependencies**

We are working on making this process automatic (See https://github.com/mvnpm/mvnpm/issues/4614), but for now you have two options:
- a. Checkout the PR locally and use `mvnpm-repo` profile to build. It will make sure all missing transitive dependencies are synchronized on Maven Central.
- b. Configure your CI to use the `mvnpm-repo` on dependabot updates PRs.

