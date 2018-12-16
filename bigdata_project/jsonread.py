import urllib.request, json
with urllib.request.urlopen("http://arena.kakaocdn.net/kakao_arena/shopping/cate1.json?TWGServiceId=kakao_arena&Expires=1543608849&Signature=b3ArHHj547aTxJ5roMNpqGcIE2o%3D&AllowedIp=220.66.115.87") as url:
    data = json.loads(url.read().decode())
    obj = open('cate_s.txt', 'w')
    for item in data['s'].items():
        obj.write(str(item[1]) + ':' + str(item[0]) + '\n')
    obj.close