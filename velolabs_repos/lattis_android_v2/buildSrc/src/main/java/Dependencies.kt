object PluginDependencies {
    val android = "com.android.tools.build:gradle:${Versions.gradleAndroidPlugin}"
    val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
}

object ProjectModules {
    val data = ":data"
    val domain = ":domain"
}

object ProjectDependencies {
    val kotlin = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    val androidx_core = "androidx.core:core-ktx:${Versions.androidx_core}"


    // retrofit
    val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    val rxJava2Adapter = "com.squareup.retrofit2:adapter-rxjava3:${Versions.retrofit}"
    val gson = "com.squareup.retrofit2:converter-gson:${Versions.retrofit}"
    val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:${Versions.loggingInterceptor}"


    // dagger
    val dagger = "com.google.dagger:dagger:${Versions.dagger}"
    val daggerAndroid = "com.google.dagger:dagger-android:${Versions.dagger}"
    val daggerSupport = "com.google.dagger:dagger-android-support:${Versions.dagger}"
    val daggerAnnotationProcessor = "com.google.dagger:dagger-android-processor:${Versions.dagger}"
    val daggerCompiler = "com.google.dagger:dagger-compiler:${Versions.dagger}"

    // RxKotlin
    val rxKotlin = "io.reactivex.rxjava3:rxkotlin:${Versions.rxkotlin}"
    val rxandroid = "io.reactivex.rxjava3:rxandroid:${Versions.rxandroid}"

    // ui
    val constraintLayout =  "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
    val material =  "com.google.android.material:material:${Versions.material}"

    //spinkit
    val spinkit = "com.github.ybq:Android-SpinKit:${Versions.spinkit}"

    //picasso
    val picasso = "com.squareup.picasso:picasso:${Versions.picasso}"

    val javaxAnnotation =  "javax.annotation:jsr250-api:${Versions.javax_annotation}"
    val javaxInject =  "javax.inject:javax.inject:${Versions.javax_inject}"



    //ui
    val cardview = "androidx.cardview:cardview:${Versions.cardview}"
    val recyclerView = "androidx.recyclerview:recyclerview:${Versions.recyclerView}"
    val appCompact = "androidx.appcompat:appcompat:${Versions.appCompact}"


    //showing red alert in text view
    val keyboardSurfer = "de.keyboardsurfer.android.widget:crouton:${Versions.keyboardSurfer}"


    //mapbox
    val mapbox =  "com.mapbox.mapboxsdk:mapbox-android-sdk:${Versions.mapbox}"
    val mapboxTurf = "com.mapbox.mapboxsdk:mapbox-sdk-turf:${Versions.mapboxTurf}"

    //google play service
//    val playServicePlaces = "com.google.android.gms:play-services-places:${Versions.playServicePlaces}"
    val googleLibPlaces = "com.google.android.libraries.places:places:${Versions.googleLibPlaces}"
    val googleServiceClasspath = "com.google.gms:google-services:${Versions.googleServiceClasspath}"
    val googlePlayCore = "com.google.android.play:core:${Versions.googlePlayCore}"

    //glide
    val glide = "com.github.bumptech.glide:glide:${Versions.glide}"
    val glideCompiler = "com.github.bumptech.glide:compiler:${Versions.glideCompiler}"

    //parceler
    val parceler = "org.parceler:parceler-api:${Versions.parceler}"
    val parcelerCompiler = "org.parceler:parceler:${Versions.parcelerCompiler}"

    //crop
    val soundCloudCrop = "com.soundcloud.android:android-crop:${Versions.soundCloudCrop}"

    //permission dispatcher
    val permissionDispatcher = "com.github.permissions-dispatcher:permissionsdispatcher:${Versions.permissionDispatcher}"
    val permissionDispatcherCompiler = "com.github.permissions-dispatcher:permissionsdispatcher-processor:${Versions.permissionDispatcherCompiler}"

    //stripe
    val stripe = "com.stripe:stripe-android:${Versions.stripe}"

    //zxing
    val zxing = "com.journeyapps:zxing-android-embedded:${Versions.zxing}"


    //bubbleview
    val bubbleView = "com.cpiz.bubbleview:bubbleview:${Versions.bubbleView}"


    //eazyImage
    val eazyImage = "com.github.jkwiecien:EasyImage:${Versions.eazyImage}"


    //firebase BOM

    val firebaseBom = "com.google.firebase:firebase-bom:${Versions.firebaseBom}"


    //firebaseAnalytics
    val firebaseAnalytics = "com.google.firebase:firebase-analytics-ktx"

    // firebase messaging
    val firebaseMessaging = "com.google.firebase:firebase-messaging-ktx"

    // firebase crashlytics
    val firebaseCrashlyticsGradle = "com.google.firebase:firebase-crashlytics-gradle:${Versions.firebaseCrashlyticsGradle}"
    val firebaseCrashlytic = "com.google.firebase:firebase-crashlytics-ktx"

    //realm
    val realmClasspath = "io.realm:realm-gradle-plugin:${Versions.realmClasspath}"

    //retrolambda
    val retrolambdaClasspath = "me.tatarka:gradle-retrolambda:${Versions.retrolambdaClasspath}"


    // RxBinding, for wrapping widget actions with Rx concepts.
    val rxbinding =  "com.jakewharton.rxbinding4:rxbinding-core:${Versions.rxbinding}"
    val materialRxBinding = "com.jakewharton.rxbinding4:rxbinding-material:${Versions.rxbinding}"
    val rxbindingAppCompact = "com.jakewharton.rxbinding4:rxbinding-appcompat:${Versions.rxbinding}"
    val rxbindingDesign = "com.jakewharton.rxbinding4:rxbinding-design:${Versions.rxbinding}"
    val rxbindingRecycler = "com.jakewharton.rxbinding4:rxbinding-recyclerview:${Versions.rxbinding}"

    // PhoneNumberUtil
    val phoneNumberUtil = "com.googlecode.libphonenumber:libphonenumber:${Versions.phoneNumberUtil}"

    //sliding panel
    val slidingPanel = "com.github.mancj:SlideUp-Android:${Versions.slidingPanel}"

    //timber
    val timber = "com.jakewharton.timber:timber:${Versions.timber}"

    // preference
    val preference = "androidx.preference:preference-ktx:${Versions.preference}"

    //single date time picker
    val singleDateTimePicker = "com.github.florent37:singledateandtimepicker:${Versions.singleDateTimePicker}"


    // parsing ISO-8601 durations
    val threeTenAbp = "com.jakewharton.threetenabp:threetenabp:${Versions.threeTenAbp}"

    // image slider
    val imageSlider = "com.github.smarteist:autoimageslider:${Versions.imageSlider}"

    // mercado pago service
//    val mercadoPagoService = "com.mercadopago:services:${Versions.mercadoPagoService}"

}

object ProjectTestDependencies{
    // tests
    val junit = "junit:junit:${Versions.junit}"
    val assertJ = "org.assertj:assertj-core:${Versions.assertJ}"
    val mockito = "org.mockito:mockito-core:${Versions.mockito}"
    val mockitoInline = "org.mockito:mockito-inline:${Versions.mockito}"
    val mockitoKotlin = "com.nhaarman:mockito-kotlin:${Versions.mockitoKotlin}"
    val mockitoAndroid = "org.mockito:mockito-android:${Versions.mockitoAndroid}"
    val androidTestRunner = "androidx.test.ext:junit:${Versions.androidTestRunner}"
    val androidTestRule = "androidx.test:rules:${Versions.androidTestRunner}"
    val espresso = "androidx.test.espresso:espresso-core:${Versions.espresso}"
    val kotlinJUnit = "org.jetbrains.kotlin:kotlin-test-junit:${Versions.kotlin}"
    val daggerCompiler = "com.google.dagger:dagger-compiler:${Versions.dagger}"
    val lifecycleTesting = "android.arch.core:core-testing:${Versions.lifecycle}"
    val kakao = "com.agoda.kakao:kakao:${Versions.kakao}"

}



object Versions {
    val gradleAndroidPlugin = "7.0.3"
    val kotlin = "1.5.31"

    val compileSdk = 31
    val targetSdk = 31
    val minSdkVersion = 21

    val releaseVersionCode = 554
    val releaseVersionName = "2.1.99"


    val support = "1.1.0"
    val androidx_core = "1.6.0"


    val retrofit = "2.9.0"
    val loggingInterceptor = "4.9.2"
    val dagger = "2.39.1"
    val rxkotlin = "3.0.1"
    val rxandroid = "3.0.0"


    val constraintLayout = "2.1.1"
    val material = "1.4.0"


    val javax_annotation = "1.0"
    val javax_inject= "1"

    val spinkit = "1.1.0"
    val picasso = "2.71828"

    val junit = "4.12"
    val assertJ = "3.9.1"
    val mockito = "2.22.0"
    val mockitoKotlin = "1.6.0"
    val mockitoAndroid = "2.6.1"
    val androidTestRunner = "1.1.0"
    val espresso = "3.2.0"
    val lifecycle = "2.4.0-rc01"
    val kakao = "2.1.0"
    val hugo = "1.2.1"
    val cardview = "1.0.0'"
    val recyclerView = "1.2.1"
    val appCompact = "1.3.1"
    val butterKnife = "10.1.0"
    val butterknifeClasspath ="10.1.0"
    val keyboardSurfer = "1.8.5@aar"
    val crashlytics = "2.10.1@aar"
    val crashlyticsClasspath = "1.28.0"
    val mapbox = "9.7.0"
    var mapboxTurf = "5.8.0"
    val playServicePlaces = "17.0.0"
    val googleLibPlaces = "2.4.0"
    val googleServiceClasspath = "4.3.10"
    val googlePlayCore = "1.10.2"
    val glide = "4.12.0"
    val glideCompiler = "4.12.0"
    val parceler = "1.1.13"
    val parcelerCompiler = "1.1.13"
    val soundCloudCrop = "1.0.1@aar"
    val permissionDispatcher = "4.9.1"
    val permissionDispatcherCompiler = "4.9.1"
    val stripe = "14.5.0"
    val zxing = "3.6.0"
    val bubbleView = "1.0.2"
    val eazyImage = "2.1.0"
    val firebaseBom = "28.4.2"
    val firebaseCrashlyticsGradle = "2.7.1"
    val realmClasspath = "10.8.0"
    val retrolambdaClasspath = "3.7.1"
    val rxbinding = "4.0.0"
    val googlePlayService = "17.0.0"
    val googlePlaces = "2.2.0"
    val phoneNumberUtil = "8.2.0"
    val slidingPanel = "2.2.8"
    val timber = "5.0.1"
    val preference = "1.1.1"
    val singleDateTimePicker = "2.2.6"
    val threeTenAbp = "1.3.1"
    val imageSlider = "1.4.0"
//    var
//    = "1.0.1"
}