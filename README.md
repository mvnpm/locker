# mvnpm locker Maven Plugin

[![Build Status](https://img.shields.io/github/actions/workflow/status/mvnpm/locker/build.yaml?label=Build&branch=master)](https://github.com/vandmo/dependency-lock-maven-plugin/actions/workflows/test-and-release.yaml)
[![usefulness 100%](https://img.shields.io/badge/usefulness-100%25-success.svg?label=Usefulness)](https://www.google.com/search?q=pasta+machine)
[![Maven Central](https://img.shields.io/maven-central/v/io.mvnpm/locker-maven-plugin.svg?label=Maven%20Central)](https://search.maven.org/artifact/se.vandmo/dependency-lock-maven-plugin)
[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/apache/maven.svg?label=License)](https://www.apache.org/licenses/LICENSE-2.0)

The mvnpm locker Maven Plugin will create a version locker BOM for your `org.mvnpm` and `org.webjars` dependencies.

## Lock your mvnpm and webjars versions 

This will create a locker BOM and add it to your pom.xml as a `locker` profile (if not yet there).

For standalone projects (non multi-module):
```shell
mvn io.mvnpm:locker-maven-plugin:[VERSION]:lock
```

For multi-modules projects:
```shell
mvn io.mvnpm:locker-maven-plugin:[VERSION]:lock -Dlocker.standalone=false
```

NOTE: if the the `locker` profile is already in your pom.xml and you don't have the extension installed, you need to add this `-P\!locker` when using the `lock` goal.

## Add the locker extension (optional)

_This extension is optional, it is very helpful for standalone projects to allow building your bom if needed before running the project (for example when a new contributor clone the project and run it)._

`.mvn/extensions.xml`
```xml
<extensions xmlns="http://maven.apache.org/EXTENSIONS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://maven.apache.org/EXTENSIONS/1.0.0 http://maven.apache.org/xsd/core-extensions-1.0.0.xsd">
    <extension>
        <groupId>io.mvnpm</groupId>
        <artifactId>locker-maven-plugin</artifactId>
        <version>[VERSION]</version>
    </extension>
</extensions>
```

Features:
- Install the Locker BOM if needed (`locker/pom.xml` before continuing on any goal (but `lock`)
- Ignore the `locker` profile when using the `lock` goal.
