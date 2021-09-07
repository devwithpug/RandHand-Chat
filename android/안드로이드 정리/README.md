# Android with Kotlin

## _1. 사용 라이브러리_

* Retrofit

```groovy
def retrofit_version = '2.9.0' 
implementation "com.squareup.retrofit2:retrofit:$retrofit_version"
implementation "com.squareup.retrofit2:converter-gson:$retrofit_version"
```

* Glide & okhttp

```groovy
implementation 'com.github.bumptech.glide:glide:4.12.0'
annotationProcessor 'com.github.bumptech.glide:compiler:4.12.0'
```

* Room

```groovy
def room_version = "2.2.6"

implementation "androidx.room:room-runtime:$room_version"
kapt "androidx.room:room-compiler:$room_version"
implementation "androidx.room:room-ktx:$room_version"
```

* Mediapipe

```groovy
// MediaPipe hands solution API and solution-core.
implementation 'com.google.mediapipe:solution-core:latest.release'
implementation 'com.google.mediapipe:hands:latest.release'
// MediaPipe deps
implementation 'com.google.flogger:flogger:latest.release'
implementation 'com.google.flogger:flogger-system-backend:latest.release'
implementation 'com.google.guava:guava:27.0.1-android'
implementation 'com.google.protobuf:protobuf-java:3.11.4'
```

* CameraX

```groovy
// CameraX core library
def camerax_version = "1.0.1"

implementation "androidx.camera:camera-core:$camerax_version"
implementation "androidx.camera:camera-camera2:$camerax_version"
implementation "androidx.camera:camera-lifecycle:$camerax_version"
```

* Event Bus

```groovy
implementation 'org.greenrobot:eventbus:3.2.0'
```

## _2. 프로그램 Package 구조_

| Package     | Detail                                            |
| ----------- | :------------------------------------------------ |
| `Adapter`   | 리사이클러뷰 adapter를 위한 패키지                |
| `Database`  | room database 및 db domain 위한 패키지            |
| `Domain`    | 서버 통신시 domain 위한 패키지                    |
| `Fragments` | 메인 페이지에서 각 fragment를 위한 패키지         |
| `MediaPipe` | Mediapipe 사용 및 인식된 손 preview를 위한 패키지 |
| `Retrofit`  | 서버와 통신을 위한 패키지                         |
| `Util`      | 앱에 전적으로 사용을 위한 패키지                  |