import numpy as np
import base64
import cv2
import pandas as pd
import mediapipe as mp

class Gesture_Predict:

    def __init__(self, limit=1):

        # mp setting
        mp_drawing = mp.solutions.drawing_utils
        mp_hands = mp.solutions.hands
        hands = mp_hands.Hands(
            static_image_mode=True,
            max_num_hands=2,
            min_detection_confidence=0.5)

        self.angle_data = pd.DataFrame(columns=['angle'+str(x) for x in range(0,18,1)])
        self.limit = limit
        self.hands = hands
        self.predict = []

    def refresh_data(self):
        self.angle_data = pd.DataFrame(columns=['angle'+str(x) for x in range(0,18,1)])
        self.predict = []

    # =======================================================================================
    # Compute Angles
    # =======================================================================================
    def compute_angles(self, img_list, user_ids):

        for idx, file in enumerate(img_list):
        
            # img detection & preprocessing
            img = cv2.flip(file, 1)
            try:
                img2 = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
            except Exception:
                return idx
            result = self.hands.process(img2)

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

                    # Normalize v
                    v = v / np.linalg.norm(v, axis=1)[:, np.newaxis]

                    # Get angle using arcos of dot product
                    angle = np.arccos(np.einsum('nt,nt->n',
                        v[[0,5, 9,13],:], 
                        v[[3,9,13,17],:]))
                    angle2 = np.arccos(np.einsum('nt,nt->n',
                        -v[[1,2,4,5,6,8, 9,10,12,13,14,16,17,18],:], 
                        v[[2,3,5,6,7,9,10,11,13,14,15,17,18,19],:]))

                    angle = np.concatenate((angle, angle2),axis=0)

                    # angle = np.degrees(angle) # Convert radian to degree
                    self.angle_data.loc[str(user_ids[idx])] = angle


    # =======================================================================================
    # Custom Unsupervised Learning Algorithm
    # =======================================================================================
    def fit_predict(self, df):
        distance = pd.DataFrame(columns=['x','y', 'value'])
        count=1
        for i in range(0,len(df.index),1):
            for j in range(i+1,len(df.index),1):
                row1=np.array(df.iloc[i,:])
                row2=np.array(df.iloc[j,:])
                distance=distance.append({'x': df.index[i], 'y': df.index[j],'value':((row1-row2)**2).sum()}, ignore_index=True)
    
        distance=distance[distance.value<self.limit] # 거리 제한

        for k in distance.itertuples():
            try:
                x = distance.loc[distance.value.idxmin()]
                self.predict.append([x.x, x.y])
                count += 1
                distance = distance[distance['x'] != x.x]
                distance = distance[distance['x'] != x.y]
                distance = distance[distance['y'] != x.x]
                distance = distance[distance['y'] != x.y]
            except:
                break
        return self.predict


    def do_predict(self, gesture_dict):
        self.refresh_data()
        img_list = []
        # image decode (서버에서 base64 형식으로 packet이 오면, 형태를 이미지화 해줌)
        for key in gesture_dict:
            decoded = np.asarray(bytearray(base64.b64decode(gesture_dict[key])), dtype=np.uint8)
            img = cv2.imdecode(decoded, cv2.IMREAD_COLOR)
            img_list.append(img)

        err = self.compute_angles(img_list, list(gesture_dict.keys()))

        if err is not None:
            return (True, err)

        df = self.angle_data
        thumb_false = df[df['angle0']*180/np.pi<90].iloc[:,1:]
        thumb_true = df[df['angle0']*180/np.pi>=90].iloc[:,1:]
        thumb_false.iloc[:,:3] *= 2
        thumb_true.iloc[:,:3] *= 2

        self.fit_predict(thumb_false)
        self.fit_predict(thumb_true)

        return (False, self.predict)
