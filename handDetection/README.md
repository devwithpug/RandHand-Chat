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

이미지는 `opencv`를 통해 읽어들이고, 서버에서 base64형태의 값이 전송 될 경우 decode 한다.  
`max_num_hands`=2 이므로 2개의 손을 인식이 가능하다. (수정 예정)  
hands.process()에 이미지를 넣으면 그에 따른 `landmark`가 생성되게 된다.  

``` python
# result save
if result.multi_hand_landmarks is not None:
    for res in result.multi_hand_landmarks:
        joint = np.zeros((21, 3))
        for j, lm in enumerate(res.landmark):
            joint[j] = [lm.x, lm.y, lm.z]

        # Compute angles between joints
        v1 = joint[[0,1,2,3,0,5,6,7,0, 9,10,11, 0,13,14,15, 0,17,18,19],:] # Parent joint
        v2 = joint[[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20],:] # Child joint
        v = v2 - v1 # [20,3]
```  

위의 코드를 통해서 각 `landmark`를 추출하고 `joint`라는 list에 저장한다.  
`joint`에는 x, y, z 좌표가 기록되어있는데 구해야 할 벡터들은 parent joint에 child joint를 빼주어 생성한다.  
각 Landmark에 대한 위치는 아래의 그림에 나타내었다.  

<img src = "img/2.png">  

우리가 구하고자 하는 각도는 0에서부터 뻗어 나가는 각 손가락 끝 점까지의 각도이므로 벡터 사이의 내적에 대한 `arccos` 값을 구해서 각도에 대한 절대 값을 구한다. 여기서 손가락은 항상 안쪽으로 구부리므로 벡터의 방향을 생각해서 절대값 그대로의 각도를 사용한다.  

``` python
# Get angle using arcos of dot product
angle = np.arccos(np.einsum('nt,nt->n',
    v[[0,5, 9,13],:], 
    v[[3,9,13,17],:]))
angle2 = np.arccos(np.einsum('nt,nt->n',
    -v[[1,2,4,5,6,8, 9,10,12,13,14,16,17,18],:], 
    v[[2,3,5,6,7,9,10,11,13,14,15,17,18,19],:]))

angle = np.concatenate((angle, angle2),axis=0)
```

`angle`과 `angle2`를 따로 구하고 concatenate를 하는 이유는 벡터의 방향이 다르기 때문에 각도를 구하는 공식을 다르게 하였다.  
여기서 중요한 점은 엄지에 대한 각도 계산이 부정확해서 clustering 알고리즘이 잘 작동되지 않았다.  
따라서 우리는 엄지를 굽혔을 때와 굽히지 않았을 때를 나눠서 계산했다.  
엄지을 굽혔을 때와 굽히지 않았을 때에 대한 `0->1` 벡터와 `3->4` 벡터 사이의 각도가 서로 다르게 나왔다. (자세한 내용은 matching algorithm에서 설명)  


## Matching Algorithm  

매칭 알고리즘의 경우 위의 설명과 같이 `0->1` 벡터와 `3->4` 벡터 사이의 각도 차이를 이용해서 2개의 다른 데이터 군집을 설정했다.  
(각 각 각도가 90도 이상인지 아닌지로 판별했다.)  
