import os
import cv2
import mediapipe as mp
mp_drawing = mp.solutions.drawing_utils
mp_hands = mp.solutions.hands

# For static images:
path = 'C:/Users/mpoli/Desktop/Git/RandHand-Chat/mediaPipe/proto_image' # 파일 경로 지정
os.chdir(path)
IMAGE_FILES = os.listdir(path)

with mp_hands.Hands(
  static_image_mode=True,
  max_num_hands=2,
  min_detection_confidence=0.5) as hands:

  f = open('../coordinate/mediapipe.csv', 'w')
  column = []
  for i in range(0,21,1):
    column.append('x'+str(i))
  for i in range(0,21,1):
    column.append('y'+str(i))
  for i in range(0,21,1):
    column.append('z'+str(i))
  for i in range(0,62,1):
    f.write(column[i]+',')
  f.write('z20'+'\n')

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
        locate.append(str(landmrk.x))

      for ids, landmrk in enumerate(hand_landmarks.landmark):
        locate.append(str(landmrk.y))

      for ids, landmrk in enumerate(hand_landmarks.landmark):
        locate.append(str(landmrk.z))
            
      mp_drawing.draw_landmarks(
        annotated_image, hand_landmarks, mp_hands.HAND_CONNECTIONS)
        
    # csv 파일 제작
    f.write(locate.pop(0))
    for i in locate:
      f.write(','+i)
    f.write('\n')

    cv2.imwrite(
        '../completion_image/annotated_image' + str(idx) + '.png', cv2.flip(annotated_image, 1))