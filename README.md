# π λμμ± : RandHand-Chat π

## κ³ΌνκΈ°μ μ λ³΄ν΅μ λΆ μ£Όμ΅ / 2021 κ³΅κ°SW κ°λ°μλν

### [λμμ± ν(OSS-24-027) μμ μμ](https://www.oss.kr/dev_competition_notice/show/eafbc679-e341-4d07-9dd3-8e28e5443358)

> μμ° μμ: [https://youtu.be/epUv5T32eJ8](https://youtu.be/epUv5T32eJ8)   
> λ°ν μλ£: [κ³΅κ°SW κ°λ°μλν μ΅μ’ λ°ν (λμμ±)](./λ°ν%20μλ£.pdf)

<img src = "https://user-images.githubusercontent.com/63226023/138600419-24779e81-9854-406b-ab8f-1980c9f15fac.gif">

## κ°μ

μ λͺ¨μμ ν΅ν΄ μμλ‘ λ§λ  μμ νΈ(Hand gesture)λ₯Ό μΈμνμ¬ λμΌν μμ νΈλ₯Ό μλ ₯ν μλλ°©κ³Ό λλ€μΌλ‘ λ§€μΉ­ν΄μ£Όλ μ±ν μ νλ¦¬μΌμ΄μμλλ€. [Mediapipe - Hands](https://google.github.io/mediapipe/solutions/hands)λ₯Ό μ΄μ©νμ¬ κ°λ°νμ΅λλ€.

## λΌμ΄μΌμ€

* Android App - `Apache License 2.0` ([android/LICENSE](./android/LICENSE))
* Backend Server - `The MIT License` ([backend/LICENSE](./backend/LICENSE))
* Object Detection - `The MIT License` ([handDetection/LICENSE](./handDetection/LICENSE))

## νμ

| νμ                                          | μ­ν                                      |
| :-------------------------------------------- | :--------------------------------------- |
| π [μ΅μ€κ·(PM)](https://github.com/devwithpug) | Backend (Spring frameworks, AWS, CI/CD)  |
| π [λ°μ€ν](https://github.com/ppeper)         | μλλ‘μ΄λ μ νλ¦¬μΌμ΄μ with Kotlin      |
| π€’ [νλν](https://github.com/DongHyun99)     | Object Detection (Mediapipe, Tensorflow) |

## μ νλ¦¬μΌμ΄μ κ΅¬μ±

### 01. Welcome, λ‘κ·ΈμΈ, μ€μ  / νλ‘ν λ° λ³κ²½


<div style="float:left;margin:0 10px 10px 0" markdown="1">
<img src = "https://user-images.githubusercontent.com/63226023/138600469-7d45d40c-3a10-410c-934d-40882c4ae7f8.png" width="15%" height="15%">
<img src = "https://user-images.githubusercontent.com/63226023/138600472-883a25ff-95ef-44cd-92b7-b1db7a892c76.png" width="15%" height="15%">
<img src = "https://user-images.githubusercontent.com/63226023/138600479-0bb44c1b-aebb-4058-a079-00fbb2d07f99.png" width="15%" height="15%">
<img src = "https://user-images.githubusercontent.com/63226023/138600485-fa6f34db-f9bb-4cc4-9a15-3a4f83918993.png" width="15%" height="15%">
<img src = "https://user-images.githubusercontent.com/63226023/138600495-bbc052d6-1336-4248-a36d-12115c112218.png" width="15%" height="15%">
</div>

### 02. μ±νλ°©, μ±ννλ©΄

<div style="float:left;margin:0 10px 10px 0" markdown="1">
<img src = "https://user-images.githubusercontent.com/63226023/138601544-87db375b-2b05-4c72-a63f-5ae4317c0b1c.png" width="15%" height="15%">
<img src = "https://user-images.githubusercontent.com/63226023/138601546-a324f71f-338b-4b9a-9dfe-233c47cec147.png" width="15%" height="15%">
</div>

### 03. λμμ±ν, λ§€μΉ­
<div style="float:left;margin:0 10px 10px 0" markdown="1">
<img src = "https://user-images.githubusercontent.com/63226023/138600547-b62a8ecf-d5e1-479b-a767-ab537cb77991.jpg" width="15%" height="15%">
<img src = "https://user-images.githubusercontent.com/63226023/138600584-4fc54160-bace-42de-8086-32ac5efc1c12.gif" width="15%" height="15%">
</div>

## κΈ°μ  μ€ν

![backend-1](https://user-images.githubusercontent.com/69145799/141271929-b9b246d8-8313-4647-99d4-85c8c2457f79.png)

![backend-2](https://user-images.githubusercontent.com/69145799/141271953-553adcf9-bb25-4445-8f73-f3e93246c42f.png)

> μ€νλ§ νλ‘μ νΈμ `application.properties` μ€μ  νμΌ μΈλΆ κ΄λ¦¬ : [RandHand-config](https://github.com/devwithpug/RandHand-config)

## κ°λ° μΌμ§

### Android

* [μλλ‘μ΄λ μ¬μ© λΌμ΄λΈλ¬λ¦¬ λ° ν¨ν€μ§ κ΅¬μ‘°](./android/μλλ‘μ΄λ%20μ λ¦¬/README.md)
* [μλλ‘μ΄λ Debug SHA Key μΆμΆνκΈ°](./android/μλλ‘μ΄λ%20μ λ¦¬/Android%20Debug%20SHA%20Key.md)
* [μλλ‘μ΄λ EventBusλ‘ λ©μμ§ μ΄λ²€νΈ κ΄λ¦¬](./android/μλλ‘μ΄λ%20μ λ¦¬/Android%20EventBus%20μ¬μ©νκΈ°.md)
* [μλλ‘μ΄λ νλ‘ν, μ¬μ§ λ³΄λ΄κΈ° μν Glide λΌμ΄λΈλ¬λ¦¬ μ¬μ©](./android/μλλ‘μ΄λ%20μ λ¦¬/Android%20Glide%20μ¬μ©νκΈ°.md)
* [μλλ‘μ΄λ λ°±μ€λ μλ²μ ν΅μ μ μν Retrofit μ¬μ©](./android/μλλ‘μ΄λ%20μ λ¦¬/Android%20Retrofit%20μ¬μ©νκΈ°.md)

### Object Detection

* [handDetection & matching Algorithm](./handDetection/README.md)

### Backend

* [2021.08.03 - 04 JVM Warm Upμ ν΅ν΄μ μ€νλ§ ν΄λΌμ°λμ μ²« λ²μ§Έ μμ²­λ λμΌν μλλ‘ μ κ³΅νκΈ°](./backend/κ°λ°μΌμ§/04-JVM%20Warm%20Upμ%20ν΅ν΄μ%20μ€νλ§%20ν΄λΌμ°λμ%20μ²«%20λ²μ§Έ%20μμ²­λ%20λμΌν%20μλλ‘%20μ κ³΅νκΈ°.md)

* [2021.07.23 - 03 Github Actions, AWS CodeDeployλ₯Ό ν΅ν CI/CD νμ΄νλΌμΈ μλν](./backend/κ°λ°μΌμ§/03-Github%20Actions,%20AWS%20CodeDeployλ₯Ό%20ν΅ν%20CICD%20νμ΄νλΌμΈ%20μλν.md)

* [2021.07.17 - 02 RabbitMQ, Spring Cloud Busλ₯Ό ν΅ν application.yml μΈλΆ κ΄λ¦¬, μ€μκ° λκΈ°ν κ΅¬ν](./backend/κ°λ°μΌμ§/02-RabbitMQ,%20Spring%20Cloud%20Busλ₯Ό%20ν΅ν%20application.yml%20μΈλΆ%20κ΄λ¦¬,%20μ€μκ°%20λκΈ°ν%20κ΅¬ν.md)

* [2021.07.12 - 01 λ°±μλ μλ² μΈμ¦ λ°©μμΌλ‘ JWTλ₯Ό λμν¨](./backend/κ°λ°μΌμ§/01-λ°±μλ%20μλ²%20μΈμ¦%20λ°©μμΌλ‘%20JWTλ₯Ό%20λμν¨.md)

## μ»€λ° λ©μμ§ κ·μΉ 

1. λ¬Έμ₯μ λμ `.` λ₯Ό λΆμ΄μ§ λ§κΈ°

2. μ΄μ λ²νΈλ₯Ό μ»€λ° λ©μμ§ λμ λΆμ΄κΈ°

3. νμ

   > [νμ]: [λ΄μ©] [μ΄μ λ²νΈ]

4. μμ

   > docs: OOλ©μλ κ΄λ ¨ μ€λͺ μ£Όμ [#3]
   >
   > feature: μμ½ μμ€νμ add() [#6]

5. νμ μ’λ₯

   > \- chore : κ°λ¨ν μμ 
   >
   > \- feature : μλ‘μ΄ κΈ°λ₯
   >
   > \- fix : λ²κ·Έ λμ²
   >
   > \- refactor : μ½λ μμ  / λ¦¬ν©ν°λ§
   >
   > \- test : νμ€νΈ μΆκ°
   >
   > \- docs : λ¬Έμ μμ±
