plugins {
    id("java")
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "petpet-core"
            from(components["java"])
        }
    }
}

dependencies {
    implementation("net.objecthunter:exp4j:0.4.8")
    implementation("net.coobird:thumbnailator:0.4.20")
    implementation("com.jhlabs:filters:2.0.235-1")
    implementation("com.madgag:animated-gif-lib:1.4")
    implementation("com.pngencoder:pngencoder:0.15.0")
}