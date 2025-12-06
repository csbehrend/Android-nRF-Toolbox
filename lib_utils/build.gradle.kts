plugins {
    alias(libs.plugins.nordic.feature)
}

android {
    namespace = "no.nordicsemi.android.toolbox.lib.utils"
    defaultConfig {
        minSdk = 33
    }
}

dependencies {
    implementation(libs.nordic.log.timber)
}