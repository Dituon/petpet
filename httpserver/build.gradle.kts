plugins {
    id("java")
    id ("com.gradleup.shadow") apply true
}

dependencies {
    implementation(project(":core"))
    implementation(project(":service"))

    implementation("io.javalin:javalin:6.3.0")
    implementation("ch.qos.logback:logback-classic:1.5.15")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "moe.dituon.petpet.httpserver.ServerMain"
    }
}

