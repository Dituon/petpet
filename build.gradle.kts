import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.mamoe.mirai.console.gradle.BuildMiraiPluginV2
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.serialization") version "1.9.24" apply false
    kotlin("plugin.lombok") version "2.0.21" apply false
    id("com.gradleup.shadow") version ("8.3.5") apply false
    id("net.mamoe.mirai-console") version ("2aa96098bb") apply false
}

val releasePath: File = rootProject.layout.buildDirectory.get().asFile.resolve("releases").absoluteFile

tasks.register<Jar>("sourceJar") {
    group = JavaBasePlugin.BUILD_TASK_NAME
    description = "build source jar"
    from(subprojects.map { it.sourceSets["main"].allSource })
    archiveClassifier.set("sources")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    destinationDirectory = releasePath
}

tasks.register("setReleaseOutputPath") {
    group = JavaBasePlugin.BUILD_TASK_NAME
    description = "set release jars name and output path"
    if (!releasePath.exists()) releasePath.mkdirs()

    project(":bot:onebot-petpet").tasks.named<ShadowJar>("shadowJar") {
        archiveBaseName.set("petpet-onebot")
        archiveClassifier.set("")
        destinationDirectory.set(releasePath)
    }

    project(":httpserver").tasks.named<ShadowJar>("shadowJar") {
        archiveBaseName.set("petpet-http-server")
        archiveClassifier.set("")
        destinationDirectory.set(releasePath)
    }

    project(":bot:mirai-petpet").tasks.named<BuildMiraiPluginV2>("buildPlugin") {
        archiveBaseName.set("petpet")
        destinationDirectory.set(releasePath)
    }
}

tasks.register("releaseJars") {
    group = JavaBasePlugin.BUILD_TASK_NAME
    description = "test and build release jars"
    dependsOn("test")
    dependsOn("setReleaseOutputPath")
    dependsOn(":bot:mirai-petpet:buildPlugin")
    dependsOn(":bot:onebot-petpet:shadowJar")
    dependsOn(":httpserver:shadowJar")
    dependsOn("sourceJar")
}

// TODO: buildSrc
allprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "org.jetbrains.kotlin.plugin.lombok")

    group = "moe.dituon.petpet"
    version = "1.0.0-beta2"

    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

        compileOnly("org.projectlombok:lombok:1.18.34")
        annotationProcessor("org.projectlombok:lombok:1.18.34")
        testCompileOnly("org.projectlombok:lombok:1.18.34")
        testAnnotationProcessor("org.projectlombok:lombok:1.18.34")

        testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

        implementation("org.jetbrains:annotations:26.0.1")
        implementation("org.slf4j:slf4j-api:2.0.16")
        testImplementation("ch.qos.logback:logback-classic:1.5.15")
    }

    tasks.named<Test>("test") {
        useJUnitPlatform()
    }

    repositories {
        maven { setUrl("https://maven.aliyun.com/repository/public") }
        maven { setUrl("https://jitpack.io") }
        mavenCentral()
    }
}
