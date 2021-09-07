# - Retrofit2
<https://square.github.io/retrofit/>
  * 스퀘어 사의 오픈소스 라이브러리
  * 애플리케이션에서 범위가 조금 커지게되면 서버와의 통신은 불가피하다.

    `Retrofit` 을 사용하여 API 통신을 사용하는 서비스에서 보다 쉡게 사용할 수 있다.
  
## 사용법

### 1. `build.gradle(app)`에서 `dependencies` 추가

```groovy
// retrofit2, gson implementation
def retrofit_version = insert latest version
implementation "com.squareup.retrofit2:retrofit:$retrofit_version"
implementation "com.squareup.retrofit2:converter-gson:$retrofit_version"
```

### 2. ServiceURL 설정

* `Retrofit`에서 제공하는 Builder()를 이용하여 BaseURL을 지정하여 request할 ServiceURL을 설정해 준다.
```kotlin
// Singleton 으로 설정
object ServiceURL {
    // Base Url
    private const val BASE_URL = "http://Api 주소"
    // Retrofit 선언
    private lateinit var retrofit: Retrofit
    private var gson = GsonBuilder().setLenient().create()

    // getInstance 함수로 Retrofit 반환
    fun getInstance(): Retrofit {
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        return retrofit
    }
}
```
※ Singleton 패턴이란?
> * 어떤 클래스의 인스턴스는 오직 `하나만 생성` 하여 , 생생된 객체를 어디에서든지 참조 가능한 디자인 패턴이다.
> * Singleton의 인스턴스는 `전역적`으로 사용되기 때문에 메모리를 효율적으로 사용하고 다른 클래스의 인스턴스들이 데이터를 공유 변경이 가능하다는 장점이 있다.

코틀린에서 Singleton 생성(간단)
```kotlin
object 클래스이름
```

### 3. Interface Class 선언

* 이전에 생성한 ServiceURL에 호출할 통신 방식 생성(예시)

```kotlin
interface IRetrofitUser {
  @GET("users/{userId}")
  fun getUserInfo(
    // Header 에 넣는 userId는 지금 사용자가 누구인지
    @Header("Authorization") userToken: String,
    @Header("userId") userId: String,
    // 조회하고 싶은 userId -> 위의 {}안의 이름과 동일
    @Path("userId") findId: String
  ): Call<ResponseUser>
}
```
기본적으로 `Call<T>` 객체를 반환해준다

 ```
 @GET 은 통신 방식을 나타내는 어노테이션이다.
 @GET, @POST, @PUT, @PACH, @HEAD 의 종류가 있습니다.
{sessionId} @Path 와 매칭되는 동적인 변수역할을 합니다.
```
※예시 `userId = "kotlin123"`이면 `users/kotlin123`으로 API 호출

  - 파라미터 어노테이션 정리
  ```
  @Path - 동적으로 경로를 사용하기 위한 어노테이션
  @Query, @QueryMap - @GET 에서 사용하며 조건 파라미터를 설정
  @Field, @FieldMap - @POST 에서 사용하며 조건 파라미터를 설정
  @Body - 객체를 이용하여 조건 파라미터를 설정
  @Header - 해더 설정
  ```
  - `@Field` 사용시 `@FormUrlEncoded`와 함깨 사용
  
    - `FormUrlEncoded` 는 key=value&key=value 와 같은 형태로 데이터를 전달하는 것을 말한다.

### 4 Repo class 생성

* POJO(Plain Old java Object) 형태의 Java POJO 파일 생성(`예시: ResponseUser`)

  `JSON 으로 이루어진 API로 호출한 Response 결과 값을 너노테이션이나 getter/setter 를 사용하여 자동으로 넘기는 객체`
* Gson

  Gson은 _자바 객체_ 와 _JSON_ 간의 `직렬화` 및 `역직렬화`를 위한 오픈소스 자바 라이브러리이다.
  
  ```
  ex) @SerializedName("userId) var userId: String
  ->
  {
    "userId":"exampleId"
  }
  ```
```kotlin
data class ResponseUser(
  @SerializedName("userId")
  var userId: String?,
  @SerializedName("email")
  var email: String?,
  @SerializedName("name")
  var name: String?,
  @SerializedName("statusMessage")
  var message: String?,
  @SerializedName("picture")
  var picture: String?,
  @SerializedName("userFriends")
  var userfriends: List<ResponseUser>?,
  @SerializedName("userBlocked")
  var userblocked: List<ResponseUser>?
)
```
### 5. 사용예시
```kotlin
fun getUserInfo(supplementService: IRetrofitUser, token: String, userId: String, pathId: String) {
  // token, userId는 @Header로 들어가고 pathId는 @Path에 들어간다
    supplementService.getUserInfo(token, userId, pathId).enqueue(object: Callback<Responseuser> {
      override fun onResponse(call: Call<ResponseUser>, response: Response<ResponseUser>) {
        // 
      }

      override fun onFailure(call: Call<ResponseUser>, t: Throwable) {
          //
      }
    })
             
}
```
  위의 예시에서 Retrofit은 통신의 결과에 따라 성공시 onResponse 를 실행하고 실패 시 onFailure를 실행한다.

-> ResponseUser를 Callback 해준다.

### References
* https://square.github.io/retrofit/
* https://gaybee.tistory.com/16
* https://question0.tistory.com/12

  





