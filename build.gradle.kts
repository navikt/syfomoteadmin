import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.syfo"
version = "1.0.0"

val nimbusSDKVersion = "7.0.3"
val oidcSupportVersion = "0.2.18"
val kotlinJacksonVersion = "2.11.2"

val mqVersion = "9.0.4.0"
val tjenesteSpesifikasjonerVersion = "1.2019.09.25-00.21-49b69f0625e0"
val tjenesteSpesifikasjonerGithubVersion = "1.2020.06.11-19.53-1cad83414166"

val prometheusVersion = "1.0.6"
val logstashLogbackEncoderVersion = "4.10"
val slf4jVersion = "1.7.25"
val javaxWsRsVersion = "2.0.1"
val javaxInjectVersion = "1"
val jose4jVersion = "0.5.0"
val aspectjweaverVersion = "1.8.8"
val apacheCommonsVersion = "3.5"
val javaxActivationVersion = "1.2.0"
val javaxMailVersion = "1.5.0-b01"
val jaxRiVersion = "2.3.2"

val flywayVersion = "5.1.4"
val ojdbc8Version = "19.3.0.0"

plugins {
    kotlin("jvm") version "1.4.10"
    id("io.freefair.lombok") version "5.1.0"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.4.10"
    id("java")
    id("com.github.johnrengelman.shadow") version "6.0.0"
    id("org.springframework.boot") version "2.1.8.RELEASE"
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
}

allOpen {
    annotation("org.springframework.context.annotation.Configuration")
    annotation("org.springframework.stereotype.Service")
    annotation("org.springframework.stereotype.Component")
}

val githubUser: String by project
val githubPassword: String by project
repositories {
    mavenCentral()
    maven(url = "https://repo1.maven.org/maven2/")
    maven {
        url = uri("https://maven.pkg.github.com/navikt/tjenestespesifikasjoner")
        credentials {
            username = githubUser
            password = githubPassword
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
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

    implementation("com.sun.xml.ws:jaxws-ri:$jaxRiVersion")
    implementation("com.sun.activation:javax.activation:$javaxActivationVersion")

    implementation("com.nimbusds:oauth2-oidc-sdk:$nimbusSDKVersion")
    implementation("no.nav.security:oidc-spring-support:$oidcSupportVersion")
    testImplementation("no.nav.security:oidc-test-support:$oidcSupportVersion")

    implementation("com.ibm.mq:com.ibm.mq.allclient:$mqVersion")
    implementation("no.nav.tjenestespesifikasjoner:varsel-inn:$tjenesteSpesifikasjonerVersion")
    implementation("no.nav.tjenestespesifikasjoner:nav-virksomhet-stoppReVarsel-v1-meldingsdefinisjon:$tjenesteSpesifikasjonerVersion")
    implementation("no.nav.tjenestespesifikasjoner:nav-virksomhet-varselMedHandling-v1-meldingsdefinisjon:$tjenesteSpesifikasjonerVersion")
    implementation("no.nav.tjenestespesifikasjoner:nav-virksomhet-opprettOppgavehenvendelse-v1-meldingsdefinisjon:$tjenesteSpesifikasjonerVersion")
    implementation("no.nav.tjenestespesifikasjoner:servicemeldingMedKontaktinformasjon-v1-tjenestespesifikasjon:$tjenesteSpesifikasjonerGithubVersion")

    implementation("io.micrometer:micrometer-registry-prometheus:$prometheusVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashLogbackEncoderVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
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


    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
}
