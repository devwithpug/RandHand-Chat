import cv2
import numpy as np
import pandas as pd
import glob

#data load
input_img_path = 'handDetection/static_image/original_images/'
output_img_path = 'handDetection/static_image/original_images/resize/'

img_list = glob.glob(input_img_path+'*.jpg')

for idx, imgs in enumerate(img_list):

    img = cv2.imread(imgs)        
    img = cv2.resize(img, (1152,648))
    cv2.imwrite(output_img_path+'{}.jpg'.format(idx), img)
