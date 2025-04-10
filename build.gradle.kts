import xyz.jpenilla.runpaper.task.RunServer

plugins {
  id("module.java-conventions")
  id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.2.0"
  id("io.papermc.paperweight.userdev") version "2.0.0-beta.14"
  id("xyz.jpenilla.run-paper") version "2.3.1"
}

repositories {
  maven("https://repo.papermc.io/repository/maven-public/")
  maven("https://repo.viaversion.com/")
}

dependencies {
  paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")

  compileOnly(libs.viaversion.api)
  compileOnly(libs.viaversion.bukkit)
  compileOnly(libs.viabackwards)

  bundle(libs.netty.codec.http)
}

tasks {
  withType<RunServer> {
    systemProperty("com.mojang.eula.agree", true)
  }

  processResources {
    from("LICENSE")
  }
}

bukkitPluginYaml {
  authors.addAll("OptimisticDev", "amyavi")

  depend.addAll("ViaVersion", "ViaBackwards")
  softDepend.add("ViaRewind")

  main = "dev.optimistic.eaglxor.Main"
  apiVersion = "1.21"
}