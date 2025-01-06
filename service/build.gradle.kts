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
}