from urllib.parse import quote

import json
import requests

serverHost = 'http://127.0.0.1:2333/petpet'

dataList = json.loads(requests.get(serverHost).text)
for data in dataList['petData']:
    print('Key: ' + data['key'] + '  Required parameters: ' + str(data['types']))

# GET
key = 'petpet'
toAvatarUrl = 'https://avatars.githubusercontent.com/u/68615161?v=4'
apiUrl = serverHost + f"?key={quote(key)}&toAvatar={quote(toAvatarUrl)}"
print('Key: ' + key + '\n' + apiUrl)
# imageBlob = requests.get(serverHost).content

key = 'osu'
texts = 'hso!'
apiUrl = serverHost + f"?key={quote(key)}&textList={quote(texts)}"
print('Key: ' + key + '\n' + apiUrl)
# imageBlob = requests.get(serverHost).content


# POST
def fetch_image(postDTO):
    return requests.post(
        url=serverHost,
        json=postDTO
    ).content

petPostDTO = {
    'key': 'petpet',
    'to': {
        'name': 'Dituon',
        'avatar': toAvatarUrl
    }
}
# imageBlob = fetch_image(petPostDTO)

"""
from PIL import Image
import io

image = Image.open(io.BytesIO(imageBlob))
# image.save(key + '.' + image.format)
image.show()
"""
