import os
import cv2
import mediapipe as mp
import pandas as pd 
mp_drawing = mp.solutions.drawing_utils
mp_hands = mp.solutions.hands

# For static images:
path = 'C:\\Users\\dh\\Desktop\\Git\\RandHand-Chat\\handDetection\\static_image\\original_images\\resize' # 파일 경로 지정
os.chdir(path)
IMAGE_FILES = os.listdir(path)
# --------------------------------------<파일 순서 개선>---------------------------------------
# 파일 이름은 숫자로 넣어줘야 함
COPY_FILES = []

for i in IMAGE_FILES:
    COPY_FILES.append(i.replace('.jpg', ''))

COPY_FILES=sorted(COPY_FILES, key=int)
IMAGE_FILES = []

for i in COPY_FILES:
    IMAGE_FILES.append(i+'.jpg')

# --------------------------------------------------------------------------------------------

with mp_hands.Hands(
  static_image_mode=True,
  max_num_hands=2,
  min_detection_confidence=0.5) as hands:

  column = []
  column += ['x'+str(i) for i in range(0,21,1)]
  column += ['y'+str(i) for i in range(0,21,1)]
  column += ['z'+str(i) for i in range(0,21,1)]

  df = pd.DataFrame([],columns=column)

  for idx, file in enumerate(IMAGE_FILES):
    # Read an image, flip it around y-axis for correct handedness output (see
    # above).
    image = cv2.flip(cv2.imread(file), 1)
    # Convert the BGR image to RGB before processing.
    results = hands.process(cv2.cvtColor(image, cv2.COLOR_BGR2RGB))

    # Print handedness and draw hand landmarks on the image.
    print('Handedness:', results.multi_handedness)
    if not results.multi_hand_landmarks:
      continue
    image_height, image_width, _ = image.shape
    annotated_image = image.copy()

    for hand_landmarks in results.multi_hand_landmarks:
      locate = []

      for ids, landmrk in enumerate(hand_landmarks.landmark):
        locate.append(landmrk.x)

      for ids, landmrk in enumerate(hand_landmarks.landmark):
        locate.append(landmrk.y)

      for ids, landmrk in enumerate(hand_landmarks.landmark):
        locate.append(landmrk.z)
            
      mp_drawing.draw_landmarks(
        annotated_image, hand_landmarks, mp_hands.HAND_CONNECTIONS)
      # csv 파일 제작
      df.loc[idx]=locate

    cv2.imwrite(
        'C:/Users/dh/Desktop/Git/RandHand-Chat/handDetection/static_image/annotated_images/' + str(idx) + '.png', cv2.flip(annotated_image, 1))
        
df.to_csv('C:/Users/dh/Desktop/Git/RandHand-Chat/handDetection/static_image/dataSets/mediapipe.csv',mode='w')