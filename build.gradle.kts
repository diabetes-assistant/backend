plugins {
    application
    java
    id("io.freefair.lombok") version "6.0.0-m2"
    id("org.springframework.boot") version "2.5.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.github.spotbugs") version "4.7.1"
    id("com.diffplug.spotless") version "5.12.5"
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

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.auth0:java-jwt:3.16.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.mockito:mockito-core:3.+")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    spotbugsPlugins("com.h3xstream.findsecbugs:findsecbugs-plugin:1.10.1")
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
