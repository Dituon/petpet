plugins {
    id("java")
    id("net.mamoe.mirai-console") version ("2aa96098bb") apply true
}

dependencies {
    implementation(project(":core"))
    implementation(project(":script"))
    implementation(project(":service"))
    implementation(project(":bot:shared"))
    implementation("net.mamoe.yamlkt:yamlkt:0.13.0")
    compileOnly("org.openjdk.nashorn:nashorn-core:15.4")
}

mirai {
    jvmTarget = JavaVersion.VERSION_11
}