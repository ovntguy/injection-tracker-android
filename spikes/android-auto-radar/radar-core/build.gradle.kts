plugins {
    id("org.jetbrains.kotlin.jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("org.apache.commons:commons-compress:1.27.1")
    testImplementation(kotlin("test"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register<JavaExec>("renderProof") {
    group = "verification"
    description = "Fetch one NEXRAD Level 3 frame and one MRMS WMS frame; write docs PNGs."
    dependsOn(tasks.named("testClasses"))
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.deeeelay.aaradar.spike.ProofMain")
    workingDir = rootProject.projectDir
}
