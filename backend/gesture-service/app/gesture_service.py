from flask import Flask, request, Response
from flask_apscheduler import APScheduler, scheduler
import py_eureka_client.eureka_client as eureka_client
import socket, argparse, time, json, pika
from contextlib import closing
from gesture import Gesture_Predict

def log(msg):
    print("[{}] {}".format(
            time.strftime("%c", time.localtime(time.time())),
            msg
        ))

gesture_dict = {}
parser = argparse.ArgumentParser()
parser.add_argument('host', nargs='?', type=str, default='localhost', help='ex) "localhost"')
parser.add_argument('rabbitmq_host', nargs='?', type=str, default='localhost', help='ex) "localhost"')
parser.add_argument('username', nargs='?', type=str, default='guest', help='ex) "guest"')
parser.add_argument('password', nargs='?', type=str, default='guest', help='ex) "guest"')
args = parser.parse_args()

HOST_IP = args.host
rabbitmq_host = args.rabbitmq_host
username = args.username
password = args.password
log("{} {} {} {}".format(HOST_IP, rabbitmq_host, username, password))

channel = None
credentials = pika.PlainCredentials(username=username, password=password)

while channel is None:
    try:
        connection = pika.BlockingConnection(pika.ConnectionParameters(rabbitmq_host, '5672', credentials=credentials, heartbeat=300, blocked_connection_timeout=300))
        channel = connection.channel()
    except RuntimeError as err:
        log(err + "\nretry in 1m")
        time.sleep(60.0)

channel.exchange_declare(exchange='match.direct.exchange', exchange_type='fanout', durable=False)

predict = Gesture_Predict(limit=1.5)
rest_port = 5000

# 랜덤 포트 설정
with closing(socket.socket(socket.AF_INET, socket.SOCK_STREAM)) as s:
    s.bind(('', 0))
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    rest_port = s.getsockname()[1]

eureka_client.init(eureka_server=HOST_IP+":8761/eureka",
                    app_name="gesture-service",
                    instance_port=rest_port)

app = Flask(__name__)
scheduler = APScheduler()
scheduler.api_enabled = True
scheduler.init_app(app)
scheduler.start()


@scheduler.task('interval', id='predict_gesture', seconds=10)
def do_predict():

    if len(gesture_dict) > 0:

        err, result = predict.do_predict(gesture_dict)

        if err:
            log("ERROR invalid image : {}", result)
            gesture_dict.pop(result)
            log("invalid image '{}' was removed", result)

        if len(result) > 0:

            for ids in result:
                gesture_dict.pop(ids[0])
                gesture_dict.pop(ids[1])
                log("New chat room created! : {}".format(ids))

                value = {'userIds': ids}
                body = json.dumps(value)

                channel.basic_publish(
                    exchange='match.direct.exchange',
                    routing_key='match.queue',
                    body=body
                )

            log("gesture_queue : {}".format(list(gesture_dict.keys())))


# create Queue
@app.route("/queue", methods=['POST'])
def create_queue():

    try:
        gesture = request.json['gesture']
        userId = request.headers['userId']

        assert isinstance(gesture, str)
        assert isinstance(userId, str)
    except (AssertionError, KeyError):
        error = {"error": "gesture, userId 모두 입력해야 합니다"}
        return error, 400

    gesture_dict[userId] = gesture

    log("create queue : {}".format(userId))
    log("gesture_queue : {}".format(list(gesture_dict.keys())))

    return Response(status=200)

# cancel queue
@app.route("/queue", methods=['DELETE'])
def cancel_queue():

    try:
        userId = request.headers['userId']
    except KeyError:
        error = {"error": "userId 값이 필요합니다"}
        return error, 400

    try:
        gesture_dict.pop(userId)
    except KeyError:
        error = {"error": "queue에 등록되지 않은 userId 입니다"}
        return error, 404

    log("cancel queue : {}".format(userId))

    return Response(status=200)

# health_check
@app.route("/health", methods= ['GET'])
def health():
    return "gesture-service is ON port : {}".format(rest_port)


if __name__ == "__main__":
    app.run(host='0.0.0.0', port= rest_port)
    connection.close()
