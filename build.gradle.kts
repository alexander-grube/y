plugins {
    id("java")
    application
}

group = "cto.shadow"
version = "1.0-SNAPSHOT"

application {
    mainClass = "cto.shadow.Main"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.undertow:undertow-core:2.3.18.Final")
    implementation("com.alibaba.fastjson2:fastjson2:2.0.56")
    implementation("org.postgresql:postgresql:42.7.5")
    implementation("com.zaxxer:HikariCP:6.3.0")
    implementation("io.github.cdimascio:dotenv-java:3.2.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}