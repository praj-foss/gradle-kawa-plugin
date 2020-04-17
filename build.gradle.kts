plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.3.41"
}

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
    plugins.creating {
        id = "in.praj.kawa"
        implementationClass = "in.praj.kawa.KawaPlugin"
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
