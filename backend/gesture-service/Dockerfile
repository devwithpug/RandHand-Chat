FROM devwithpug/python_env_test:0.1
ADD ./app /app
WORKDIR /app
ENV TZ Asia/Seoul
ARG HOST, rabbitmq.host, rabbitmq.username, rabbitmq.password
RUN pip install -r requirements.txt

CMD ["sh", "-c", "python -u gesture_service.py $HOST $rabbitmq.host $rabbitmq.username $rabbitmq.password"]