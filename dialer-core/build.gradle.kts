import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    id("maven-publish")
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
        publishLibraryVariants("release")
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "DialerCore"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            implementation(libs.compose.runtime)
        }

        androidMain.dependencies {
            implementation(libs.androidx.core.ktx.v1131)
            implementation(libs.androidx.core.telecom)
            implementation(libs.koin.android)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "io.voxity.dialer.core"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = 30
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

/**
 * ✅ Create Empty Javadoc JAR (Required by Maven Central)
 */
tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    // Empty JAR since Android projects typically don't have Javadoc
}



publishing {
    publications.withType<MavenPublication>().configureEach {
        groupId = "io.voxity"
        version = "1.0.1"

        // Set artifactId per target automatically
        pom {
            name.set("dialer-core")
            description.set("Core module for Voxity Dialer")
        }
    }

    repositories {
        mavenLocal()
    }
}





//
//publishing {
//    publications {
//        create<MavenPublication>("maven") {
//            groupId = "io.voxity"
//            artifactId = "dialer-core"
//            version = "1.0.0-SNAPSHOT"
//
//            artifact(tasks["sourcesJar"])
//            artifact(tasks["javadocJar"])
//
//            pom {
//                name.set("Voxity Dialer Core")
//                description.set("Core module for Voxity Dialer")
//                url.set("https://github.com/VoxityTech/DialerApp")
//                licenses {
//                    license {
//                        name.set("Apache License 2.0")
//                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
//                    }
//                }
//                developers {
//                    developer {
//                        id.set("voxitytech")
//                        name.set("Nithish Sri Ram")
//                        email.set("nithish@voxity.org")
//                    }
//                }
//                scm {
//                    connection.set("scm:git:git://github.com/VoxityTech/DialerApp.git")
//                    developerConnection.set("scm:git:ssh://github.com/VoxityTech/DialerApp.git")
//                    url.set("https://github.com/VoxityTech/DialerApp")
//                }
//            }
//        }
//    }
//
//    repositories {
//        maven {
//            name = "OSSRH"
//            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
//            credentials {
//                username = (project.findProperty("ossrhUsername") as? String)
//                    ?: error("Missing property: ossrhUsername. Add it to gradle.properties or pass with -PossrhUsername=...")
//                password = (project.findProperty("ossrhPassword") as? String)
//                    ?: error("Missing property: ossrhPassword. Add it to gradle.properties or pass with -PossrhPassword=...")
//            }
//        }
//    }
//}
