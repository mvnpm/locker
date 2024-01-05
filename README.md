# mvnpm locker Maven Plugin

[![Build Status](https://img.shields.io/github/actions/workflow/status/mvnpm/locker/build.yml?label=Build&branch=main)](https://github.com/mvnpm/locker/actions/workflows/build.yml)
[![usefulness 100%](https://img.shields.io/badge/usefulness-100%25-success.svg?label=Usefulness)](https://www.google.com/search?q=pasta+machine)
[![Maven Central](https://img.shields.io/maven-central/v/io.mvnpm/locker-maven-plugin.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.mvnpm/locker-maven-plugin)
[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/apache/maven.svg?label=License)](https://www.apache.org/licenses/LICENSE-2.0)

The mvnpm locker Maven Plugin will create a version locker BOM for your `org.mvnpm` and `org.webjars` dependencies.
It is essential as NPM dependencies are over using ranges. After the locking, the quantity of files to download is considerably reduced (better for reproducibility, contributors and CI).

## Lock your mvnpm and webjars versions (or update)

This will create or udpate a locker BOM and add it to your pom.xml as a `locker` profile (if not yet there).

```shell
mvn io.mvnpm:locker-maven-plugin:0.0.5:lock
```

NOTE: When updating, if the `locker` profile is already in your pom.xml and you don't have the extension installed, you need to add this `-P\!locker` when using the `lock` goal.

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
        <version>0.0.5</version>
    </extension>
</extensions>
```

Features:
- Install the Locker BOM (`locker/pom.xml`) in the local Maven repository if needed before loading the Maven project (on any goal but `lock`)
- Ignore the `locker` profile when using the `lock` goal.
