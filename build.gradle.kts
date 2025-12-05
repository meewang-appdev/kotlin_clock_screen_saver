buildscript {
    dependencies {
        classpath("com.android.tools:r8:8.7.18")
    }
}

plugins {
    id("com.android.application") version "8.5.0" apply false
    kotlin("android") version "1.9.23" apply false
}
