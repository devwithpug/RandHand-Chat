# - Glide
https://bumptech.github.io/glide/

![image](https://user-images.githubusercontent.com/63226023/132191940-6893d6dc-7dba-45b0-a816-c5200f30092d.png)

* Google에서 공개한 이미지 로딩 라이브러리이다.
* 성능이 가장 좋다고 알려져있다.
* 사진, 동영상, gif 파일 로딩도 가능하다.

## 사용법

### 1. `build.gradle(app)`에서 `dependencies` 추가
```groovy
implementation 'com.github.bumptech.glide:glide:4.12.0'
annotationProcessor 'com.github.bumptech.glide:compiler:4.12.0'
```

### 2. API 기본
```
Glide.with(fragment)
    .load(url)
    .into(imageView)
```
* with() : Context 외에도 다른 객체사용 (ex(View, Activity, Fragment, FragmentActivity
* load() : Url, Uri등 이미지를 받아올 경로
* into() : 받아온 이미지를 보여줄 공간을 지정

### 3. 그 외 함수
* override(width,height) : 이미지 크기 지정
* placeholder() : 이미지가 로딩하는 동안 보여줄 이미지 설정
* error() : 이미지 로딩 실패시 보여줄 이미지 설정
* thumbnail() : 원본 이미지를 썸네일로 사용. 0.1f면 실제 비율의 10%만 먼저 가져와서 흐릿하게 보여준다


## References
* https://github.com/bumptech/glide
* https://leveloper.tistory.com/162
* https://bumptech.github.io/glide/