plugins {
    application
    java
}

group = "com.miti99"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.36")
    compileOnly("org.projectlombok:lombok:1.18.36")

    implementation("io.micrometer:micrometer-core:1.14.2")
    implementation("io.micrometer:micrometer-registry-elastic:1.14.2")

    implementation("ch.qos.logback:logback-classic:1.5.15")
    implementation("net.logstash.logback:logstash-logback-encoder:8.0")
}

application {
    mainClass.set("com.miti99.MicrometerDemo")
}
