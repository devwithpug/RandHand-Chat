FROM python:3.8.5
ADD . /app
WORKDIR /app
ENV TZ Asia/Seoul
RUN pip install -r requirements.txt

ENTRYPOINT ["python", "gesture_service.py", "eureka-server:8761"]