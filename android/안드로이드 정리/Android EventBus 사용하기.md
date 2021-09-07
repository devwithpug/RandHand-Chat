# - EventBus
<https://github.com/greenrobot/EventBus>

![image](https://user-images.githubusercontent.com/63226023/132125464-3907f755-b5b8-4681-8831-944072f626dc.png)
__이벤트 버스의 동작 방식을 간단하게 설명해 주는 그림이다. (from greenrobot github readme)__
* 그린로봇이 만든 라이브러리이다.
* 이벤트 버스는 Activity, Fragment 및 백그라운드 Thread에서 잘 수행된다.
* 복잡하고 오류가 발생하기 쉬운 종속성 및 수명 주기 문제 방지

## 사용이유
```
메시지 채팅에서 상대방의 메시지 db에 내가 보낸 메시지 정보를 보여줘야하는 부분이 생겼다.
여기서 EventBus를 사용하여 subscriber에게 MessageTable 즉 메시지정보를 보내주어 문제를 해결하였다.
```
## 사용법
* 클래스 단위로 이벤트를 던질 수 있다.
* 이벤트를 수신 받기 원하는 곳에 eventbus에 register를 해두면 이벤트 수신이 가능.
* 이벤트를 더 이상 받지 않으면 unregister를 추가해 주면 된다.

### 1. `build.gradle(app)`에서 `dependencies` 추가
```groovy
implementation 'org.greenrobot:eventbus:3.2.0'
```

### 2. 이벤트를 정의
```kotlin
data class MessageTable { /* 필요한 경우 추가 필드 */ }
```
### 3. 이벤트 버스 등록 및 취소
쓰고 싶은 클래스의 생명주기에 맞추어 `EventBus.getDefault().register(this)` 를 해준다

```kotlin
override fun onResume() { 
    super.onResume() 
    if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this) 
} 

override fun onPause() { 
    super.onPause() 
    if (EventBus.getDefault().isRegistered(this)) 
        EventBus.getDefault().unregister(this) 
}
```
`(this)`는 `subscribe` 즉 구독자로 구독을 받을위치를 지정해 준다. (ex. 이벤트 버스를 생성하는 Activity나 Fragment)

### 4. `subscriber` 준비(사용 예시)
```kotlin
@Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(message: MessageTable) {
        chatBinding.messageList.scrollToPosition(messageAdapter.itemCount)
        messageAdapter.addMessage(message)
        messageDAO.insertMessage(message)
        chatDAO.updatePrefMessage(message.context, sessionId)
    }
```
이 메소드를 통해 message를 메시지 리스트, db에 넣어준다.

* `@Subscribe(threadMode = ThreadMode.Main)`
    > 메인 쓰레드에서 이벤트를 처리한다는 것을 의미한다.
    > 
    > 다른 모드를 사용할 경우 백그라운드에서 동작하게 만들 수 있다.

### 5. 이벤트 전송
```kotlin
EventBus.getDefault().post(
    createMessageTable(
        sessionId,
        null,
        fromUser,
        "IMAGE",
        url,
        now
    )
)
```
`.post(/* 보낼 이벤트 -> 프로젝트예시: message : MessageTable)` 를 통하여 이벤트 전송.
## References
* https://github.com/greenrobot/EventBus
* https://no-dev-nk.tistory.com/19?category=876493
