plugins {
    id("java-library")
    id("me.champeau.jmh") version "0.7.2"
}

group = "io.sparkmc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    // https://mvnrepository.com/artifact/com.esotericsoftware/kryo
    implementation("com.esotericsoftware.kryo:kryo5:5.6.2")
    // https://mvnrepository.com/artifact/io.netty/netty-all
    implementation("io.netty:netty-all:4.2.0.RC3")
    // https://mvnrepository.com/artifact/org.projectlombok/lombok
    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")
    // https://mvnrepository.com/artifact/org.mockito/mockito-core
    testImplementation("org.mockito:mockito-core:5.15.2")
    // https://mvnrepository.com/artifact/org.assertj/assertj-core
    testImplementation("org.assertj:assertj-core:3.27.3")
    implementation("org.openjdk.jmh:jmh-core:1.35")
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.35")
}

jmh {
    warmupIterations = 20
    iterations = 20
    fork = 1
}

tasks.test {
    useJUnitPlatform()
}