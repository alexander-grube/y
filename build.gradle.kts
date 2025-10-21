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
    implementation("io.undertow:undertow-core:2.3.20.Final")
    implementation("com.alibaba.fastjson2:fastjson2:2.0.59")
    implementation("org.postgresql:postgresql:42.7.8")
    implementation("com.zaxxer:HikariCP:7.0.2")
    implementation("io.github.cdimascio:dotenv-java:3.2.0")
    implementation("at.favre.lib:bcrypt:0.10.2")
    implementation("org.eclipse.collections:eclipse-collections-api:13.0.0")
    implementation("org.eclipse.collections:eclipse-collections:13.0.0")
    implementation("io.minio:minio:8.6.0")
    implementation("io.github.mojtabaj:c-webp:1.1.0")
    implementation("org.bytedeco:javacv-platform:1.5.12")
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}