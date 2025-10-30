plugins {
    // Core Gradle plugins
    `java-library`                                              // For creating reusable Java libraries
    application                                                 // Adds support for building and running Java applications

    // Publishing plugins
    signing                                                     // Signs artifacts for Maven Central
    `maven-publish`                                             // Enables publishing to Maven repositories
    id("com.vanniktech.maven.publish") version "0.34.0"         // Automates Maven publishing tasks

    // Tooling plugins
    `jacoco`                                                    // For code coverage reports
    id("com.gradleup.shadow") version "8.3.7"                   // Bundles dependencies into a single JAR
}

/////////////////////////
// Project metadata    //
/////////////////////////

object Meta {
    // Project coordinates
    const val groupId = "fr.inria.corese"
    const val artifactId = "corese-command"
    const val version = "4.6.2"

    // Project description
    const val desc = "A command-line tool for converting, querying, and validating RDF data with SPARQL and SHACL using the Corese engine."
    const val githubRepo = "corese-stack/corese-command"
  
    // License information
    const val license = "CeCILL-C License"
    const val licenseUrl = "https://opensource.org/licenses/CeCILL-C"
}

////////////////////////
// Project settings  //
///////////////////////

// Java compilation settings
java {
    withSourcesJar()                             // Include sources JAR in publications
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass.set("fr.inria.corese.command.App") // Define the main class for the application
}

/////////////////////////
// Dependency settings //
/////////////////////////

// Define repositories to resolve dependencies from
repositories {
    mavenLocal()    // First, check the local Maven repository
    mavenCentral()  // Then, check Maven Central
}

dependencies {
    val coreseVersion = "4.6.4"
    val picocliVersion = "4.7.7"
    val jacksonVersion = "2.17.1" // Version compatible with WireMock 3.13.1

    implementation("fr.inria.corese:corese-core:$coreseVersion") {
        // Exclude Log4j2 dependencies to avoid SLF4J multiple bindings warning
        // corese-core includes Log4j2, but corese-command uses Logback
        exclude(group = "org.apache.logging.log4j", module = "log4j-slf4j2-impl")
        exclude(group = "org.apache.logging.log4j", module = "log4j-core")
    }
    implementation("info.picocli:picocli:$picocliVersion") // Library for building a Command-Line Interface (CLI)

    implementation("org.apache.commons:commons-lang3:3.19.0") // Library for utility functions (e.g., StringUtils)
    implementation("com.github.jsonld-java:jsonld-java:0.13.6") // Library for JSON-LD support
    implementation("jakarta.ws.rs:jakarta.ws.rs-api:4.0.0") // Jakarta REST API specifications
    implementation("jakarta.activation:jakarta.activation-api:2.2.0-M1") // Jakarta Activation API
    implementation("fr.com.hp.hpl.jena.rdf.arp:arp:2.2.b") // Jena implementation for RDF parsing
    implementation("org.slf4j:slf4j-api:2.0.17") // Simple Logging Facade for Java (SLF4J)
    runtimeOnly("ch.qos.logback:logback-classic:1.5.20") // Logging framework for SLF4J

    testImplementation("org.wiremock:wiremock:3.13.1") // HTTP server mocking for API testing
    testImplementation("org.junit.jupiter:junit-jupiter:6.0.0") // JUnit 5 for testing
    testImplementation("org.mockito:mockito-core:5.15.2") // Mockito for mocking in tests
    testImplementation("org.mockito:mockito-junit-jupiter:5.15.2") // Mockito JUnit 5 integration
    testRuntimeOnly("org.junit.platform:junit-platform-launcher") // JUnit Platform launcher for running tests

    // Force Jackson version to fix compatibility issues with WireMock
    // WireMock 3.13.1 requires Jackson 2.17.x for the configure(DatatypeFeature, boolean) method
    // Without this, running tests from IDE may fail with NoSuchMethodError
    testImplementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    testImplementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
}

// Force Jackson version across all configurations to prevent version conflicts
// This ensures that transitive dependencies from corese-core or other libraries
// do not introduce older Jackson versions that are incompatible with WireMock
configurations.all {
    resolutionStrategy {
        force("com.fasterxml.jackson.core:jackson-core:2.17.1")
        force("com.fasterxml.jackson.core:jackson-annotations:2.17.1")
        force("com.fasterxml.jackson.core:jackson-databind:2.17.1")
    }
}

/////////////////////////
// Publishing settings //
/////////////////////////

mavenPublishing {
    coordinates(Meta.groupId, Meta.artifactId, Meta.version)

    pom {
        name.set(Meta.artifactId)
        description.set(Meta.desc)
        url.set("https://github.com/${Meta.githubRepo}")
        licenses {
            license {
                name.set(Meta.license)
                url.set(Meta.licenseUrl)
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("remiceres")
                name.set("Rémi Cérès")
                email.set("remi.ceres@inria.fr")
                url.set("http://www-sop.inria.fr/members/Remi.Ceres")
                organization.set("Inria")
                organizationUrl.set("http://www.inria.fr/")
            }
        }
        scm {
            url.set("https://github.com/${Meta.githubRepo}/")
            connection.set("scm:git:git://github.com/${Meta.githubRepo}.git")
            developerConnection.set("scm:git:ssh://git@github.com/${Meta.githubRepo}.git")
        }
        issueManagement {
            url.set("https://github.com/${Meta.githubRepo}/issues")
        }
    }

    publishToMavenCentral()

    // Only sign publications when GPG keys are available (CI environment)
    if (project.hasProperty("signingInMemoryKey") || project.hasProperty("signing.keyId")) {
        signAllPublications()
    }
}

/////////////////////////
// Task configuration  //
/////////////////////////

// Set UTF-8 encoding for Java compilation tasks
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:none")
}

// Configure Javadoc tasks with UTF-8 encoding and disable failure on error.
// This ensures that Javadoc generation won't fail due to minor issues.
tasks.withType<Javadoc>().configureEach {
    options.encoding = "UTF-8"
    isFailOnError = false
    // Configure Javadoc tasks to disable doclint warnings.
    (options as CoreJavadocOptions).addBooleanOption("Xdoclint:none", true)
}


// Configure the shadow JAR task to include dependencies in the output JAR.
// This creates a single JAR file with all dependencies bundled.
// The JAR file is named with the classifier "standalone" to indicate it contains all dependencies.
tasks {
    shadowJar {
        this.archiveClassifier = "standalone"
    }
}

// Configure the build task to depend on the shadow JAR task.
// This ensures that the shadow JAR is built when the project is built.
tasks.build {
    dependsOn(tasks.shadowJar)
}

// Configure Jacoco test report task to depend on the test task,
// so reports are generated after tests run successfully.
tasks.jacocoTestReport {
    dependsOn(tasks.test)

    // Enable XML reporting for Jacoco to allow further processing or CI integration.
    reports {
        xml.required.set(true)
    }
}

// Set the test task to be followed by Jacoco report generation.
// This ensures that test coverage reports are always generated after tests.
tasks.test {
    useJUnitPlatform() // Use JUnit 5 for running tests
    finalizedBy(tasks.jacocoTestReport)
}

// Ensure that all local Maven publication tasks depend on signing tasks.
// This guarantees that artifacts are signed before they are published locally.
tasks.withType<PublishToMavenLocal>().configureEach {
    dependsOn(tasks.withType<Sign>())
}

// Ensure that all remote Maven publication tasks depend on signing tasks.
// This guarantees that artifacts are signed before they are published to Maven repositories.
tasks.withType<PublishToMavenRepository>().configureEach {
    dependsOn(tasks.withType<Sign>())
}

// Fix dependency issue with Maven publication metadata generation
// The generateMetadataFileForMavenPublication task needs to depend on plainJavadocJar
// Use afterEvaluate to ensure tasks are created by the vanniktech plugin first
afterEvaluate {
    tasks.findByName("generateMetadataFileForMavenPublication")?.dependsOn("plainJavadocJar")
}
