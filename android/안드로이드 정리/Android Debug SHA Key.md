## 안드로이드 Debug SHA Key 추출
 ### _1. 디버그용 Key 발급 Gradle_ 

> 안드로이드 스튜디오 terminal로 명령어 적용 가능
> >./gradlew signingReport

* _안되는 경우_
> 안드로이드 스튜디오에서 start commands execution 이라는 기능을 만들어 두었다
> >`gradlew signingReport 입력 후 ctrl + enter 또는 enter`

![image](https://user-images.githubusercontent.com/63226023/132191842-62f91a08-020c-489e-98cf-8829259ce426.png)
### _2. 디버그용 Key 발급 keytool_

![image](https://user-images.githubusercontent.com/63226023/132191658-22ca0b4a-955d-4dec-b214-4c6f91214582.png)
> cmd에서 명령어 입력
> >keytool -list -v -alias androiddebugkey -keystore %USERPROFILE%\debug.keystore

* %USERPROFILE% 
  
```
window user 경로을 말한다
C드라이브 -> 사용자 -> UserName(본인 컴퓨터에서 설정한 이름)
여기 까지의 경로 입력후, debug.keystore 파일은 .android 폴더 아래에 위치해 있다
```