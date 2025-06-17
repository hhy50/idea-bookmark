plugins {
  id("java")
  kotlin("jvm") version "1.9.0"
  id("org.jetbrains.intellij") version "1.13.3"
}

group = "io.github.hhy"
version = "1.2.0"

repositories {
  mavenLocal()
  mavenCentral()
}

dependencies {

}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
  version.set("2023.1")
  type.set("IC") // Target IDE Platform
  plugins.set(listOf())
}

tasks {
  // Set the JVM compatibility versions
  withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
  }

  signPlugin {
    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    privateKey.set(System.getenv("PRIVATE_KEY"))
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
  }

  publishPlugin {
    token.set(System.getenv("PUBLISH_TOKEN"))
  }
  patchPluginXml {
    version.set(project.version.toString())
    sinceBuild.set("231")
    untilBuild.set("")
  }
}

sourceSets {
  main {
    java {
      srcDir("src/java")
    }
    resources {
      srcDir("src/resources")
    }
  }
  test {
    java {
      srcDir("test/java")
    }
  }
}
