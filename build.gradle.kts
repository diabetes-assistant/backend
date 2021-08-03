plugins {
    application
    java
    id("io.freefair.lombok") version "6.1.0-m2"
    id("org.springframework.boot") version "2.5.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.github.spotbugs") version "4.7.2"
    id("com.diffplug.spotless") version "5.14.2"
    id ("com.github.ben-manes.versions") version "0.39.0"
}

repositories {
    mavenCentral()
}

sourceSets {
    create("intTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val intTestImplementation by configurations.getting {
    extendsFrom(configurations.implementation.get())
    extendsFrom(configurations.testImplementation.get())
    extendsFrom(configurations.testRuntimeOnly.get())
}

configurations["intTestRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())

val junitVersion = "5.8.0-M1"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
//    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.data:spring-data-r2dbc:1.3.3")
    implementation("org.springframework.security:spring-security-crypto:5.5.1")
    implementation("org.bouncycastle:bcpkix-jdk15to18:1.69")
    implementation("com.auth0:java-jwt:3.18.1")
    implementation("org.flywaydb:flyway-core:7.12.0")
    implementation("io.r2dbc:r2dbc-postgresql:0.8.8.RELEASE")
    implementation("org.postgresql:postgresql:42.2.23")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
//    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.projectreactor:reactor-test:3.4.7")
    testImplementation("org.mockito:mockito-core:3.+")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    spotbugs("com.github.spotbugs:spotbugs:4.3.0")
    spotbugs("com.github.spotbugs:spotbugs-annotations:4.3.0")
    spotbugsPlugins("com.h3xstream.findsecbugs:findsecbugs-plugin:1.11.0")
}

group = "com.github.diabetesassistant"

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

application {
    mainClass.set("com.github.diabetesassistant.App")
}

spotless {
    java {
        googleJavaFormat()
    }
}

val integrationTest = task<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    useJUnitPlatform()

    testClassesDirs = sourceSets["intTest"].output.classesDirs
    classpath = sourceSets["intTest"].runtimeClasspath
    shouldRunAfter("test")
}

tasks.check { dependsOn(integrationTest) }

tasks.test {
    useJUnitPlatform()
}

tasks {
    named<com.github.spotbugs.snom.SpotBugsTask>("spotbugsIntTest") {
        ignoreFailures = true
    }
}
