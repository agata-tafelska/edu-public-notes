import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME

plugins {
    kotlin("jvm")
    kotlin("plugin.spring") version Dependencies.pluginSpringVersion
    kotlin("plugin.allopen") version Dependencies.pluginSpringVersion
    id("org.springframework.boot") version Dependencies.pluginSpringBootFrameworkVersion
    id("io.spring.dependency-management") version Dependencies.pluginSpringDependencyManagementVersion
    id("org.openapi.generator")
}

val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = 1.8.toString()

dependencies {
    // Spring Boot
    implementation(group = "org.springframework.boot", name = "spring-boot-starter-web")
    implementation(group = "org.springframework.boot", name = "spring-boot-starter-validation")
    implementation(group = "org.springframework.boot", name = "spring-boot-starter-data-jdbc")

    // Kotlin
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-reflect")
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-stdlib-jdk8")
    implementation(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin", version = Dependencies.jacksonKotlinModuleVersion)

    // Exposed ORM framework
    implementation(group = "org.jetbrains.exposed", name = "exposed-core", version = Dependencies.jetBrainsExposedVersion)
    implementation(group = "org.jetbrains.exposed", name = "exposed-jdbc", version = Dependencies.jetBrainsExposedVersion)
    implementation(group = "org.jetbrains.exposed", name = "exposed-java-time", version = Dependencies.jetBrainsExposedVersion)

    // Postgres DB driver
    implementation(group = "org.postgresql", name = "postgresql", version = Dependencies.postgresqlVersion)

    // Flyway database migration
    implementation(group = "org.flywaydb", name = "flyway-core", version = Dependencies.flywayDbMigrationVersion)
}

tasks.test {
    useJUnitPlatform()
}

// region Deployments
tasks.register("buildDockerImage") {
    group = "deployment"
    description = "Build service's docker image"

    dependsOn("bootJar")

    doLast {
        exec {
            commandLine("docker")
            args("build", "-t", "service-notes", ".")
        }
    }
}
// endregion

// region Open API code generation

val generatedCodeDirectoryPath = "$buildDir/open-api-generated"
val apiSpecPath = "$projectDir/api/notesApi.yaml"

tasks.openApiGenerate {
    setProperty("generatorName", "kotlin-spring")
    setProperty("validateSpec", true)
    setProperty("inputSpec", apiSpecPath)
    setProperty("outputDir", generatedCodeDirectoryPath)
    setProperty(
        "configOptions", mapOf(
            "interfaceOnly" to "true",
            "useBeanValidation" to "true",
            "packageName" to "com.atafelska.service.notes.generated",
            "modelPackage" to "com.atafelska.service.notes.generated"
        )
    )
}

tasks.apply {
    clean { finalizedBy(openApiGenerate) }
    compileJava { dependsOn(openApiGenerate) }
}

sourceSets[MAIN_SOURCE_SET_NAME].java {
    srcDir(generatedCodeDirectoryPath + File.separator + "src" + File.separator + "main" + File.separator + "kotlin")
}

// endregion

// region Local infrastructure

val dockerComposeInfraPath = projectDir.absolutePath + File.separator + "docker-compose-infra.yml"

tasks.register("runLocalInfrastructure") {
    group = "local development"
    description = "Runs infrastructure required by service locally"

    doLast {
        exec {
            commandLine("docker-compose")
            args("-f", dockerComposeInfraPath, "up")
        }
    }
}

tasks.register("cleanLocalInfrastructure") {
    group = "local development"
    description = "Cleans up local infrastructure"

    doLast {
        exec {
            commandLine("docker-compose")
            args("-f", dockerComposeInfraPath, "down")
        }
    }
}

// endregion
