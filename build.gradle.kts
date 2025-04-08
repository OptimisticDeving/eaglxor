import xyz.jpenilla.runpaper.task.RunServer

plugins {
  id("java")
  id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.2.0"
  id("io.papermc.paperweight.userdev") version "2.0.0-beta.14"
  id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "dev.optimistic"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()
  maven("https://repo.papermc.io/repository/maven-public/")
  maven("https://repo.viaversion.com/")
}

val bundle by configurations.creating {
  isTransitive = false
}

configurations {
  compileClasspath {
    extendsFrom(bundle)
  }
}

dependencies {
  paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")

  compileOnly(libs.viaversion.api)
  compileOnly(libs.viaversion.bukkit)
  compileOnly(libs.viabackwards)

  bundle(libs.netty.codec.http)
}

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

tasks {
  withType<ProcessResources> {
    filteringCharset = "UTF-8"
  }

  withType<JavaCompile> {
    options.encoding = "UTF-8"
  }

  withType<RunServer> {
    systemProperty("com.mojang.eula.agree", true)
  }

  processResources {
    dependsOn(bundle)

    from("LICENSE")

    bundle.forEach { from(zipTree(it)) }
  }
}

bukkitPluginYaml {
  authors.addAll("OptimisticDev", "amyavi")

  depend.addAll("ViaVersion", "ViaBackwards")
  softDepend.add("ViaRewind")

  main = "dev.optimistic.eaglxor.Main"
  apiVersion = "1.21"
}