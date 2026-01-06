plugins {
    `java-library`
    `maven-publish`
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

group = "eu.nitok.gradle"
version = "0.1.2"

dependencies {
    implementation(platform(libs.testcontainers.bom))
    api(libs.testcontainers.core)
    api(libs.testcontainers.mysql)
    api(libs.testcontainers.postgres)

    implementation(libs.jooq.metaExtensions.liquibase)
    implementation(libs.jooq.codegen)

    implementation(libs.driver.mysql)
    implementation(libs.driver.postgres)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}