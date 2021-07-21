docker pull devwithpug/eureka-server
docker pull devwithpug/config-service
docker pull devwithpug/gateway-service
docker pull devwithpug/user-service
docker pull devwithpug/chat-service
docker pull devwithpug/gesture-service

docker-compose -f /home/ec2-user/docker-compose.yml up -d