plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.spring") version "2.3.21"
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

group = "org.printscript"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/Ingsis-printscript-grupo3/printscript")
        credentials {
            username = (findProperty("github.user") as String?) ?: System.getenv("GITHUB_ACTOR")
            password = (findProperty("github.token") as String?) ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    // GET /actuator/health: lo usa el compose para saber si el servicio esta arriba
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.printscript:runner:1.1.0")
    // el runner no los expone con api, y el servicio arma su propio linter y formatter
    implementation("org.printscript:linter:1.1.0")
    implementation("org.printscript:formatter:1.1.0")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// el ktlint que trae el plugin por defecto (1.0.1) no entiende el compilador de Kotlin 2.3
ktlint {
    version.set("1.7.1")
}
