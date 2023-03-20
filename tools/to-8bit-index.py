from PIL import Image
import os

src_folder = './'
dst_folder = src_folder

if not os.path.exists(dst_folder):
    os.makedirs(dst_folder)

for filename in os.listdir(src_folder):
    if filename.endswith('.png'):
        with Image.open(os.path.join(src_folder, filename)) as img:
            img = img.convert('P', palette=Image.ADAPTIVE, colors=256)
            img.save(os.path.join(dst_folder, filename))