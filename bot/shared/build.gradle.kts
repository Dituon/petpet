plugins {
    id("java")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":script"))
    implementation(project(":service"))
    compileOnly("org.openjdk.nashorn:nashorn-core:15.4")
}