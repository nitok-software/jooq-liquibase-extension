plugins {
    // Apply the java-library plugin for API and implementation separation.
    `java-library`
    `maven-publish`
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

group = "eu.nitok.gradle"
version = "0.1.1"

dependencies {
    implementation(platform("org.testcontainers:testcontainers-bom:2.0.3"))
    api("org.testcontainers:testcontainers")
    api("org.testcontainers:mysql")
    api("org.testcontainers:postgresql")

    implementation("org.jooq:jooq-meta-extensions-liquibase:3.16.3")
    implementation("org.jooq:jooq-codegen:3.16.3")

    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("org.postgresql:postgresql:42.7.3")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}