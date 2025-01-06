rootProject.name = "petpet"

include("core")
include("script")
include("service")
include("httpserver")
include("bot:shared")
include("bot:mirai")
include("bot:onebot")

project(":bot:mirai").name = "mirai-petpet"
project(":bot:onebot").name = "onebot-petpet"

pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if(requested.id.toString() == "net.mamoe.mirai-console")
                useModule("com.github.dituon:mirai-console-gradle-plugin:2aa96098bb")
        }
    }
    repositories {
        gradlePluginPortal()
        maven("https://jitpack.io")
    }
}
