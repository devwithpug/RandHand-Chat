FROM devwithpug/python_env_test:0.1
ADD ./app /app
WORKDIR /app
ENV TZ Asia/Seoul
ARG HOST
ARG rabbitmq_host
ARG rabbitmq_username
ARG rabbitmq_password
RUN pip install -r requirements.txt

CMD ["sh", "-c", "python -u gesture_service.py $HOST $rabbitmq_host $rabbitmq_username $rabbitmq_password"]