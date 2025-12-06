plugins {
    alias(libs.plugins.nordic.feature)
}

android {
    namespace = "no.nordicsemi.android.toolbox.profile.parser"
    defaultConfig {
        minSdk = 33
    }
}

dependencies {
    implementation(libs.nordic.blek.client.android)
    implementation(libs.nordic.kotlin.data)
    implementation(kotlin("reflect"))

    // Unit test dependencies
    testImplementation(libs.junit4)
    testImplementation(libs.kotlin.junit)
}