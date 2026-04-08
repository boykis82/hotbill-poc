plugins {
    java
}

dependencies {
    // Jakarta Validation
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")

    // Jackson for JSON processing
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.16.1")
}
