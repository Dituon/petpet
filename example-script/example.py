import requests, json
from urllib.parse import quote

serverHost = 'http://127.0.0.1:2333/petpet'

dataList = json.loads(requests.get(serverHost).text)
for data in dataList['petData']:
    print('Key: ' + data['key'] + '  Required parameters: ' + str(data['types']))

key = 'petpet'
toAvatarUrl = 'https://avatars.githubusercontent.com/u/68615161?v=4'
apiUrl = serverHost + f"?key={quote(key)}&toAvatar={quote(toAvatarUrl)}"
print('Key: ' + key + '\n' + apiUrl)

key = 'osu'
texts = 'hso!'
apiUrl = serverHost + f"?key={quote(key)}&textList={quote(texts)}"
print('Key: ' + key + '\n' + apiUrl)

"""
from PIL import Image
import io

image = Image.open(io.BytesIO(requests.get(apiUrl).content))
# image.save(key + '.' + image.format)
image.show()
"""
