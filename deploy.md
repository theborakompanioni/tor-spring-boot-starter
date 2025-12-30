
# Deployment

### Maven Central

Publish all modules to Maven Central (notice params `signing.password`):
```sh
./gradlew --no-parallel \
    clean assemble -PjavadocEnabled \
    sign -Psigning.password=secret \
    publishNebulaPublicationToStagingDeployRepository \
    jreleaserDeploy -Prelease.version=1.0.0-SNAPSHOT
```

Publish an individual module to Maven Central:
```sh
./gradlew -p spring-tor/spring-tor-core \
    --no-parallel \
    clean assemble -PjavadocEnabled \
    sign -Psigning.password=secret \
    publishNebulaPublicationToStagingDeployRepository \
    jreleaserDeploy -Prelease.version=1.0.0-SNAPSHOT
```

In your local `~/.jreleaser/config.properties` you need
```properties
JRELEASER_MAVENCENTRAL_USERNAME=<YOUR_USERNAME>
JRELEASER_MAVENCENTRAL_TOKEN=<YOUR_TOKEN>

JRELEASER_GITHUB_TOKEN=DO_NOT_SET_REAL_TOKEN_FOR_NOW___THIS_IS_JUST_TO_MAKE_THE_BUILD_HAPPY
```

## Resources
- Maven Central Publish Guide: https://central.sonatype.org/publish/publish-portal-api/
- https://discuss.gradle.org/t/how-to-publish-artifacts-signatures-asc-files-using-maven-publish-plugin/7422/24
- JReleaser (GitHub): https://github.com/jreleaser/jreleaser
- JReleaser (Docs): https://jreleaser.org/guide/latest/examples/maven/maven-central.html
