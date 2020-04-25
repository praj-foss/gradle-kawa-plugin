plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.3.41"
    id("com.gradle.plugin-publish") version "0.11.0"
}

group       = "in.praj.kawa"
version     = "0.1.0"
description = "Add Kawa language support to Gradle projects"

repositories {
    jcenter()
}

dependencies {
    implementation(platform(kotlin("bom")))
    implementation(kotlin("stdlib-jdk8"))

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
}

gradlePlugin {
    plugins {
        create("gradleKawaPlugin") {
            id = "in.praj.kawa"
            displayName = "Kawa plugin for Gradle"
            description = project.description
            implementationClass = "in.praj.kawa.KawaPlugin"
        }
    }
}

val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

gradlePlugin.testSourceSets(functionalTestSourceSet)
configurations.getByName("functionalTestImplementation")
        .extendsFrom(configurations.getByName("testImplementation"))

val functionalTest by tasks.creating(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
}

val check by tasks.getting(Task::class) {
    dependsOn(functionalTest)
}

pluginBundle {
    vcsUrl      = "https://github.com/praj-foss/gradle-kawa-plugin"
    website     = vcsUrl
    description = project.description
    tags        = listOf("kawa", "scheme", "language")
}
