import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
}

group = "me.user"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    // implementation("org.junit.jupiter:junit-jupiter:5.4.2")
    // testImplementation(kotlin("test-junit5"))
    // testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    // testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")

    testImplementation("io.kotest:kotest-runner-junit5:4.4.3")
    testImplementation("io.kotest:kotest-assertions-core:.4.4.3")

    testImplementation("org.mock-server:mockserver-netty:5.11.2")
    implementation("com.github.kittinunf.fuel:fuel:2.3.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}
