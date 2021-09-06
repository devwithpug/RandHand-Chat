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
총 38장의 데이터에 대해서 비굫 보았을 때 결과는 다음과 같다.    

<img src="img/3.png">  

이 방법은 엄지를 핀 것과 굽힌 것에 대해 정확히 구분해 내었고 더 군집으로 묶기가 용이해 졌다.  
그 후 군집으로 묶을 방법을 생각해보았다.  
매우 짧은 시간마다 자주 실행하기 때문에 최대한 간단하고 정확도 높은 알고리즘을 생각해 내야 했다. 우리가 처음에 생각한 방법은 총 3가지 였다.  

    1. Kmeans
    2. DBSCAN
    3. 유클리드 거리 기반 clustering Algorithm  

이 3가지 방법들은 쉽게 구현이 가능하다는 장점이 있었으나 Kmeans의 경우 군집의 수를 지정해줄 때 outlier의 처리가 애매해 질 수 있었고, DBSCAN의 경우 하이퍼 파라미터의 값에 따라서 큰 차이를 보이기 때문에 사용하기에 어려움이 있었다.  
따라서 매칭 시스템을 위한 유클리드 기반의 알고리즘을 생각해 보았다.  

### 알고리즘의 과정  
    * 각 각도를 점 좌표의 값이라고 생각했을 떄 총 17차원의 좌표계를 생각할 수 있었고 점과 점사이의 거리를 구하는 공식을 사용해 점간의 겨리를 구한다.  
    * 점과 점사이의 거리가 가까운 순으로 매칭을 잡아준다. 단 그렇게 되면 결국 매우 먼 두 점에 대해서도 매칭이 잡힐 수 있으므로 같은 손이라고 판단할수 있는 최대 거리는 1~1.5 정도라는 결론이 나게 되었다.  
    * 따라서 매칭시 1.5 이상의 거리가 나오는 점 들은 매칭 되지 않게 했고, 끝까지 매칭되지 않을 경우 outlier 처리를 했다.

이렇게 매칭된 결과를 비교해보면 다음과 같다.  

    * 굽혔을 때 않았을 때의 Accuracy: 76.47%
    * 굽히지 않았을 때의 Accracy: 100.00% 

> 38개의 data에 대한 총 Accracy: 89.47%

전체적으로 볼 때 90% 가량의 Accracy를 보여줬으나 다른 사진에 대해서 비슷한 성능을 낼 수 있을 것이라는 보장이 없다.  
따라서 앞으로 알고리즘을 개선할 필요성이 있다.  

<img src = "img/4.png">  
