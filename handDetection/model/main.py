import cv2
import mediapipe as mp
import pandas as pd
import glob
import base64
from PIL import Image
from io import BytesIO
import matplotlib.pyplot as plt
import numpy as np
from sklearn.decomposition import PCA

image_path = 'handDetection/model/original_images/'
IMAGE_FILES = glob.glob(image_path+'*.jpg')


mp_drawing = mp.solutions.drawing_utils
mp_hands = mp.solutions.hands
hands = mp_hands.Hands(
    max_num_hands=1,
    min_detection_confidence=0.5,
    min_tracking_confidence=0.5 )


base64_list = []
img_list = []
angle_data = pd.DataFrame(columns=['angle'+str(x) for x in range(0,15,1)])

# image base64 encode
for img_name in IMAGE_FILES:
    with open(img_name,'rb') as img:
        base64_string = base64.b64encode(img.read())
        base64_list.append(base64_string)

# image decode (서버에서 base64 형식으로 오면, 형태를 이미지화 해줌)
for img64 in base64_list:
    decoded = np.asarray(bytearray(base64.b64decode(img64)), dtype=np.uint8)
    img = cv2.imdecode(decoded, cv2.IMREAD_COLOR)
    img_list.append(img)

# example image plot
# plt.imshow(img)
# plt.show()


def fit_predict(df, limit=1.5):
    distance = pd.DataFrame(columns=['x','y', 'value'])
    predict = pd.Series(data=[0 for x in range(0,len(df),1)], index=df.index)
    count=1
    for i in range(0,len(df.index),1):
        for j in range(i+1,len(df.index),1):
            row1=np.array(df.iloc[i,:])
            row2=np.array(df.iloc[j,:])
            distance=distance.append({'x': df.index[i], 'y': df.index[j],'value':((row1-row2)**2).sum()}, ignore_index=True)
    
    distance=distance[distance.value<limit] # 거리 제한

    for k in distance.itertuples():
        try:
            x = distance.loc[distance.value.idxmin()]
            predict[int(x.x)]=count
            predict[int(x.y)]=count
            count += 1
            distance = distance[distance['x'] != x.x]
            distance = distance[distance['x'] != x.y]
            distance = distance[distance['y'] != x.x]
            distance = distance[distance['y'] != x.y]
        except:
            break
    return predict


for idx, file in enumerate(img_list):
    # img detection & preprocessing
    img = cv2.flip(file, 1)
    img2 = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    result = hands.process(img2)

    # result save
    if result.multi_hand_landmarks is not None:
        for res in result.multi_hand_landmarks:
            joint = np.zeros((21, 3))
            for j, lm in enumerate(res.landmark):
                joint[j] = [lm.x, lm.y, lm.z]

            # Compute angles between joints
            v1 = joint[[0,1,2,3,0,5,6,7,0,9,10,11,0,13,14,15,0,17,18,19],:] # Parent joint
            v2 = joint[[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20],:] # Child joint
            v = v2 - v1 # [20,3]
            # Normalize v
            v = v / np.linalg.norm(v, axis=1)[:, np.newaxis]

            # Get angle using arcos of dot product
            angle = np.arccos(np.einsum('nt,nt->n',
                v[[0,1,2,4,5,6,8,9,10,12,13,14,16,17,18],:], 
                v[[1,2,3,5,6,7,9,10,11,13,14,15,17,18,19],:])) # [15,]

            angle = np.degrees(angle) # Convert radian to degree
            angle_data.loc[idx] = angle

print(angle_data)
'''
predict = fit_predict(angle_data)
print(predict)

pca = PCA(n_components=2)
val = pca.fit_transform(angle_data)
df = pd.DataFrame(val, columns=['x','y'])

plt.title('clustering')
plt.scatter(df.x, df.y,c=predict, cmap='tab20')
plt.colorbar()
plt.show()
'''