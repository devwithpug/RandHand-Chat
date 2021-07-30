from kafka import KafkaConsumer, TopicPartition
from kafka.admin import KafkaAdminClient, NewTopic
from kafka.errors import NoBrokersAvailable
import json, argparse, requests, time


def log(msg):
    print("[{}] {}".format(
            time.strftime("%c", time.localtime(time.time())),
            msg
        ))


parser = argparse.ArgumentParser()
parser.add_argument('gateway_host', nargs='?', type=str, default='localhost', help='ex) "localhost"')
parser.add_argument('kafka_host_ip', nargs='?', type=str, default='localhost:9092', help='ex) "localhost:9092"')
parser.add_argument('kafka_topic', nargs='?', type=str, default='queue-topic', help='ex) "queue-topic"')
args = parser.parse_args()

GATEWAY = args.gateway_host
HOST = args.kafka_host_ip
TOPIC = args.kafka_topic
log("{}, {}".format(HOST, TOPIC))

# Return KafkaConsumer that subscribes specific topics.
def init_consumer(server, topic_name):

    consumer = KafkaConsumer(group_id='queue_listener_group',
                            bootstrap_servers=[server],
                            value_deserializer=lambda m: json.loads(m.decode('utf-8')))
    # queue-topic 없는 경우 새로 생성
    if not topic_name in consumer.topics():
        admin_client = KafkaAdminClient(bootstrap_servers=[server], client_id='python_kafka_admin_client')
        admin_client.create_topics([NewTopic(topic_name, 1, 1)])

    tp = TopicPartition(topic_name, 0)
    consumer.assign([tp])

    return consumer

if __name__ == "__main__":

    consumer = None

    while consumer is None:
        try:
            consumer = init_consumer(HOST, TOPIC)
        except NoBrokersAvailable as err:
            log("ERROR: {} / Retry in 10 seconds".format(err))
            time.sleep(10.0)

    log("Broker Connected, topics: {}".format(consumer.topics()))

    for msg in consumer:
        assert isinstance(msg.value, dict)
        log(msg.value)
        requests.post("http://" + GATEWAY + ":8000/gesture-service/queue", json=msg.value)


# 1. android 에서 chat-service/chats POST 요청 보냄 (makeChatQueue)
# 2. queueDto를 produce(sendQueue)[queue-topic] 함
# 3. python에서 userId, gesture 이용해서 매칭… 
# 4. 성공시 chatDto produce(flask구현필요)[match-topic]
# 5. chat-service에서 consume하여 ChatRoom 생성 후 produce[chat-topic]
# 6. android 에서 consume 하여 웹소켓 세션 접속 수행
# (이때 하나의 partition은 하나의 consumer만 가능하므로 userId를
# consumer group id로 설정하여 multiple consumer들이 consume하도록함

# 참고할 것
# KAFKA
# https://pypi.org/project/kafka-python/
# https://kafka-python.readthedocs.io/en/master/usage.html
# https://ichi.pro/ko/socketioleul-sayonghayeo-kafka-mesijileul-saengseong-sobihaneun-flask-api-bildeu-164520298467772

# DOCKER
# https://runnable.com/docker/python/dockerize-your-flask-application