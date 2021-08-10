from flask import Flask, request, Response
from flask_apscheduler import APScheduler, scheduler
import py_eureka_client.eureka_client as eureka_client
import socket, argparse, time, json
from contextlib import closing
from kafka import KafkaProducer
from gesture import Gesture_Predict

def log(msg):
    print("[{}] {}".format(
            time.strftime("%c", time.localtime(time.time())),
            msg
        ))

gesture_dict = {}
parser = argparse.ArgumentParser()
parser.add_argument('host', nargs='?', type=str, default='localhost', help='ex) "localhost"')
args = parser.parse_args()

EUREKA_IP = args.host

rest_port = 5000

# 랜덤 포트 설정
with closing(socket.socket(socket.AF_INET, socket.SOCK_STREAM)) as s:
    s.bind(('', 0))
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    rest_port = s.getsockname()[1]

rest_port = 5000

eureka_client.init(eureka_server=EUREKA_IP+":8761/eureka",
                    app_name="gesture-service",
                    instance_port=rest_port)

app = Flask(__name__)
scheduler = APScheduler()
scheduler.api_enabled = True
scheduler.init_app(app)
scheduler.start()

producer = KafkaProducer(acks=0,
                         compression_type='gzip', 
                         bootstrap_servers=[EUREKA_IP+':9092'],
                         value_serializer=lambda x: json.dumps(x).encode('utf-8'))

@scheduler.task('interval', id='predict_gesture', seconds=10)
def do_predict():

    predict = Gesture_Predict(gesture_dict, limit=1.5)
    result = predict()

    if len(result) > 0:

        for ids in result:
            gesture_dict.pop(ids[0])
            gesture_dict.pop(ids[1])
            log("New chat room created! : {}".format(ids))

            value = {'userIds': ids}
            producer.send('match-topic', value=value)

        log("gesture_queue : {}".format(list(gesture_dict.keys())))

# TODO - chat room 생성한뒤에 chat-service [match-topic] 전송까지 완료
# TODO - chat-service 에서 [match-topic] consume하여 chatroom 싱글턴 리스트로 관리하고
# TODO - [chat-topic] 으로 chatroom 전송 & 안드에서 컨슘하여 채팅방 접속
# TODO - 안드에서 큐 삭제 요청이 오거나 채팅방 나가기 요청이 오는 경우 chatroom 리스트에서 sessionId삭제

# create Queue
@app.route("/queue", methods=['POST'])
def create_queue():

    gesture = request.json['gesture']
    userId = request.headers['userId']

    try:
        assert isinstance(gesture, str)
        assert isinstance(userId, str)
    except AssertionError:
        return Response(status=400)

    gesture_dict[userId] = gesture

    log("create queue : {}".format(userId))
    log("gesture_queue : {}".format(list(gesture_dict.keys())))

    return Response(status=200)

@app.route("/queue/cancel", methods=['POST'])
def cancel_queue():

    userId = request.headers['userId']

    try:
        gesture_dict.pop(userId)
    except KeyError:
        return Response(status=404)

    log("cancel queue : {}".format(userId))
    return Response(status=200)

# health_check
@app.route("/health", methods= ['GET'])
def health():
    return "gesture-service is ON port : {}".format(rest_port)


if __name__ == "__main__":
    app.run(host='0.0.0.0', port= rest_port)


# 참고할 것
# KAFKA
# https://pypi.org/project/kafka-python/
# https://kafka-python.readthedocs.io/en/master/usage.html
# https://ichi.pro/ko/socketioleul-sayonghayeo-kafka-mesijileul-saengseong-sobihaneun-flask-api-bildeu-164520298467772

# DOCKER
# https://runnable.com/docker/python/dockerize-your-flask-application