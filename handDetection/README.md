# handDetection & matching Algorithm

> 2021-09-06 한동현  

## 개요  

mediapipe를 이용한 손을 인식하고, 인식한 손에 대한 landmark를 통해 매칭 해주는 알고리즘에 대해 소개한다.  

### 구성도  

<img src="img/1.png">  

간단하게 정리하자면 mediapipe를 통해 손의 Landmark 좌표를 추출하고 벡터로 변환한 다음 각 Landmark의 각도를 구해서 비지도 학습으로 clustering 하는 알고리즘이다.  
(PCA 알고리즘은 보기 쉽게 하기 위해 사용했고 실제로 값을 계산할때는 사용하지 않는다.)  

## hand Detection  

먼저 mediaPipe를 사용해서 손의 landmark를 detection 하는 작업을 거친다.  
기본적인 mediaPipe의 setting은 다음과 같다.  

``` python
# mp setting
        mp_drawing = mp.solutions.drawing_utils
        mp_hands = mp.solutions.hands
        hands = mp_hands.Hands(
            static_image_mode=True,
            max_num_hands=2,
            min_detection_confidence=0.5)
```