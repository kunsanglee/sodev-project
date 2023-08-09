#!/bin/sh

/home/ubuntu/kafka/bin/zookeeper-server-start.sh /home/ubuntu/kafka/config/zookeeper.properties &

sleep 10  # Zookeeper가 완전히 시작되길 기다린 후

/home/ubuntu/kafka/bin/kafka-server-start.sh /home/ubuntu/kafka/config/server.properties &