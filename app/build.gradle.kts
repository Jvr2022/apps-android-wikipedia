plugins {
    id("com.android.application")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlinx-serialization")
}

// Copy the signing.properties.sample file to ~/.sign/signing.properties and adjust the values.
val PROD_PROPS_FILE = File(System.getProperty("user.home"), ".sign/signing.properties")
val REPO_PROPS_FILE = File("repo.properties")
val PROD_PROPS = loadProperties(PROD_PROPS_FILE)
val REPO_PROPS = loadProperties(REPO_PROPS_FILE)

fun computeVersionName(versionCode: Int, label: String): String {
    return "2.7.$versionCode-$label-${java.time.LocalDate.now()}"
}

val JAVA_VERSION = JavaVersion.VERSION_17

android {
    compileSdkVersion(34)
    compileOptions {
        coreLibraryDesugaringEnabled(true)
        sourceCompatibility = JAVA_VERSION
        targetCompatibility = JAVA_VERSION
    }
    kotlinOptions {
        jvmTarget = JAVA_VERSION
    }
    defaultConfig {
        applicationId = "org.wikipedia"
        minSdkVersion(21)
        targetSdkVersion(34)
        versionCode = 50473
        testApplicationId = "org.wikipedia.test"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
        vectorDrawables.useSupportLibrary = true
        signingConfig = signingConfigs.getByName("debug")
        buildConfigField("String", "DEFAULT_RESTBASE_URI_FORMAT", "\"%1\$s://%2\$s/api/rest_v1/\"")
        buildConfigField("String", "META_WIKI_BASE_URI", "\"https://meta.wikimedia.org\"")
        buildConfigField("String", "EVENTGATE_ANALYTICS_EXTERNAL_BASE_URI", "\"https://intake-analytics.wikimedia.org\"")
        buildConfigField("String", "EVENTGATE_LOGGING_EXTERNAL_BASE_URI", "\"https://intake-logging.wikimedia.org\"")
        val TEST_LOGIN_USERNAME = System.getenv("TEST_LOGIN_USERNAME")
        val TEST_LOGIN_PASSWORD = System.getenv("TEST_LOGIN_PASSWORD")
        buildConfigField("String", "TEST_LOGIN_USERNAME", TEST_LOGIN_USERNAME?.let { "\"$it\"" } ?: "\"Foo\"")
        buildConfigField("String", "TEST_LOGIN_PASSWORD", TEST_LOGIN_PASSWORD?.let { "\"$it\"" } ?: "\"Bar\"")
    }
    testOptions {
        execution = TestExecution.ANDROIDX_TEST_ORCHESTRATOR
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    sourceSets {
        create("prod") {
            java.srcDirs += "src/extra/java"
        }
        create("beta") {
            java.srcDirs += "src/extra/java"
        }
        create("alpha") {
            java.srcDirs += "src/extra/java"
        }
        create("dev") {
            java.srcDirs += "src/extra/java"
        }
        create("custom") {
            java.srcDirs += "src/extra/java"
        }
        create("androidTest") {
            assets.srcDirs += files("$projectDir/schemas".toString())
        }
    }
    signingConfigs {
        create("prod") {
            setSigningConfigKey(this, PROD_PROPS)
        }
        create("debug") {
            setSigningConfigKey(this, REPO_PROPS)
        }
    }
    buildTypes {
        create("debug") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            testProguardFiles("test-proguard-rules.pro")
        }
        create("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            testProguardFiles("test-proguard-rules.pro")
        }
    }
    flavorDimensions("default")
    productFlavors {
        create("dev") {
            versionName = computeVersionName(defaultConfig.versionCode, "dev")
            applicationIdSuffix = ".dev"
            buildConfigField("String", "META_WIKI_BASE_URI", "\"https://meta.wikimedia.beta.wmflabs.org\"")
            buildConfigField("String", "EVENTGATE_ANALYTICS_EXTERNAL_BASE_URI", "\"https://intake-analytics.wikimedia.beta.wmflabs.org\"")
            buildConfigField("String", "EVENTGATE_LOGGING_EXTERNAL_BASE_URI", "\"https://intake-logging.wikimedia.beta.wmflabs.org\"")
        }
        create("prod") {
            versionName = computeVersionName(defaultConfig.versionCode, "r")
            signingConfig = signingConfigs.getByName("prod")
        }
        create("alpha") {
            versionName = computeVersionName(defaultConfig.versionCode, "alpha")
            applicationIdSuffix = ".alpha"
        }
        create("beta") {
            versionName = computeVersionName(defaultConfig.versionCode, "beta")
            applicationIdSuffix = ".beta"
            signingConfig = signingConfigs.getByName("prod")
        }
        create("fdroid") {
            versionName = computeVersionName(defaultConfig.versionCode, "fdroid")
            signingConfig = signingConfigs.getByName("prod")
        }
        create("custom") {
            versionName = computeVersionName(defaultConfig.versionCode, customChannel)
            manifestPlaceholders["customChannel"] = getProperty("customChannel").toString()
            signingConfig = signingConfigs.getByName("prod")
        }
    }
    testOptions {
        unitTests {
            includeAndroidResources = true
            returnDefaultValues = true
        }
    }
    bundle {
        language {
            enableSplit(false)
        }
    }
    namespace("org.wikipedia")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

configurations {
    compileClasspath {
        extendsFrom(implementation)
    }
}

apply(from = "../gradle/src/test.gradle")
apply(from = "../gradle/src/checkstyle.gradle")
apply(from = "../gradle/src/ktlint.gradle")

dependencies {
    // To keep the Maven Central dependencies up-to-date
    // use http://gradleplease.appspot.com/ or http://search.maven.org/.
    // Debug with ./gradlew -q app:dependencies --configuration compile
    val okHttpVersion = "4.12.0"
    val retrofitVersion = "2.9.0"
    val glideVersion = "4.16.0"
    val mockitoVersion = "5.2.0"
    val leakCanaryVersion = "2.13"
    val kotlinCoroutinesVersion = "1.7.3"
    val firebaseMessagingVersion = "23.4.0"
    val mlKitVersion = "17.0.4"
    val roomVersion = "2.6.1"
    val espressoVersion = "3.5.1"
    val serializationVersion = "1.6.2"
    val metricsVersion = "2.3"
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$kotlinCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.browser:browser:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("com.android.installreferrer:installreferrer:2.2")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("org.wikimedia.metrics:metrics-platform:$metricsVersion")
    implementation("com.github.michael-rapp:chrome-like-tab-switcher:0.4.6") {
        exclude(group = "org.jetbrains")
    }
    implementation("com.github.bumptech.glide:glide:$glideVersion")
    implementation("com.github.bumptech.glide:okhttp3-integration:$glideVersion")
    ksp("com.github.bumptech.glide:ksp:$glideVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okHttpVersion")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:adapter-rxjava3:$retrofitVersion")
    implementation("io.reactivex.rxjava3:rxjava:3.1.8")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    implementation("com.github.skydoves:balloon:1.6.4")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("org.maplibre.gl:android-sdk:10.2.0")
    implementation("org.maplibre.gl:android-plugin-annotation-v9:2.0.2")
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    implementation("androidx.room:room-rxjava3:$roomVersion")
    // For language detection during editing
    prodImplementation("com.google.mlkit:language-id:$mlKitVersion")
    betaImplementation("com.google.mlkit:language-id:$mlKitVersion")
    alphaImplementation("com.google.mlkit:language-id:$mlKitVersion")
    devImplementation("com.google.mlkit:language-id:$mlKitVersion")
    customImplementation("com.google.mlkit:language-id:$mlKitVersion")
    // For receiving push notifications for logged-in users.
    prodImplementation("com.google.firebase:firebase-messaging-ktx:$firebaseMessagingVersion")
    betaImplementation("com.google.firebase:firebase-messaging-ktx:$firebaseMessagingVersion")
    alphaImplementation("com.google.firebase:firebase-messaging-ktx:$firebaseMessagingVersion")
    devImplementation("com.google.firebase:firebase-messaging-ktx:$firebaseMessagingVersion")
    customImplementation("com.google.firebase:firebase-messaging-ktx:$firebaseMessagingVersion")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:$leakCanaryVersion")
    implementation("com.squareup.leakcanary:plumber-android:$leakCanaryVersion")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-inline:$mockitoVersion")
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("com.squareup.okhttp3:okhttp:$okHttpVersion")
    testImplementation("com.squareup.okhttp3:mockwebserver:$okHttpVersion")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("androidx.room:room-testing:$roomVersion")
    androidTestImplementation("androidx.test.espresso:espresso-core:$espressoVersion")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:$espressoVersion")
    androidTestImplementation("androidx.test.espresso:espresso-intents:$espressoVersion")
    androidTestImplementation("androidx.test.espresso:espresso-web:$espressoVersion")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    androidTestImplementation("androidx.room:room-testing:$roomVersion")
    androidTestUtil("androidx.test:orchestrator:1.4.2")
}

private fun setSigningConfigKey(config: SigningConfig, props: Properties?) {
    if (props != null) {
        config.storeFile = props["keystore"]?.let { file(it) }
        config.storePassword = props["store.pass"] as String
        config.keyAlias = props["key.alias"] as String
        config.keyPassword = props["key.pass"] as String
    }
}

private fun loadProperties(file: File): Properties? {
    val props = Properties()
    if (file.canRead()) {
        props.load(FileInputStream(file))
    } else {
        System.err.println("\"$file\" not found")
    }
    return props
}
