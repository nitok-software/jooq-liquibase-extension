import nu.studer.gradle.jooq.JooqGenerate
import org.jooq.meta.jaxb.Property

plugins {
    id("java-library")
    id("nu.studer.jooq").version("8.0")
}

repositories{
    mavenCentral()
}

dependencies {
    jooqGenerator(project(":jooq-liquibase"))//this repository
    jooqGenerator("org.liquibase:liquibase-core:4.23.0")//(optional!) only needed when pinning a version

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}
jooq {
    version = "3.16.3"
    configurations {
        //this is kotlin gradle syntax. When using groovy (.gradle instead of .gradle.kts) see here https://github.com/etiennestuder/gradle-jooq-plugin/blob/main/example/configure_toolchain_gradle_dsl/build.gradle
        create("main") {
            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "org.testcontainers.jdbc.ContainerDatabaseDriver"
                    url = "jdbc:tc:mysql:8.0.33:///;databaseName=YOUR_SCHEMA_NAME?TC_TMPFS=/testtmpfs:rw&user=root"
                }
                generator.apply {
                    database.apply {
                        name = "eu.nitok.jooq.extension.MySqlLiquibaseDatabase"
                        properties.apply {
                            add(Property().withKey("scripts").withValue("changelog.xml"))
                            add(Property().withKey("database.defaultSchemaName").withValue("YOUR_SCHEMA_NAME"))
                            add(Property().withKey("database.liquibaseSchemaName").withValue("PUBLIC"))
                        }
                    }
                }
            }
        }
    }
}

tasks.named<JooqGenerate>("generateJooq").configure {
    (runtimeClasspath as? ConfigurableFileCollection)?.from(tasks.processResources.get().outputs.files)
    allInputsDeclared = true
}

tasks.test {
    useJUnitPlatform()
}
