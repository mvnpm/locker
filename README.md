# mvnpm locker Maven Plugin

[![Build Status](https://img.shields.io/github/actions/workflow/status/mvnpm/locker/build.yml?label=Build&branch=main)](https://github.com/mvnpm/locker/actions/workflows/build.yml)
[![usefulness 100%](https://img.shields.io/badge/usefulness-100%25-success.svg?label=Usefulness)](https://www.google.com/search?q=pasta+machine)
[![Maven Central](https://img.shields.io/maven-central/v/io.mvnpm/locker-maven-plugin.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.mvnpm/locker-maven-plugin)
[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/apache/maven.svg?label=License)](https://www.apache.org/licenses/LICENSE-2.0)

The mvnpm locker Maven Plugin will create a version locker BOM for your `org.mvnpm` and `org.webjars` dependencies.
Allowing you to mimick the `npm-shrinkwrap.json` and `yarn.lock` files in a Maven world.

It is essential as NPM dependencies are typically deployed using version ranges, without locking your builds will use different versions of dependencies between builds if any of your transitive NPM based dependencies are updated. 

In additon when using the locker, the number of files Maven need to download is considerably reduced as it no longer need to check all possible version ranges (better for reproducibility, contributors and CI).

## Lock your mvnpm and webjars versions (or update)

This will create or udpate a locker BOM and add it to your pom.xml as a `locker` profile (if not yet there).

```shell
mvn io.mvnpm:locker-maven-plugin:0.0.6:lock
```

**NOTE:**

When updating, if you don't have the locker extension installed, you need to add `-Dunlocked` when using the `lock` goal:
```shell
mvn io.mvnpm:locker-maven-plugin:0.0.6:lock -Dunlocked
```

## Common issues

**Maven resolver can't determine which version to use between two versions?**

In that case, choose which version to use in you dependency management.

**Dependabot update fails to build because of missing transitive dependencies**

- a. Checkout the PR locally and use `mvnpm-repo` profile to build. It will make sure all missing transitive dependencies are synchronized on Maven Central.
- b. Configure your CI to use the `mvnpm-repo` on dependabot updates PRs.


## Install the locker BOM in the Maven local repository

It is required before building your project (else it will fail).

On a standalone project:
- Manually `cd .locker && mvn clean install`. Add it to your install doc and add as a new step in your CI.
- [Using the locker extension](#add-the-locker-extension-optional) to automate this.

On multi-module project, add the locker bom as a module in the parent pom.xml:
```xml
      <modules>
        ...
        <module>my-module/.locker</module>
        <module>my-module</module>
        ...
      </modules>
```

## Add the locker extension (optional)

_This extension is optional, it is very helpful for standalone projects to allow building your bom if needed before running the project (for example when a new contributor clone the project and runs it or in CI)._

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
- Install the Locker BOM (`locker/pom.xml`) in the local Maven repository if needed before loading the Maven project (on any goal but `lock`)
- Ignore the `locker` profile when using the `lock` goal.
