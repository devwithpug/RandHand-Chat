from flask import Flask, request
import py_eureka_client.eureka_client as eureka_client
import socket, argparse
from contextlib import closing

parser = argparse.ArgumentParser()
parser.add_argument('eureka_server_ip', nargs='?', type=str, default='localhost:8761', help='ex) "localhost:8761"')
args = parser.parse_args()

EUREKA_IP = args.eureka_server_ip

rest_port = 5000

# 랜덤 포트 설정
with closing(socket.socket(socket.AF_INET, socket.SOCK_STREAM)) as s:
    s.bind(('', 0))
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    rest_port = s.getsockname()[1]

eureka_client.init(eureka_server=EUREKA_IP+"/eureka",
                    app_name="gesture-service",
                    instance_port=rest_port)

app = Flask(__name__)

# create Queue
@app.route("/queue", methods=['POST'])
def create_queue():

    # TODO - request data 검증 & 매칭 수행

    data = request.json
    print(data)
    return data

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