[![Build](https://github.com/theborakompanioni/tor-spring-boot-starter/actions/workflows/build.yml/badge.svg)](https://github.com/theborakompanioni/tor-spring-boot-starter/actions/workflows/build.yml)
[![GitHub Release](https://img.shields.io/github/release/theborakompanioni/tor-spring-boot-starter.svg?maxAge=3600)](https://github.com/theborakompanioni/tor-spring-boot-starter/releases/latest)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.theborakompanioni/spring-tor-core.svg?maxAge=3600)](https://search.maven.org/#search|g%3A%22io.github.theborakompanioni%22)
[![License](https://img.shields.io/github/license/theborakompanioni/tor-spring-boot-starter.svg?maxAge=2592000)](https://github.com/theborakompanioni/tor-spring-boot-starter/blob/master/LICENSE)


<p align="center">
    <img src="https://github.com/theborakompanioni/tor-spring-boot-starter/blob/master/docs/assets/images/logo.png" alt="Logo" width="255" />
</p>


tor-spring-boot-starter
===

## Table of Contents

- [Install](#install)
- [Development](#development)
- [Contributing](#contributing)
- [Resources](#resources)
- [License](#license)


## Install

[Download](https://search.maven.org/#search|g%3A%22io.github.theborakompanioni%22) from Maven Central.

### Gradle
```groovy
implementation "io.github.theborakompanioni:spring-tor-starter:${torSpringBootStarterVersion}"
```

### Maven
```xml
<dependency>
    <groupId>io.github.theborakompanioni</groupId>
    <artifactId>spring-tor-starter</artifactId>
    <version>${torSpringBootStarter.version}</version>
</dependency>
```

The example above imports module `spring-tor-starter` - you can import any module by its name.

## Development

### Requirements
- java >=11

### Build
```shell script
./gradlew build -x test
```
 
### Test
```shell script
./gradlew test integrationTest
```

### Dependency Verification
Gradle is used for checksum and signature verification of dependencies.

```shell script
# write metadata for dependency verification
./gradlew --write-verification-metadata pgp,sha256 --export-keys
```

See [Gradle Userguide: Verifying dependencies](https://docs.gradle.org/current/userguide/dependency_verification.html)
for more information.

### Checkstyle
[Checkstyle](https://github.com/checkstyle/checkstyle) with adapted [google_checks](https://github.com/checkstyle/checkstyle/blob/master/src/main/resources/google_checks.xml)
is used for checking Java source code for adherence to a Code Standard.

```shell script
# check for code standard violations with checkstyle
./gradlew checkstyleMain
```

### SpotBugs
[SpotBugs](https://spotbugs.github.io/) is used for static code analysis.

```shell script
# invoke static code analysis with spotbugs
./gradlew spotbugsMain
```


## Contributing
All contributions and ideas are always welcome. For any question, bug or feature request, 
please create an [issue](https://github.com/theborakompanioni/tor-spring-boot-starter/issues). 
Before you start, please read the [contributing guidelines](contributing.md).


## Resources

- Spring Boot (GitHub): https://github.com/spring-projects/spring-boot
---
- Tor: https://www.torproject.org/
- netlayer (GitHub): https://github.com/bisq-network/netlayer

## License

The project is licensed under the Apache License. See [LICENSE](LICENSE) for details.

