docker pull devwithpug/eureka-server:0.1
docker pull devwithpug/config-service:0.1
docker pull devwithpug/gateway-service:0.1
docker pull devwithpug/user-service:0.1
docker pull devwithpug/chat-service:0.1
docker pull devwithpug/gesture-service:0.1

/usr/local/bin/docker-compose -f /home/ec2-user/docker-compose.yml up -d rabbitmq
/usr/local/bin/docker-compose -f /home/ec2-user/docker-compose.yml run --name wait-for-sec wait-for-sec
/usr/local/bin/docker-compose -f /home/ec2-user/docker-compose.yml up -d config-service
/usr/bin/docker start -a wait-for-sec
/usr/local/bin/docker-compose -f /home/ec2-user/docker-compose.yml up -d eureka-server
/usr/bin/docker start -a wait-for-sec
/usr/local/bin/docker-compose -f /home/ec2-user/docker-compose.yml up -d gateway-service
/usr/bin/docker start -a wait-for-sec
/usr/local/bin/docker-compose -f /home/ec2-user/docker-compose.yml up -d zookeeper
/usr/bin/docker start -a wait-for-sec
/usr/local/bin/docker-compose -f /home/ec2-user/docker-compose.yml up -d kafka
/usr/bin/docker start -a wait-for-sec
/usr/local/bin/docker-compose -f /home/ec2-user/docker-compose.yml up -d user-service
/usr/bin/docker start -a wait-for-sec
/usr/local/bin/docker-compose -f /home/ec2-user/docker-compose.yml up -d chat-service
# /usr/bin/docker start -a wait-for-sec
# /usr/local/bin/docker-compose -f /home/ec2-user/docker-compose.yml up -d gesture-service
/usr/bin/docker stop wait-for-sec
/usr/bin/docker rm wait-for-sec