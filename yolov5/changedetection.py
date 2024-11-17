import os
import cv2
import pathlib
import requests
from datetime import datetime

class ChangeDetection:
    result_prev = []
    HOST = 'https://syw5141.pythonanywhere.com'
    username = 'syw5141'
    password = '1234'
    token = 'b8f529195320e4886b7c00777630759ecfa21d7a'
    title = 'yolo'
    text = 'yolo test'

    def __init__(self, names):
        self.result_prev = [0 for _ in range(len(names))]
        res = requests.post(
            self.HOST + '/api-token-auth/',
            {'username': self.username, 'password': self.password}
        )
        res.raise_for_status()
        self.token = res.json()['token']  # 토큰 저장
        print(self.token)

    def add(self, names, detected_current, save_dir, image):
        self.title = ''
        self.text = ''
        change_flag = 0  # 변화 감지 플래그
        person_detected = False  # 사람이 감지되었는지 여부

        for i in range(len(self.result_prev)):
            if self.result_prev[i] == 0 and detected_current[i] == 1:
                change_flag = 1
                self.title = names[i]
                self.text += names[i] + ", "

            # 사람이 감지되었는지 확인
            if names[i].lower() == 'person' and detected_current[i] == 1:
                person_detected = True

        # 사람이 감지되지 않았다면 send 호출하지 않음
        if not person_detected:
            print("No person detected. Skipping send.")
            return

        self.result_prev = detected_current[:]  # 객체 검출 상태 저장

        if change_flag == 1:
            self.send(save_dir, image)



    def send(self, save_dir, image):
        now = datetime.now()
        today = datetime.now()
        save_path = pathlib.Path(
            os.getcwd()
        ) / save_dir / 'detected' / str(today.year) / str(today.month) / str(today.day)
        save_path.mkdir(parents=True, exist_ok=True)

        full_path = save_path / '{0}-{1}-{2}-{3}.jpg'.format(
            today.hour, today.minute, today.second, today.microsecond
        )

        dst = cv2.resize(image, dsize=(320, 240), interpolation=cv2.INTER_AREA)
        cv2.imwrite(str(full_path), dst)

        # 인증이 필요한 요청에 아래의 headers를 붙임
        headers = {'Authorization': 'TOKEN ' + self.token, 'Accept': 'application/json'}

        # Post Create
        data = {
            'title': self.title,
            'text': self.text,
            'created_date': now.isoformat(),
            'published_date': now.isoformat(),
            'author': "1"
        }
        files = {'image': open(full_path, 'rb')}

        res = requests.post(self.HOST + '/api_root/Post/', data=data, files=files, headers=headers)
        print(res)
