# 03-Github Actions, AWS CodeDeploy를 통한 CI/CD 파이프라인 자동화

> 2021-07-23 최준규

## 개요

백엔드를 개발하면서 스프링 프레임워크와는 별개로 서비스 배포에 대해 공부하면서 DevOps 관련 자료들을 많이 찾아보게 되었다. 

평소에 DevOps에 대해 알아보면서 CI/CD의 이론적인 느낌들만 알고 있었는데, 이번 프로젝트를 기회 삼아 직접 구현해보고자 했다.

배포 파이프라인은 아래와 같이 구상하였다.

| (1)           | (2)CI                                               | (3)CD                                        |
| :------------ | :-------------------------------------------------- | :------------------------------------------- |
| commit & push | Build gradle proj & dockerize and push to DockerHub | Pull docker images from hub & docker-compose |

## Continuous Integration

### Github Actions

CI 관련 툴은 다양하지만 Github Actions를 사용하기로 했다. 그 이유는 젠킨스와 같은 설치형 CI 툴과는 다르게 추가적인 절차 없이 내가 관리하고 있는 GitHub 리포지토리 에서 바로 사용할 수 있는 장점이 있기 때문이다.

Github Actions의 워크플로우는 기업과 커뮤니티에서 제공하는 다양한 템플릿들을 사용하거나 자신의 입맛에 맞게 수정하여 사용이 가능하며 이는 리포지토리 루트의 `.github/workflows` 디렉토리에서 관리된다.

또한 CI/CD에서 사용되는 `ID, PW, SECRET_KEY` 와 같은 credentials를 모두 깃허브 리포지토리 Secrets 탭에서 간단히 암호화 하여 관리가 가능하다.

CI 워크플로우는 아래와 같이 구성했다.

### workflow

```yml
name: Gradle 프로젝트 빌드 & 도커 빌드, 푸시 & AWS CodeDeploy 트리거 발동

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:

  RandHand-Chat-CI-CD:

    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v2

    - name: 도커 Buildx 셋업
      uses: docker/setup-buildx-action@v1

    - name: DockerHub 로그인
      uses: docker/login-action@v1
      with:
        username: ${{ secrets.DOCKER_USERNAME }}
        password: ${{ secrets.DOCKER_PASSWORD }}

    - name: Build & Push eureka-server
      uses: docker/build-push-action@v2
      with:
        context: ./backend/eureka-server
        push: true
        tags: devwithpug/eureka-server:0.1

    - name: Build & Push config-service
      uses: docker/build-push-action@v2
      with:
        context: ./backend/config-service
        push: true
        tags: devwithpug/config-service:0.1

    - name: Build & Push gateway-service
      uses: docker/build-push-action@v2
      with:
        context: ./backend/gateway-service
        push: true
        tags: devwithpug/gateway-service:0.1

    - name: Build & Push user-service
      uses: docker/build-push-action@v2
      with:
        context: ./backend/user-service
        push: true
        tags: devwithpug/user-service:0.1

    - name: Build & Push chat-service
      uses: docker/build-push-action@v2
      with:
        context: ./backend/chat-service
        push: true
        tags: devwithpug/chat-service:0.1

    - name: Build & Push gesture-service
      uses: docker/build-push-action@v2
      with:
        context: ./backend/gesture-service
        push: true
        tags: devwithpug/gesture-service:0.1

    - name: EC2 인스턴스 내부의 CodeDeploy 트리거 발동
      run: aws deploy --region ap-northeast-2 create-deployment --application-name CodeDeploy-application-randhand --deployment-config-name CodeDeployDefault.OneAtATime --deployment-group-name CodeDeploy-group-randhand --github-location repository=devwithpug/RandHand-Chat,commitId=${GITHUB_SHA}
      env:
        AWS_ACCESS_KEY_ID: ${{ secrets.AWS_ACCESS_KEY_ID }}
        AWS_SECRET_ACCESS_KEY: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
        Default_region_name: ap-northeast-2
```

조금 비효율적이지만.. 각각의 마이크로 서비스들을 빌드&푸시 한 후에 CD 트리거를 호출하도록 구성했다. 

개선한다면 변경 또는 추가 된 서비스만 빌드하도록 구성하여 시간을 단축할 수 있을 것 같다.

### Dockerfile

CI를 진행할 때 가장 먼저 Gradle 프로젝트를 빌드할 필요가 있기 때문에 서비스들의 `Dockerfile` 을 아래와 같이 구성했다.

* ex) chat-service

```Dockerfile
FROM gradle:7.0.0-jdk11-openj9 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY --from=build /home/gradle/src/build/libs/*.jar ChatService.jar
ENTRYPOINT [ "java", "-jar", "ChatService.jar" ]
```

Gradle 프로젝트를 빌드 한 후에 해당 `.jar` 파일을 도커 이미지화 하였다.

워크플로우를 구성할 때 `docker/build-push-action@v1` 버전을 사용했었는데 deprecated 경고가 뜨기도 하고 각각의 빌드마다 username과 password를 모두 선언해 주어야 했기 때문에 반복되는 코드를 줄일 필요가 있었다. 따라서 v2로 변경하고 `docker/login-action@v1` 를 함께 사용하여 코드를 간소화하였다.

## Continuous Deployment

CD 구현 방법은 AWS에서 제공하는 CodeDeploy를 사용하여 구현하였으며 이에 필요한 IAM, EC2, CodeDeploy 설정은 이동욱님의 블로그 [https://jojoldu.tistory.com/281](https://jojoldu.tistory.com/281) 를 참고했다.

### appspec.yml

프로젝트 루트에 `appspec.yml` 파일을 아래와 같이 생성하였다.

```yml
version: 0.0
os: linux
files:
  - source: /
    destination: /home/ec2-user/randhand/build

hooks:
  BeforeInstall:
    - location: .github/scripts/BeforeInstall.bash
      runas: root
  AfterInstall:
    - location: .github/scripts/AfterInstall.bash
      runas: root
```

코드를 보면 `source: /` 를 통해 깃헙 리포지토리의 파일 전체를 EC2 인스턴스의 `/home/ec2-user/randhand/build` 디렉토리에 가져오도록 되어있다.

또한 `BeforeInstall`, `AfterInstall` 과 같이 임의로 hook을 생성하였는데, 내가 원하는 일련의 과정들이 수행될 수 있도록 파이프라인을 구성할 수 있다.

`CodeDeploy` 는 배포 트리거가 작동될 때마다 변경사항을 감지하여 CD 파이프라인을 작동하게 되는데 이때 변경사항은 깃헙 커밋 ID에 의존하게 된다.

![image](https://user-images.githubusercontent.com/69145799/126743833-6c3bbcb3-7492-4165-90de-36b8cee0aca0.png)

### 문제점

CD를 구현하면서 생각보다 애로사항이 많았다. 먼저 사용하는 EC2 인스턴스는 프리 티어인 `t2.micro` 를 사용하고 있으며 MSA 서버를 구성하기에는 사양이 매우 부족하다. 실제 제공되는 메모리는 1GB 였기 때문에 스왑 메모리 구성이 필수였다. 하지만 그만큼 서버에 생기는 딜레이는 어쩔 수 없었다..

랜손챗 프로젝트에서 현재로서는 8개의 도커 컨테이너가 실행되어야 한다..^^ 이를 EC2에서 버틸 수 있을지도 궁금했고 여러 테스트를 해보았다.

![image](https://user-images.githubusercontent.com/69145799/126745766-89a5f477-49e4-4edc-94f7-34610ed14b4d.png)

먼저 `docker-compose` 를 이용하여 순차적으로 도커 컨테이너들을 실행하였는데, `t2.micro` 인스턴스가 __부하를 견디지 못하고 그대로 뻗어버리는 상황이 생겼다..(2시간 동안 복구가 안돼서 인스턴스 강제 중지 ㅜㅜ)__

### 원인 & 해결

`docker-compose` 를 사용하기 전에 수동으로 마이크로 서비스들을 실행할 때는 이러한 문제가 없었기 때문에 `docker-compose` 의 도커 컨테이너 사이사이에 딜레이를 추가해야겠다고 생각했다. 물론 그만큼 배포 파이프라인의 딜레이가 생기지만 `t2.micro` 의 컴퓨팅 파워를 문제없이 사용하기 위해서 어쩔 수 없이 차선책을 선택했다.

`docker-compose` 에서는 delay startup을 기본적으로 지원하지 않고 있다! 도커 공식 문서 [https://docs.docker.com/compose/startup-order/](https://docs.docker.com/compose/startup-order/) 에서는 이를 위해 [wait-for-it](https://github.com/vishnubob/wait-for-it), [wait-for](https://github.com/Eficode/wait-for) 과 같은 툴을 사용하라고 권장한다. 해당 기능을 구현하기 위해 구글링을 하면서 [docker-compose-wait](https://github.com/ufoscout/docker-compose-wait) 처럼 `docker-compose.yml` 내부에 선언하여 WAIT_ 명령어를 통해 다양한 방법으로 딜레이를 추가할 수 있는 커맨드라인 유틸리티도 알게되었지만 사용하지는 않았다. 

그 이유는 docker-compose-wait 을 사용하려면 모든 마이크로 서비스 `Dockerfile` 내부에 docker-compose-wait 스크립트 이미지를 추가해 주어야 했고 간단히 딜레이를 주기 위해서 다른 툴에 의존하고 싶지는 않았다. 또한 마이크로 서비스 특성 상 실행이 정상적으로 완료된 후에 초기화 작업을 하면서 리소스를 크게 잡는 부분들이 있으므로 WAIT_HOST 보다는 sleep 명령어를 통한 절댓값을 주어 딜레이를 선언하는 것이 간편하며 추후 변경도 용이하다고 생각했다.

따라서 `CodeDeploy` 를 통해 실행되는 `AfterInstall.bash` 를 다음과 같이 수정하였다.

```bash
docker pull devwithpug/eureka-server:0.1
docker pull devwithpug/config-service:0.1
docker pull devwithpug/gateway-service:0.1
docker pull devwithpug/user-service:0.1
docker pull devwithpug/chat-service:0.1
docker pull devwithpug/gesture-service:0.1

/usr/local/bin/docker-compose -f /home/ec2-user/docker-compose.yml up -d rabbitmq
/home/ec2-user/sleep.sh
/usr/local/bin/docker-compose -f /home/ec2-user/docker-compose.yml up -d config-service
/home/ec2-user/sleep.sh
/usr/local/bin/docker-compose -f /home/ec2-user/docker-compose.yml up -d eureka-server
/home/ec2-user/sleep.sh
/usr/local/bin/docker-compose -f /home/ec2-user/docker-compose.yml up -d gateway-service
/home/ec2-user/sleep.sh
/usr/local/bin/docker-compose -f /home/ec2-user/docker-compose.yml up -d zookeeper
/home/ec2-user/sleep.sh
/usr/local/bin/docker-compose -f /home/ec2-user/docker-compose.yml up -d kafka
/home/ec2-user/sleep.sh
/usr/local/bin/docker-compose -f /home/ec2-user/docker-compose.yml up -d user-service
/home/ec2-user/sleep.sh
/usr/local/bin/docker-compose -f /home/ec2-user/docker-compose.yml up -d chat-service
```

각각의 도커 컨테이너가 실행된 후에 `sleep 60` 커맨드로 60초간 딜레이를 주었다. 이후 테스트를 진행해보니 문제없이 배포가 완료되었으며 EC2 인스턴스도 정상적으로 작동하는 것을 확인할 수 있었다!

## References

* [https://github.com/docker/build-push-action](https://github.com/docker/build-push-action)
* [https://docs.github.com/en/actions/guides/publishing-docker-images](https://docs.github.com/en/actions/guides/publishing-docker-images)
* [https://jojoldu.tistory.com/281](https://jojoldu.tistory.com/281)