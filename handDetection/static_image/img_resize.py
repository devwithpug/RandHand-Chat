import cv2
import numpy as np
import pandas as pd
import glob

#data load
input_img_path = 'D:/archive/dataset/woman/U/'
output_img_path = 'D:/data/'

img_list = glob.glob(input_img_path+'*.jpg')

for idx, imgs in enumerate(img_list):
    if idx==50:
        break
    img = cv2.imread(imgs)        
    img = cv2.resize(img, (1152,648))
    cv2.imwrite(output_img_path+'{}.jpg'.format(idx+1050), img)
