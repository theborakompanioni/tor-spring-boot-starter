[![Build](https://github.com/theborakompanioni/tor-spring-boot-starter/actions/workflows/build.yml/badge.svg)](https://github.com/theborakompanioni/tor-spring-boot-starter/actions/workflows/build.yml)
[![GitHub Release](https://img.shields.io/github/release/theborakompanioni/tor-spring-boot-starter.svg?maxAge=3600)](https://github.com/theborakompanioni/tor-spring-boot-starter/releases/latest)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.theborakompanioni/spring-tor-core.svg?maxAge=3600)](https://search.maven.org/#search|g%3A%22io.github.theborakompanioni%22)
[![License](https://img.shields.io/github/license/theborakompanioni/tor-spring-boot-starter.svg?maxAge=2592000)](https://github.com/theborakompanioni/tor-spring-boot-starter/blob/master/LICENSE)


<p align="center">
    <img src="https://github.com/theborakompanioni/tor-spring-boot-starter/blob/master/docs/assets/images/logo.png" alt="Logo" width="255" />
</p>


tor-spring-boot-starter
===

A module containing a spring boot starter for an embedded [Tor daemon](https://www.torproject.org/).
The starter will automatically expose your application as hidden service!
Easily create hidden service sockets programmatically within your Spring Boot application.

A common configuration can look like this:
```yaml
org.tbk.tor:
  enabled: true  # whether auto-config should run - default is `true`
  auto-publish-enabled: true # auto publish the web port as hidden service - default is `true`
  working-directory: 'my-tor-directory' # the working directory for tor - default is `tor-working-dir`
  startup-timeout: 30s # max startup duration for tor to successfully start - default is `60s`
```

## Table of Contents

- [Install](#install)
- [Example](#example)  
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

## Example

Start the example application with
```shell
./gradlew -p spring-tor/spring-tor-example-application bootRun
```

Example output (2021-01-21):
```
2021-01-21 01:23:30.035  INFO [...] : Starting Tor
2021-01-21 01:23:33.490  INFO [...] : Tomcat started on port(s): 8080 (http) with context path ''
2021-01-21 01:23:33.511  INFO [...] : Started TorExampleApplication in 8.417 seconds (JVM running for 8.972)
2021-01-21 01:23:33.605  INFO [...] : =================================================
2021-01-21 01:23:33.606  INFO [...] : url: http://<your_onion_url>.onion:80
2021-01-21 01:23:33.607  INFO [...] : virtual host: <your_onion_url>.onion
2021-01-21 01:23:33.607  INFO [...] : virtual port: 80
2021-01-21 01:23:33.607  INFO [...] : host: 127.0.0.1
2021-01-21 01:23:33.607  INFO [...] : port: 8080
2021-01-21 01:23:33.607  INFO [...] : directory: /home/tbk/workspace/tor-spring-boot-starter/spring-tor/spring-tor-example-application/tor-working-dir/spring_boot_app
2021-01-21 01:23:33.608  INFO [...] : -------------------------------------------------
2021-01-21 01:23:33.608  INFO [...] : run: torsocks -p 46735 curl http://<your_onion_url>.onion:80/index.html -v
2021-01-21 01:23:33.608  INFO [...] : =================================================
```

## Development

### Requirements
- java >=17

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

