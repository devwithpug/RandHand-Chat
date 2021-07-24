FROM python:3.8.5
ADD . /consumer
WORKDIR /consumer
RUN pip install -r requirements.txt

ENTRYPOINT ["python", "kafka_consumer.py", "172.18.0.101:9092", "queue-topic"]