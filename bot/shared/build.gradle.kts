plugins {
    id("java")
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "petpet-bot-shared"
            from(components["java"])
        }
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":script"))
    implementation(project(":service"))
    compileOnly("org.openjdk.nashorn:nashorn-core:15.4")
}