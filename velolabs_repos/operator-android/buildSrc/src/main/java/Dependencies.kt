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
    val rxJava2Adapter = "com.squareup.retrofit2:adapter-rxjava2:${Versions.retrofit}"
    val gson = "com.squareup.retrofit2:converter-gson:${Versions.retrofit}"
    val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:${Versions.loggingInterceptor}"


    // dagger
    val dagger = "com.google.dagger:dagger:${Versions.dagger}"
    val daggerAndroid = "com.google.dagger:dagger-android:${Versions.dagger}"
    val daggerSupport = "com.google.dagger:dagger-android-support:${Versions.dagger}"
    val daggerAnnotationProcessor = "com.google.dagger:dagger-android-processor:${Versions.dagger}"
    val daggerCompiler = "com.google.dagger:dagger-compiler:${Versions.dagger}"

    // RxKotlin
    val rxKotlin = "io.reactivex.rxjava2:rxkotlin:${Versions.rxkotlin}"
    val rxandroid = "io.reactivex.rxjava2:rxandroid:${Versions.rxandroid}"

    // ui
    val constraintLayout =  "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
    val material =  "com.google.android.material:material:${Versions.material}"


    val javaxAnnotation =  "javax.annotation:jsr250-api:${Versions.javax_annotation}"
    val javaxInject =  "javax.inject:javax.inject:${Versions.javax_inject}"

    val lifecycle = "android.arch.lifecycle:extensions:${Versions.lifecycle}"

    val hugo = "com.jakewharton.hugo:hugo-plugin:${Versions.hugo}"


    //retrolambda
    val retrolambdaClasspath = "me.tatarka:gradle-retrolambda:${Versions.retrolambdaClasspath}"


    // RxBinding, for wrapping widget actions with Rx concepts.
    val rxbinding =  "com.jakewharton.rxbinding2:rxbinding:${Versions.rxbinding}"
    val rxbindingAppCompact = "com.jakewharton.rxbinding2:rxbinding-appcompat-v7:${Versions.rxbinding}"
    val rxbindingDesign = "com.jakewharton.rxbinding2:rxbinding-design:${Versions.rxbinding}"
    val rxbindingRecycler = "com.jakewharton.rxbinding2:rxbinding-recyclerview-v7:${Versions.rxbinding}"


    //ui
    val cardview = "androidx.cardview:cardview:${Versions.cardview}"
    val recyclerView = "androidx.recyclerview:recyclerview:${Versions.recyclerView}"
    val appCompact = "androidx.appcompat:appcompat:${Versions.appCompact}"

    //glide
    val glide = "com.github.bumptech.glide:glide:${Versions.glide}"
    val glideCompiler = "com.github.bumptech.glide:compiler:${Versions.glideCompiler}"


    //zxing
    val zxing = "com.journeyapps:zxing-android-embedded:${Versions.zxing}"

    //permission dispatcher
    val permissionDispatcher = "org.permissionsdispatcher:permissionsdispatcher:${Versions.permissionDispatcher}"
    val permissionDispatcherCompiler = "org.permissionsdispatcher:permissionsdispatcher-processor:${Versions.permissionDispatcherCompiler}"

    // segment
    val androidSegment = "com.github.addisonelliott:SegmentedButton:${Versions.androidSegment}"

    //map
    val googleMap = "com.google.android.gms:play-services-maps:${Versions.googleMap}"

    // for clustering
    val googleMapUtil = "com.google.maps.android:android-maps-utils:${Versions.googleMapUtil}"
    val googleLibPlaces = "com.google.android.libraries.places:places:${Versions.googleLibPlaces}"
    val googleServiceClasspath = "com.google.gms:google-services:${Versions.googleServiceClasspath}"
    val googlePlayCore = "com.google.android.play:core:${Versions.googlePlayCore}"


    // preference
    val preference = "androidx.preference:preference-ktx:${Versions.preference}"

}


object Versions {
    val gradleAndroidPlugin = "4.1.1"
    val kotlin = "1.4.20"

    val compileSdk = 30
    val targetSdk = 30
    val minSdkVersion = 21
    val releaseVersionCode = 27
    val releaseVersionName = "1.0.8"


    val support = "1.1.0"
    val androidx_core = "1.3.0"


    val retrofit = "2.4.0"
    val loggingInterceptor = "3.11.0"
    val dagger = "2.25.4"
    val rxkotlin = "2.3.0"
    val rxandroid = "2.1.0"


    val constraintLayout = "2.0.0-beta4"
    val material = "1.2.0-alpha01"

    val javax_annotation = "1.0"
    val javax_inject= "1"

    val lifecycle = "2.0.0"
    val hugo = "1.2.1"
    val cardview = "1.0.0'"
    val recyclerView = "1.0.0"
    val appCompact = "1.1.0"
    val retrolambdaClasspath = "3.7.1"
    val rxbinding = "2.0.0"

    val glide = "4.11.0"
    val glideCompiler = "4.11.0"

    val zxing = "3.6.0"

    val permissionDispatcher = "4.6.0"
    val permissionDispatcherCompiler = "4.6.0"
    val androidSegment = "3.1.9"
    val googleMap = "17.0.1"
    val googleMapUtil = "2.2.4"
    val googlePlayCore = "1.6.4"
    val googleLibPlaces = "2.2.0"
    val googleServiceClasspath = "4.3.5"
    val preference = "1.1.1"
}