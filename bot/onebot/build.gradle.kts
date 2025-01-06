plugins {
    id("java")
    id ("com.gradleup.shadow") apply true
}

dependencies {
    implementation(project(":core"))
    implementation(project(":script"))
    implementation(project(":service"))
    implementation(project(":bot:shared"))
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("top.mrxiaom.mirai:onebot:1.0.1")
    implementation("org.java-websocket:Java-WebSocket:1.5.7")
    implementation("net.mamoe.yamlkt:yamlkt:0.13.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    implementation("ch.qos.logback:logback-classic:1.5.15")

    compileOnly("org.openjdk.nashorn:nashorn-core:15.4")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "moe.dituon.petpet.bot.qq.onebot.MainKt"
    }
}

repositories {
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
}
