if [ -d /home/ec2-user/randhand/build ]; then
  rm -rf /home/ec2-user/randhand/build
fi

mkdir -vp /home/ec2-user/randhand/build

/usr/local/bin/docker-compose -f /home/ec2-user/docker-compose.yml down

if [[ "$(docker ps -a -q 2> /dev/null)" != "" ]]; then
    docker stop $(docker ps -a -q)
    docker rm $(docker ps -a -q)
    docker rmi $(docker images -q)
fi