from flask import Flask, request
import py_eureka_client.eureka_client as eureka_client
import socket
from contextlib import closing

rest_port = 5000

# 랜덤 포트 설정
with closing(socket.socket(socket.AF_INET, socket.SOCK_STREAM)) as s:
    s.bind(('', 0))
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    rest_port = s.getsockname()[1]

print(rest_port)
eureka_client.init(eureka_server="http://eureka-server:8761/eureka",
                    app_name="gesture-service",
                    instance_port=rest_port)

app = Flask(__name__)

# POST 테스트
@app.route("/post_test", methods=['POST'])
def postTest():
    data = request.json
    print(data)
    return data

# GET 테스트
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