# 👋 랜손챗 : RandHand-Chat 👋

<img src = "https://user-images.githubusercontent.com/63226023/132185973-58a4ea78-a256-45b9-89ee-06c40cd53b3a.gif">

## 개요

손 모양을 통해 임의로 만든 수신호(Hand gesture)를 인식하여 동일한 수신호를 입력한 상대방과 랜덤으로 매칭해주는 채팅 애플리케이션입니다. [Mediapipe - Hands](https://google.github.io/mediapipe/solutions/hands)를 이용하여 개발했습니다.

## 팀원

| 팀원                                          | 역할                                     |
| :-------------------------------------------- | :--------------------------------------- |
| 😆 [최준규(PM)](https://github.com/devwithpug) | Backend (Spring frameworks, AWS, CI/CD)  |
| 😎 [박준후](https://github.com/ppeper)         | 안드로이드 애플리케이션 with Kotlin      |
| 🤢 [한동현](https://github.com/DongHyun99)     | Object Detection (Mediapipe, Tensorflow) |

## 애플리케이션 구성

### 01. Welcome, 로그인, 설정 / 프로필 및 변경

<div style="float:left;margin:0 10px 10px 0" markdown="1">
<img src = "https://user-images.githubusercontent.com/63226023/132186204-6bb481e3-c429-4853-b8fc-161334ac04ee.png" width="15%" height="15%">
<img src = "https://user-images.githubusercontent.com/63226023/132176392-cda026ba-3953-4080-9cb3-309daffe3724.png" width="15%" height="15%">
<img src = "https://user-images.githubusercontent.com/63226023/132186299-37d1ec92-13ec-4243-849b-01d2e559e300.png" width="15%" height="15%">
<img src = "https://user-images.githubusercontent.com/63226023/132186811-4826f896-920d-4cca-8273-b0229a883082.png" width="15%" height="15%">
<img src = "https://user-images.githubusercontent.com/63226023/132186817-9af91391-7bd8-4b74-8856-2f8f75fe3b9e.png" width="15%" height="15%">
</div>

### 02. 채팅방, 채팅화면

<div style="float:left;margin:0 10px 10px 0" markdown="1">
<img src = "https://user-images.githubusercontent.com/63226023/132188667-edfb6b59-e191-404f-b15b-c2f203653436.png" width="15%" height="15%">
<img src = "https://user-images.githubusercontent.com/63226023/132188678-95811e6e-e61c-4297-9617-7c8ebba9352d.png" width="15%" height="15%">
</div>

### 03. 랜손채팅, 매칭, 채팅방, 채팅화면
<div style="float:left;margin:0 10px 10px 0" markdown="1">
<img src = "https://user-images.githubusercontent.com/63226023/132188919-15ddf4f5-e09c-40ea-9a62-4ba8e26525ae.jpg" width="15%" height="15%">
<img src = "https://user-images.githubusercontent.com/63226023/132189489-3c122182-c90f-4733-b0b3-b292b712f6f7.gif" width="15.8%" height="15.8%">
</div>

> 시연 영상: [https://youtu.be/46Wa7FWnS6k](https://youtu.be/46Wa7FWnS6k)

## 기술 스택

![stack](https://user-images.githubusercontent.com/69145799/132272891-444f8c9a-9fd6-472b-9d92-b5980741aa0e.png)

> Spring Cloud 프레임워크 및 AMQP 생략(Spring Cloud Eureka, Spring Cloud Config, Kafka)   
> 스프링 프로젝트의 `application.properties` 설정 파일 외부 관리 : [RandHand-config](https://github.com/devwithpug/RandHand-config)

## 개발 일지

### Android

* [안드로이드 사용 라이브러리 및 패키지 구조](./android/안드로이드%20정리/README.md)
* [안드로이드 Debug SHA Key 추출하기](./android/안드로이드%20정리/Android%20Debug%20SHA%20Key.md)
* [안드로이드 EventBus로 메시지 이벤트 관리](./android/안드로이드%20정리/Android%20EventBus%20사용하기.md)
* [안드로이드 프로필, 사진 보내기 위한 Glide 라이브러리 사용](./android/안드로이드%20정리/Android%20Glide%20사용하기.md)
* [안드로이드 백앤드 서버와 통신을 위한 Retrofit 사용](./android/안드로이드%20정리/Android%20Retrofit%20사용하기.md)

### Object Detection

* [handDetection & matching Algorithm](./handDetection/README.md)

### Backend

* [2021.08.03 - 04 JVM Warm Up을 통해서 스프링 클라우드의 첫 번째 요청도 동일한 속도로 제공하기](./backend/개발일지/04-JVM%20Warm%20Up을%20통해서%20스프링%20클라우드의%20첫%20번째%20요청도%20동일한%20속도로%20제공하기.md)

* [2021.07.23 - 03 Github Actions, AWS CodeDeploy를 통한 CI/CD 파이프라인 자동화](./backend/개발일지/03-Github%20Actions,%20AWS%20CodeDeploy를%20통한%20CICD%20파이프라인%20자동화.md)

* [2021.07.17 - 02 RabbitMQ, Spring Cloud Bus를 통한 application.yml 외부 관리, 실시간 동기화 구현](./backend/개발일지/02-RabbitMQ,%20Spring%20Cloud%20Bus를%20통한%20application.yml%20외부%20관리,%20실시간%20동기화%20구현.md)

* [2021.07.12 - 01 백엔드 서버 인증 방식으로 JWT를 도입함](./backend/개발일지/01-백엔드%20서버%20인증%20방식으로%20JWT를%20도입함.md)

## 커밋 메시지 규칙 

1. 문장의 끝에 `.` 를 붙이지 말기

2. 이슈 번호를 커밋 메시지 끝에 붙이기

3. 형식

   > [타입]: [내용] [이슈 번호]

4. 예시

   > docs: OO메소드 관련 설명 주석 [#3]
   >
   > feature: 예약 시스템의 add() [#6]

5. 타입 종류

   > \- chore : 간단한 수정
   >
   > \- feature : 새로운 기능
   >
   > \- fix : 버그 대처
   >
   > \- refactor : 코드 수정 / 리팩터링
   >
   > \- test : 테스트 추가
   >
   > \- docs : 문서 작성