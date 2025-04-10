plugins {
  id("java")
}

group = "dev.optimistic"
version = "1.0.0-SNAPSHOT"

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

val bundle by configurations.creating {
  isTransitive = false
}

repositories {
  mavenCentral()
}

configurations {
  compileClasspath {
    extendsFrom(bundle)
  }
}

tasks {
  withType<ProcessResources> {
    filteringCharset = "UTF-8"
  }

  withType<JavaCompile> {
    options.encoding = "UTF-8"
  }

  processResources {
    dependsOn(bundle)

    doFirst {
      bundle.forEach { from(zipTree(it)) }
    }
  }
}