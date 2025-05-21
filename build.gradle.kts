plugins {
    id("java")
}

group = "ru.zaostrovtsev"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.sparkjava:spark-core:2.9.4")
    implementation ("org.jfree:org.jfree.svg:5.0.4")
    implementation ("org.jfree:jfreechart:1.5.4")
    implementation ("org.slf4j:slf4j-simple:2.0.7")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("redis.clients:jedis:5.0.2")
    implementation ("org.jfree:jfreechart:1.5.4")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
}

tasks.test {
    useJUnitPlatform()
}
