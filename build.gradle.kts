/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version "8.3.1"
    signing
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<Javadoc> { isFailOnError = false }

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    api(libs.info.picocli.picocli)
    api(libs.fr.inria.corese.corese.core)
    testImplementation(libs.org.junit.jupiter.junit.jupiter.api)
    testImplementation(libs.org.junit.jupiter.junit.jupiter.engine)
    testImplementation(libs.org.wiremock.wiremock)
    testImplementation(libs.org.assertj.assertj.core)
}

group = "fr.inria.corese"
version = "4.5.1"
description = "corese-command"
java.sourceCompatibility = JavaVersion.VERSION_1_8

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}

tasks {
    shadowJar {
        this.archiveClassifier = ""
    }
}