val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
  kotlin("jvm") version "1.8.21"
  id("io.ktor.plugin") version "2.3.0"
  id("org.jetbrains.kotlin.plugin.serialization") version "1.8.21"
  id("com.apollographql.apollo3") version "3.8.2"
}

group = "com.lanigan"
version = "0.0.1"
application {
  mainClass.set("com.lanigan.ApplicationKt")

  val isDevelopment: Boolean = project.ext.has("development")
  applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-auth-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-default-headers-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
  implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
  implementation("ch.qos.logback:logback-classic:$logback_version")
  implementation("com.apollographql.apollo3:apollo-runtime:3.8.2")

  testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

apollo {
  service("service") {
    packageName.set("com.lanigan")
  }
}