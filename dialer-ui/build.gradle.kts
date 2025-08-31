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
            baseName = "DialerUI"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":dialer-core"))

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.materialIconsExtended)
            implementation(libs.compose.runtime)

            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx.v1131)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.androidx.material3)
            implementation(libs.koin.android)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "io.voxity.dialer.ui"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = 30
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

//     Add this to ensure proper resource compilation
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
        version = "1.0.0-SNAPSHOT"

        // Set artifactId per target automatically
        pom {
            name.set("dialer-ui")
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
//            artifactId = "dialer-ui"
//            version = "1.0.0-SNAPSHOT"
//
//            artifact(tasks["sourcesJar"])
//            artifact(tasks["javadocJar"])
//
//            pom {
//                name.set("Voxity Dialer UI")
//                description.set("UI module for Voxity Dialer")
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
