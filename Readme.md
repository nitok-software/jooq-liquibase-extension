 # Jooq Liquibase-testcontainer extension
This jooq extension enables you to generate code from a liquibase changeset that is a applied to a real database and not only H2 as the official jooq-liquibase extension does.

This project is in its early stages - but it is still quite usable you only use one class

| Version | Supported JOOQ | Supported Liquibase |
|---------|----------------|---------------------|
| 0.x.x   | 3.16.x         | 4.23                |
> The liquibase version can be changed by importing into the same scope as long as it has no breaking changes

## Supports

* MySQL
* Postgres

Further implementations are extremely easy (as long as supported by liquibase, jooq and testcontainers).
Just open a PR - its just copy paste and adding a dependency

## Full Example

There is a mininal, fully tested & working example under [example](./example)

## Usage
> This library is NOT published on maven central! If you want to use it use one of theese options:
> - [jitpack](https://jitpack.io/#nitok-software/jooq-liquibase-extension)
> - git clone + git submodule + gradle `includeBuild`
>   - This means you clone this repo into the repo you want to use it in
>   - Then add it as **git submodule** so that you do not check in my code
>   - use `includeBuild` in gradle in order to use it as dependency

### Maven
Should work basically the same as gradle but not documented yet
### Gradle
```groovy
dependencies {
    jooqGenerator("eu.nitok:jooq-liquibase")//this repository
    jooqGenerator('org.liquibase:liquibase-core:4.23.0')//(optional!) only needed when pinning a version
}
jooq {
    version = '3.16.3'
    configurations {
        main {
            generationTool {
                jdbc {
                    driver = "org.testcontainers.jdbc.ContainerDatabaseDriver"
                    url = "jdbc:tc:mysql:8.0.33:///;databaseName=YOUR_SCHEMA_NAME?TC_TMPFS=/testtmpfs:rw&user=root"
                }
                generator {
                    database {
                        name = 'eu.nitok.jooq.extension.MySqlLiquibaseDatabase'
                        properties {
                            property {
                                key = 'scripts'
                                value = "path/to/your/changelogs.xml"
                            }
                            property {
                                key = 'database.defaultSchemaName'
                                value = 'YOUR_SCHEMA_NAME'
                            }
                            property {
                                key = 'database.liquibaseSchemaName'
                                value = 'PUBLIC'
                            }
                        }
                    }
                }
            }
        }
    }
}
```
Tipps:
  - You need to make sure that `path/to/your/changelogs.xml` is on the `jooqGenerator` classpath!
      - kotlin DSL
        ```kotlin
        tasks.named<JooqGenerate>("generateJooq").configure {
            (runtimeClasspath as? ConfigurableFileCollection)?.from(tasks.processResources.get().outputs.files)
            allInputsDeclared = true 
        }
        ```
      - groovy DSL
        ```groovy
        tasks.named('generateJooq').configure {
            dependsOn(processResources)
            getRuntimeClasspath().from("$projectDir/src/main/resources")
            allInputsDeclared = true
        }
        ```