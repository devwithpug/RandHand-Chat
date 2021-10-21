## - 새로운 API ActivityResultContract로 Migration

### 1. Deprecate 이전 방식
  ```
 - StartActivityForResult() 메소드와 onActivityResult가 Deprecated 되었다
  기존의 방식은 StartActivityForResult로 결과를 가져올 Activity를 실행하고, 어떤 액티비티를 실행했는지에 상관없이 reSultCode를 설정하여
  onActivityResult Callback에서 결과를 처리를 했다
  ```
### 2. Activity Result에 Callback 등록
- `registerForActivityResult()` API를 통해 결과 콜백을 등록할 수 있다.
- `registerForActivityResult()`는 `ActivityResultContract`  _계약_ 을 통해 `ActivityResultCallback`을 가져와서 다른 활동을 실행하는 데 사용할 `ActivityResultLauncher`를 반환 한다.
- `ActivityResultContract`는 입출력 유형을 정의하고 API에는 사진 촬영, 권한 요청 등 기본 계약을 제공한다.
- 사용자에 따라서 `자체 맞춤 계약`을 만들수도 있다.

### 3. 사용법
- ActivityResultLauncher에 제공되면 기본 Contracts(계약) 
- 필요시 커스텀 Contracts(자체 맞춤 계약)을 사용할 수 있다.

<img src = "https://user-images.githubusercontent.com/63226023/136908519-71d69938-51f7-471e-bd4b-c600ab5d8349.png" width="80%" height="80%">

- _미리 만든 "계약서"_
1. 맞춤 계약 
  - 각 `ActivityResultContract`는 입력이 필요하지 않은 경우 입력 유형으로 `Void를 사용하여(Kotlin에서는 Void? 또는 Unit 사용)` 입력 및 출력 클래스를 정의해야 한다. 
  - 각 계약은 `createIntent()` 메서드를 구현해야 합니다. 이 메서드는`Context`와 입력을 가져와 `startActivityForResult()`와 함께 사용할 Intent를 구성한다. 
  - 각 계약은 주어진 `resultCode(예: Activity.RESULT_OK 또는 Activity.RESULT_CANCELED)`와 `Intent`에서 출력을 생성하는 `parseResult()`도 구현해야 한다. 
  - `createIntent()`를 호출하고 다른 활동을 시작하며 `parseResult()`를 사용하여 결과를 빌드할 필요 없이 주어진 입력의 결과를 확인할 수 있는 경우 계약은 `getSynchronousResult()`를 구현할지 선택할 수 있다.

```kotlin
class 맞춤계약 : ActivityResultContract<Input, Output>() {
    override fun createIntent(context: Context, name: Input) = 
        // Create Intent
        Intent(~) {
            
        }

    override fun parseResult(resultCode: Int, result: Intent?) : Output? {
        if (resultCode != Activity.RESULT_OK) {
            return null
        }
        return // Do something
    }
}
```
2. 맞춤 계약이 필요하지 않다면 `StartActivityForResult` 계약을 사용. 이 계약은 일반 계약으로, Intent를 입력으로 가져와서 `ActivityResult`를 반환하므로 다음 예와 같이 resultCode와 Intent를 콜백의 일부로 추출할 수 있다.
```kotlin
val startForResult = registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
    if (result.resultCode == Activity.RESULT_OK) {
        val intent = result.data
        // Handle the Intent
    }
}
override fun onCreate(savedInstanceState: Bundle) {
    // ...
    binding.startButton.setOnClickListener {
        // passing it the Intent you want to start
        startForResult.launch(Intent(this, ExampleActivity::class.java))
    }
}
```
### 4. 결과를 위한 활동 실행

### References
* https://developer.android.com/training/basics/intents/result




