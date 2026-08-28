# This justfile requires https://github.com/casey/just

# run example application
[group("example")]
run-example port='7000' profiles='development':
    @SPRING_PROFILES_ACTIVE={{ profiles }} \
    ./gradlew -p spring-tor/spring-tor-example-application \
    bootRun --args="--server.port={{ port }}"
