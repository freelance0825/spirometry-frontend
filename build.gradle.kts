// Project-level build.gradle.kts
buildscript {
    repositories {
        google() // Ensure this repository is included
        mavenCentral()
    }
    dependencies {
        // Add the Google services classpath
        classpath("com.google.gms:google-services:4.3.14")
    }
}