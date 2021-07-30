FROM python:3.8.5
ADD . /consumer
WORKDIR /consumer
ENV TZ Asia/Seoul
RUN pip install -r requirements.txt

ENTRYPOINT ["python", "-u", "kafka_consumer.py", "172.18.0.1", "172.18.0.101:9092", "queue-topic"]