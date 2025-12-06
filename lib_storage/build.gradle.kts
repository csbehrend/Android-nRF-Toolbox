plugins {
    alias(libs.plugins.nordic.feature)
    alias(libs.plugins.ksp)

}

android {
    namespace = "no.nordicsemi.android.toolbox.lib.storage"
    defaultConfig {
        minSdk = 33
    }
}

dependencies {
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
}