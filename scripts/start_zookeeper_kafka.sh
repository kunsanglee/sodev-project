#!/bin/sh

# Zookeeper 프로세스 확인
if ! pgrep -f "zookeeper-server-start" > /dev/null; then
    /home/ubuntu/kafka/bin/zookeeper-server-start.sh /home/ubuntu/kafka/config/zookeeper.properties &
fi

sleep 20  # Zookeeper가 완전히 시작되길 기다린 후

# Kafka 프로세스 확인
if ! pgrep -f "kafka-server-start" > /dev/null; then
    /home/ubuntu/kafka/bin/kafka-server-start.sh /home/ubuntu/kafka/config/server.properties &
fi
