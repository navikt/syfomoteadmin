import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.syfo"
version = "1.0.0"

val oidcSupportVersion = "0.2.18"
val kotlinLibVersion = "1.3.50"
val kotlinJacksonVersion = "2.9.8"

val mqVersion = "9.0.4.0"
val tjenesteSpesifikasjonerVersion = "1.2019.09.25-00.21-49b69f0625e0"
val varselMedHandlingV1Version = "1.0.0"
val stoppRevarselV1Version = "1.0.1"
val opprettOppgavehenvendelseV1Verion = "1.0.0"
val servicemeldingMedKontaktinformasjonV1Version = "1.0.0"

val prometheusVersion = "1.0.6"
val logstashLogbackEncoderVersion = "4.10"
val slf4jVersion = "1.7.25"
val javaxWsRsVersion = "2.0.1"
val javaxInjectVersion = "1"
val jose4jVersion = "0.5.0"
val aspectjweaverVersion = "1.8.8"
val apacheCommonsVersion = "3.5"
val javaxMailVersion = "1.5.0-b01"

val flywayVersion = "5.1.4"
val ojdbc8Version = "19.3.0.0"

plugins {
    kotlin("jvm") version "1.3.50"
    id("io.freefair.lombok") version "4.1.2"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.3.50"
    id("java")
    id("com.github.johnrengelman.shadow") version "4.0.3"
    id("org.springframework.boot") version "2.1.8.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
}

allOpen {
    annotation("org.springframework.context.annotation.Configuration")
    annotation("org.springframework.stereotype.Service")
    annotation("org.springframework.stereotype.Component")
}

repositories {
    mavenCentral()
    jcenter()
    maven(url="https://repo.adeo.no/repository/maven-releases/")
    maven(url="https://dl.bintray.com/kotlin/kotlinx/")
    maven(url = "https://repo1.maven.org/maven2/")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinLibVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinLibVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$kotlinJacksonVersion")

    implementation("org.apache.httpcomponents:httpclient:4.5.6")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-jersey")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.retry:spring-retry")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-jta-atomikos")
    implementation("org.springframework.boot:spring-boot-starter-cache")

    implementation("org.springframework.kafka:spring-kafka")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    implementation("org.springframework:spring-jms")

    implementation("no.nav.security:oidc-spring-support:$oidcSupportVersion")
    testImplementation("no.nav.security:oidc-test-support:$oidcSupportVersion")

    implementation("com.ibm.mq:com.ibm.mq.allclient:$mqVersion")
    implementation("no.nav.tjenestespesifikasjoner:varsel-inn:$tjenesteSpesifikasjonerVersion")
    implementation("no.nav.meldinger.virksomhet:nav-virksomhet-varselMedHandling-v1-meldingsdefinisjon:$varselMedHandlingV1Version:jaxb")
    implementation("no.nav.meldinger.virksomhet:nav-virksomhet-stoppReVarsel-v1-meldingsdefinisjon:$stoppRevarselV1Version:jaxb")
    implementation("no.nav.meldinger.virksomhet:nav-virksomhet-opprettOppgavehenvendelse-v1-meldingsdefinisjon:$opprettOppgavehenvendelseV1Verion:jaxb")
    implementation("no.nav.meldinger.virksomhet:nav-virksomhet-servicemeldingMedKontaktinformasjon-v1-meldingsdefinisjon:$servicemeldingMedKontaktinformasjonV1Version:jaxb")

    implementation("io.micrometer:micrometer-registry-prometheus:$prometheusVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashLogbackEncoderVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.projectlombok:lombok:1.16.22")
    annotationProcessor("org.projectlombok:lombok:1.18.6")
    implementation("javax.ws.rs:javax.ws.rs-api:$javaxWsRsVersion")
    implementation("javax.inject:javax.inject:$javaxInjectVersion")
    implementation("org.bitbucket.b_c:jose4j:$jose4jVersion")
    runtimeOnly("org.aspectj:aspectjweaver:$aspectjweaverVersion")
    implementation("org.apache.commons:commons-lang3:$apacheCommonsVersion")
    implementation("javax.mail:mail:$javaxMailVersion")

    api("org.flywaydb:flyway-core:$flywayVersion")
    implementation("com.oracle.ojdbc:ojdbc8:$ojdbc8Version")
    testImplementation("com.h2database:h2")
}

tasks {
    withType<Jar> {
        manifest.attributes["Main-Class"] = "no.nav.syfo.Application"
    }

    create("printVersion") {
        doLast {
            println(project.version)
        }
    }

    withType<ShadowJar> {
        transform(ServiceFileTransformer::class.java) {
            setPath("META-INF/cxf")
            include("bus-extensions.txt")
        }
        transform(PropertiesFileTransformer::class.java) {
            paths = listOf("META-INF/spring.factories")
            mergeStrategy = "append"
        }
        mergeServiceFiles()
    }


    named<KotlinCompile>("compileKotlin") {
        kotlinOptions.jvmTarget = "1.8"
    }

    named<KotlinCompile>("compileTestKotlin") {
        kotlinOptions.jvmTarget = "1.8"
    }
}
