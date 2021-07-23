docker pull devwithpug/eureka-server:0.1
docker pull devwithpug/config-service:0.1
docker pull devwithpug/gateway-service:0.1
docker pull devwithpug/user-service:0.1
docker pull devwithpug/chat-service:0.1
docker pull devwithpug/gesture-service:0.1

/usr/local/bin/docker-compose -f /home/ec2-user/docker-compose.yml up -d rabbitmq
/home/ec2-user/sleep.sh
/usr/local/bin/docker-compose -f /home/ec2-user/docker-compose.yml up -d config-service
/home/ec2-user/sleep.sh
/usr/local/bin/docker-compose -f /home/ec2-user/docker-compose.yml up -d eureka-server
/home/ec2-user/sleep.sh
/usr/local/bin/docker-compose -f /home/ec2-user/docker-compose.yml up -d gateway-service
/home/ec2-user/sleep.sh
/usr/local/bin/docker-compose -f /home/ec2-user/docker-compose.yml up -d zookeeper
/home/ec2-user/sleep.sh
/usr/local/bin/docker-compose -f /home/ec2-user/docker-compose.yml up -d kafka
/home/ec2-user/sleep.sh
/usr/local/bin/docker-compose -f /home/ec2-user/docker-compose.yml up -d user-service
/home/ec2-user/sleep.sh
/usr/local/bin/docker-compose -f /home/ec2-user/docker-compose.yml up -d chat-service
/home/ec2-user/sleep.sh
/usr/local/bin/docker-compose -f /home/ec2-user/docker-compose.yml up -d gesture-service