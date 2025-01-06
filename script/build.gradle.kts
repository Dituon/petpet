plugins {
    id("java")
}

dependencies {
    implementation(project(":core"))
    implementation("org.openjdk.nashorn:nashorn-core:15.4")
}