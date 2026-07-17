// Top-level build file. Real plugin application happens in app/build.gradle.kts;
// declaring plugins here (with apply false) lets Gradle resolve a single shared
// version for every module, per the Android Gradle Plugin recommended setup.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
