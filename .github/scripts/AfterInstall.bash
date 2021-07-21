docker pull devwithpug/eureka-server:0.1
docker pull devwithpug/config-service:0.1
docker pull devwithpug/gateway-service:0.1
docker pull devwithpug/user-service:0.1
docker pull devwithpug/chat-service:0.1
docker pull devwithpug/gesture-service:0.1

/usr/local/bin/docker-compose -f /home/ec2-user/docker-compose.yml up -d