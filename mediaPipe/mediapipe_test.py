import os
import cv2
import mediapipe as mp
mp_drawing = mp.solutions.drawing_utils
mp_hands = mp.solutions.hands

# For static images:
path = 'C:/Users/dh/Desktop/Git/RandHand-Chat/mediaPipe/proto_image'
os.chdir(path)
IMAGE_FILES = os.listdir(path)

with mp_hands.Hands(
    static_image_mode=True,
    max_num_hands=2,
    min_detection_confidence=0.5) as hands:
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

    f = open('../coordinate/c_'+str(idx)+'.txt','w')
    for hand_landmarks in results.multi_hand_landmarks:
      f.write('\n x locate \n -------------------------\n')
      for ids, landmrk in enumerate(hand_landmarks.landmark):
        cx = str(landmrk.x)+'\n'
        f.write(cx)
      f.write('\n y locate \n -------------------------\n')
      for ids, landmrk in enumerate(hand_landmarks.landmark):
        cy = str(landmrk.y)+'\n'
        f.write(cy)
      f.write('\n z locate \n -------------------------\n')
      for ids, landmrk in enumerate(hand_landmarks.landmark):
        cz = str(landmrk.z)+'\n'
        f.write(cz)
            
      mp_drawing.draw_landmarks(
        annotated_image, hand_landmarks, mp_hands.HAND_CONNECTIONS)
    cv2.imwrite(
        '../completion_image/annotated_image' + str(idx) + '.png', cv2.flip(annotated_image, 1))