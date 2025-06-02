plugins {
    id("java")
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "petpet-service"
            from(components["java"])
        }
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":script"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")
}