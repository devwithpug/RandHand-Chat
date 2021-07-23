FROM python:3.8.5
ADD . /app
WORKDIR /app
RUN pip install -r requirements.txt

ENTRYPOINT ["python", "gesture_service.py"]