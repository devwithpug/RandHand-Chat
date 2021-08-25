# 04-JVM Warm Up을 통해서 스프링 클라우드의 첫 번째 요청도 동일한 속도로 제공하기

> 2021-08-03 최준규

## 개요

스프링 클라우드를 통해 MSA를 설계하면서 한 가지 문제가 있었는데 서버가 실행된 후, 혹은 서버의 요청이 없는 IDLE 상태로 1~2시간이 경과한 경우에 __첫 번째 요청 또는 새로운 요청이 발생하면 Response Time이 매우 느린 것__ 이었다. 먼저 클라우드 인스턴스와 MSA 서버 구성은 아래와 같다.

* 클라우드 인스턴스

> 🌎 __원격 서버__   
> 클라우드 서버 : AWS EC2 - 프리 티어(t2.micro)   
> 운영 체제 : Amazon Linux 2   
> CPU : 1 vCPU   
> RAM : 1 GiB   
> HDD : 30 GB   

* MSA 구성 요약

![image](https://user-images.githubusercontent.com/69145799/127898508-4e9aefeb-05ac-49d9-b632-1ba48334d403.png)

문제를 해결하기 위해 어떤 시도를 했는지, 그리고 어떤 결과를 얻었는지 정리해보았다.

## 문제점

EC2 인스턴스 위에 백엔드 서버를 올린 후 [Postman](https://www.postman.com/)을 통해 간단한 로그인 요청을 전송했을 때의 응답 결과이다.

![image](https://user-images.githubusercontent.com/69145799/127872712-79e28fe1-1df7-425e-8337-a6c6d96acaf6.png)

> ⬆️ 첫번째 요청의 응답까지 무려 38.75 초가 결렸다.. 하하하

t2.micro 인스턴스의 컴퓨팅 파워가 좋지 않아서 위와 같은 결과가 나왔지만 여러 번 테스트해본 결과 평균적으로 `20s` 정도의 응답 시간이 소요되었다. 따라서 문제를 해결하기 위해 로컬 환경에서 여러 테스트를 해보았다.

## 테스트

각각의 테스트는 로컬 환경(M1)에서 진행했으며 백그라운드 환경을 서버와 동일하게 구성하였다.

1. `chat-service` 만 분리하여 첫 번째 요청 테스트
2. MSA 환경에서 유레카 서버를 제외한 모든 서비스(gateway, chat, ...)가 재기동 된 경우 첫 번째 요청 테스트
3. MSA 환경에서 `chat-service`만 재기동 된 경우 첫 번째 요청 테스트

유레카 서버의 경우 Response Time에 큰 영향을 주지 않아서 제외하였다. 또한 __첫 번째 요청 이후에는 모든 테스트에서 평균 `80ms` 의 응답속도를 보였다.__

* `첫번째 요청의 응답 시간` -> `다음 요청의 응답 시간`

|         | chat-service 단위 테스트 | MSA (gateway, chat-service만 재기동) | MSA (chat-service만 재기동) |
| :------ | :----------------------- | :----------------------------------- | :-------------------------- |
| 1       | 503ms -> 79ms            | 1231ms -> 111ms                      | 519ms -> 93ms               |
| 2       | 508ms -> 71ms            | 1093ms -> 89ms                       | 614ms -> 101ms              |
| 3       | 523ms -> 93ms            | 1024ms -> 101ms                      | 565ms -> 85ms               |
| 4       | 497ms -> 87ms            | 1115ms -> 93ms                       | 526ms -> 90ms               |
| 5       | 564ms -> 81ms            | 1062ms -> 98ms                       | 534ms -> 88ms               |
| __avg__ | __519ms(6.49x)__         | __1105ms(13.8x)__                    | __551.6ms(6.895x)__         |

이처럼 정상적인 Response Time에 비해 6, 13배가량의 시간이 소요되었고 `gateway-service` 와 `chat-service` 각각의 환경에서 딜레이가 발생하는 것을 확인할 수 있었다.

## 원인 & 해결

### 원인

가장 먼저 __'첫 번째 요청에서만 추가적인 딜레이가 발생한다'__ 는 조건에 포커스를 맞추었다. 여러 자료들을 찾아보면서 문제의 원인에 대해 확신을 가질 수 있었다.

원인을 알기 전에 JVM 아키텍처에 대해 먼저 설명할 필요가 있다. 새로운 JVM 프로세스가 시작될 때마다 애플리케이션에 필요한 모든 클래스는 `ClassLoader`의 인스턴스에 의해 메모리에 로드된다. 이러한 Load 프로세스는 다음과 같이 3단계로 진행된다.

1. __Bootstrap Class Loading__ : Java 코드와 `java.lang.Object` 와 같은 필수 클래스를 메모리에 로드함
2. __Extension Class Loading__ : `java.ext.dirs` 경로에 있는 모든 JAR 파일을 로드함(개발자가 수동으로 JAR을 추가하는 경우)
3. __Application Class Loading__ : 애플리케이션 클래스 경로에 있는 모든 클래스를 로드함

이때 중요한 것은 __이러한 초기화 프로세스가 지연 로딩(LAZY LOADING) 방식을 기반으로 한다는 것이다.__

클래스 로딩이 완료되면 애플리케이션 프로세스 시작과 동시에 필요한 중요한 클래스가 JVM 캐시로 푸시 되어 런타임 중에 빠르게 액세스가 가능한 것이고. 이를 제외한 다른 클래스들은 애플리케이션 프로세스가 실행 중일 때 실제 요청이 발생되어야만 로드된다.(per-request basis)

따라서 스프링 프레임워크와 같이 Java 웹 애플리케이션의 첫 번째 요청의 응답이 평균 응답 시간보다 훨씬 느린 이유는 위와 같이 JVM 아키텍처가 지연 클래스 로딩과 JIT 컴파일로 이루어져 있기 때문이다.

> 응답 시간이 크리티컬한 애플리케이션을 설계할 때는 이러한 JVM 아키텍처 구조를 염두에 두어 응답에 필요한 클래스들이 무엇인지 정확히 알아야 하며 이러한 클래스들을 미리 캐시 하는 것이 중요하다!

### 해결

#### 스프링 부트에서의 Warm Up 방법

위와 같이 지연 로딩을 하는 클래스들을 미리 Warm Up 하려면 스프링 부트 애플리케이션이 정상적으로 초기화된 후에 Warm Up 을 수행하여야 한다. 스프링 부트에서는 간단히 `ApplicationListener` 의 구현체를 통해 Warm Up 프로세스를 만들 수 있다.

```java
@Component
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

    // ApplicationReadyEvent가 호출될 때 실행되는 메소드
    @Override
    public void onApplicationEvent() {
        
        // 클래스 Warm Up 수행

    }
}
```

#### Warm Up 테스트

JVM Warm Up 을 이용하여 문제가 해결될 수 있는지 확인하기 위해 `chat-service` 만 분리하여 간단히 단위 테스트를 진행해보았다.

가장 먼저 로그인을 담당하는 스프링 시큐리티의 `UserDetailsService` 메소드인 `loadUserByUsername`을 호출하여 관련된 클래스들을 미리 메모리에 캐싱하도록 하였다.

* `첫번째 요청의 응답 시간` -> `다음 요청의 응답 시간`

|         | loadByUsername Warm Up (X) | loadByUsername Warm Up (O) |
| :------ | :------------------------- | :------------------------- |
| 1       | 518ms -> 84ms              | 419ms -> 84ms              |
| 2       | 614ms -> 91ms              | 404ms -> 77ms              |
| 3       | 542ms -> 89ms              | 451ms -> 93ms              |
| 4       | 534ms -> 93ms              | 442ms -> 98ms              |
| 5       | 497ms -> 78ms              | 427ms -> 83ms              |
| __avg__ | __541ms(6.76x)__           | __428.6ms(5.35x)__         |

로그인 프로세스에서 `User` 객체를 불러오는 `loadByUsername` 메소드를 Warm Up 했을 뿐인데 유의미한 결과가 나왔다. 로그인 프로세스의 모든 클래스들을 Warm Up 하지 않았음에도 `100ms` 가량의 응답 속도를 개선할 수 있었다!

#### 효과적으로 Warm Up 하기

나는 서버의 첫 번째 응답 속도가 평균 속도와 다를 바 없는 `80~100ms` 정도의 시간을 결과로 얻고 싶었다. 그러기 위해선 실제 스프링 애플리케이션 생명주기에서 지연 로딩되는 클래스들을 모두 Warm Up 할 필요가 있었다.

하지만 Warm Up 해야 하는 클래스들을 하나하나 찾아서 임의로 호출하는 것은 생각보다 쉽지 않았고 오히려 비효율적이라고 생각했다. 스프링 아키텍처를 완전히 파악하고 있어야 함은 물론이고, 모든 스프링 빈 클래스들의 생명주기와 어떤 설계, 어떤 동작을 하는지 모두 알아야 했기 때문이다.

따라서 내부로 파고 들어가는 방법보다 외부에서 접근하는 방법을 택했다. `RestTemplate` 클래스를 통하여 직접 애플리케이션에 Http 요청을 보내는 방법이다.

이는 JVM Warm Up 과는 거리가 멀다고 생각할 수도 있지만, Warm Up은 지연 로딩 클래스를 미리 캐시에 올리는 것을 의미한다. 또한 `RestTemplate` 을 사용하는 것은 클라이언트가 실제 서버에 요청하는 상황을 가정하여 미리 애플리케이션 초기화 단계에서 해당 요청을 발생시켜 필요한 클래스들을 메모리에 캐싱 하는 것이므로 스프링과 같은 웹 애플리케이션의 Warm Up에 효과적이라고 판단했다.

#### RestTemplate Warm Up 테스트

```java
@Override
public void onApplicationEvent() {
    
    RequestUser req = RequestUser.builder().email("t@t").auth("t").picture("t").name("t").build();
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    headers.add("Accept", "application/json");
    headers.add("Accept-Encoding", "gzip, deflate, br");

    HttpEntity<RequestUser> requestUserHttpEntity = new HttpEntity<>(req, headers);

    RestTemplate rt = new RestTemplate();

    try {

        // localhost에 RestTemplate call 수행
        // 1. 유저 회원가입 요청
        // 2. 유저 조회 요청
        // 3. 유저 회원탈퇴 요청

    } catch (Exception e) {
        log.warn(e.toString());
    }
}
```

실제 클라이언트가 요청을 보내는 것과 동일하게 구성하여 HTTP 요청을 보내도록 코드를 작성하여 테스트했다.

* `첫번째 요청의 응답 시간` -> `다음 요청의 응답 시간`

|         | RestTemplate Warm Up (X) | RestTemplate Warm Up (O) |
| :------ | :----------------------- | :----------------------- |
| 1       | 518ms -> 84ms            | 103ms -> 111ms           |
| 2       | 614ms -> 91ms            | 97ms -> 86ms             |
| 3       | 542ms -> 89ms            | 89ms -> 83ms             |
| 4       | 534ms -> 93ms            | 95ms -> 101ms            |
| 5       | 497ms -> 78ms            | 81ms -> 93ms             |
| __avg__ | __541ms(6.76x)__         | __93ms(1.16x)__          |

기대했던 결과가 나왔다! 첫번째 요청과 그 이후 요청들의 응답시간을 동일하게 얻을 수 있었다!!!

#### `gateway-service` Warm Up 수행

`chat-service` 단위 테스트에서 원하던 결과를 얻었기 때문에 `gateway-service` 에서도 이와 같이 Warm Up을 수행하였다.

하지만 `gateway-service` 애플리케이션 내부에서 Warm Up을 수행하는 것보다 `chat-service`에서 `gateway-service`로 HTTP 요청을 보내도록 하는 것이 옳다고 생각했으며 그 이유는 다음과 같다.

1. 서버에 배포할 때 `chat-service` 보다 먼저 `gateway-service` 가 실행되기 때문에 `gateway-service` 에서 Warm Up을 수행하면 실제 로드밸런서를 통해 `chat-service`로 요청이 가도록 하는 `spring-cloud-gateway` 클래스들의 Warm Up을 완벽하게 수행할 수 없다.

2. `gateway-service` 는 로드 밸런싱의 역할만 하기 때문에 독자적으로 수행되는 프로세스 & 로직 보다 `eureka-server`, `다른 MSA 서비스` 들에 의존하는 로직이 많다.

따라서 아래와 같이 HTTP 요청을 보내도록 하였다.

```java
/* onApplicationEvent 내부 */

RestTemplate rt = new RestTemplate();

ResponseEntity<String> response = rt.getForEntity(
        // Docker 네트워크의 내부 컨테이너로 접근
        "http://gateway-service:8000/chat-service/" + URI,
        String.class
        );
```

#### Spring Scheduler 등록

서버 실행 후 첫 번째 요청에 대해서는 원하는 결과를 얻을 수 있었다. 하지만 1시간 정도의 IDLE 상태 이후에 새로운 요청을 전송할 때 이전과 같이 지연 로딩으로 인한 응답 시간에 딜레이가 생기는 현상을 확인할 수 있었다.

따라서 24시간 실행되는 서버에 대해 스케줄러를 등록하여 설정된 시간마다 Warm Up을 수행하도록 하였다.

* `ChatServiceApplication.java`

```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling // 스케줄러 활성화
public class ChatServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatServiceApplication.class, args);
    }

}
```

* `WarmUpService.java`

```java
@Component
public class WarmUpService {

    // 스케줄러 등록
    @Scheduled(fixedDelay = 30 * 60 * 1000)
    public void onApplicationEvent() {

        // Warm Up code

    }
}
```

### 결과

서버 첫 실행 또는 IDLE 상태 이후에 발생되는 요청의 딜레이를 완벽히 없앨 수 있었다!

HTTP 요청을 통해 Warm Up 하는 것이 최선의 선택은 아닐 수 있지만 원하는 결과를 얻었고 웹 애플리케이션에 적용하기에는 좋은 방법인 것 같다.

구현된 코드는 [WarmUpScheduler.java](https://github.com/devwithpug/RandHand-Chat/blob/main/backend/chat-service/src/main/java/io/kgu/chatservice/scheduler/WarmUpScheduler.java) 에서 확인하실 수 있습니다.

## References

* [https://stackoverflow.com/questions/59242577/why-my-springboot-with-embbeded-tomcat-too-slow-when-process-first-request](https://stackoverflow.com/questions/59242577/why-my-springboot-with-embbeded-tomcat-too-slow-when-process-first-request)
* [https://stackoverflow.com/questions/57312745/slow-first-call-after-restarting-spring-boot-application](https://stackoverflow.com/questions/57312745/slow-first-call-after-restarting-spring-boot-application)
* [https://www.baeldung.com/java-jvm-warmup](https://www.baeldung.com/java-jvm-warmup)