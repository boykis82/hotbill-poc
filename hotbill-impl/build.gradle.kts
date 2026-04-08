plugins {
    java
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.4.1")
    }
}

configurations.all {
    resolutionStrategy {
        force("org.junit.jupiter:junit-jupiter-api:5.11.4")
        force("org.junit.jupiter:junit-jupiter-engine:5.11.4")
        force("org.junit.jupiter:junit-jupiter-params:5.11.4")
        force("org.junit.platform:junit-platform-commons:1.11.4")
        force("org.junit.platform:junit-platform-engine:1.11.4")
        force("org.junit.platform:junit-platform-launcher:1.11.4")
    }
}

dependencies {
    // API 모듈 의존성
    implementation(project(":hotbill-api"))

    // Spring Core (Spring Boot 없이)
    implementation("org.springframework:spring-context:6.2.1")
    implementation("org.springframework:spring-web:6.2.1")
    implementation("org.springframework:spring-webmvc:6.2.1")
    implementation("org.springframework:spring-jdbc:6.2.1")
    implementation("org.springframework:spring-tx:6.2.1")

    // MyBatis (Spring Boot starter 없이)
    implementation("org.mybatis:mybatis:3.5.15")
    implementation("org.mybatis:mybatis-spring:3.0.3")

    // Oracle JDBC Driver
    runtimeOnly("com.oracle.database.jdbc:ojdbc11:23.3.0.23.09")

    // Jackson for JSON processing
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")

    // Logging
    implementation("org.slf4j:slf4j-api")
    runtimeOnly("ch.qos.logback:logback-classic")

    // Lombok (Optional)
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    testCompileOnly("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")

    // Test dependencies - 명시적으로 버전 통일
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito:mockito-junit-jupiter")
    testImplementation("org.assertj:assertj-core")

    // Spring Test
    testImplementation("org.springframework:spring-test")

    // MyBatis Spring Boot (for mapper tests)
    testImplementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3") {
        exclude(group = "org.junit.jupiter")
        exclude(group = "org.junit.platform")
    }
    testImplementation("org.mybatis.spring.boot:mybatis-spring-boot-starter-test:3.0.3") {
        exclude(group = "org.junit.jupiter")
        exclude(group = "org.junit.platform")
    }

    // H2 Database for testing
    testRuntimeOnly("com.h2database:h2")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
    jvmArgs(
        "--add-opens", "java.base/java.lang=ALL-UNNAMED",
        "--add-opens", "java.base/java.util=ALL-UNNAMED"
    )
    maxHeapSize = "1024m"
}
