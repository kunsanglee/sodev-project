#!/bin/bash
PROJECT_NAME="sodev-project" # 프로젝트 이름
JAR_PATH="/home/ubuntu/sodev-project/sodev-api/build/libs/*.jar" # 빌드되는 절대 경로
DEPLOY_PATH=/home/ubuntu/$PROJECT_NAME/ # 배포할 경로 지정
DEPLOY_LOG_PATH="/home/ubuntu/$PROJECT_NAME/deploy.log" # 배포시 로그 남기는 경로
DEPLOY_ERR_LOG_PATH="/home/ubuntu/$PROJECT_NAME/deploy_err.log" # 배포 실패시 에러로그 남기는 경로
APPLICATION_LOG_PATH="/home/ubuntu/$PROJECT_NAME/application.log" # 어플리케이션에서 발생하는 로그 저장 경로
BUILD_JAR=$(ls $JAR_PATH)
JAR_NAME=$(basename $BUILD_JAR) # 스크립트를 실행시키면서 필요한 변수 정의

echo "===== 배포 시작 : $(date +%c) =====" >> $DEPLOY_LOG_PATH # 배포 시작하고 >> 하면 해당 로그가 $DEPLOY_LOG_PATH 라는 곳에 저장된다

echo "> build 파일명: $JAR_NAME" >> $DEPLOY_LOG_PATH
echo "> build 파일 복사" >> $DEPLOY_LOG_PATH
cp $BUILD_JAR $DEPLOY_PATH

echo "> 현재 동작중인 어플리케이션 pid 체크" >> $DEPLOY_LOG_PATH
CURRENT_PID=$(pgrep -f $JAR_NAME)

if [ -z $CURRENT_PID ]
then
  echo "> 현재 동작중인 어플리케이션 존재 X" >> $DEPLOY_LOG_PATH
else
  echo "> 현재 동작중인 어플리케이션 존재 O" >> $DEPLOY_LOG_PATH
  echo "> 현재 동작중인 어플리케이션 강제 종료 진행" >> $DEPLOY_LOG_PATH # 실제 회사에서는 강제로 종료하지않고 좀 더 유연하게 처리 할 것임.
  echo "> kill -9 $CURRENT_PID" >> $DEPLOY_LOG_PATH
  kill -9 $CURRENT_PID
fi

DEPLOY_JAR=$DEPLOY_PATH$JAR_NAME
echo "> DEPLOY_JAR 배포" >> $DEPLOY_LOG_PATH
nohup java -jar -Dspring.profiles.active=prod $DEPLOY_JAR --server.port=8080  >> $APPLICATION_LOG_PATH 2> $DEPLOY_ERR_LOG_PATH & # nohub 과 &를 사용해서 윈도우 창을 닫더라도, 터미널을 종료하더라도 백그라운드에서 동작할 수 있게 함. -Dspring.profiles.active=prod -> 멀티모듈을 사용해서 배포환경에 따라 local, dev, prod 이런식으로 지정하여 사용할 수 있음. --server.port=8080 -> 포트번호를 변경 가능

sleep 3

echo "> 배포 종료 : $(date +%c)" >> $DEPLOY_LOG_PATH # 배포 시작 시간과 종료 시간을 체크해서 배포하는데 얼마나 걸리는지 체크하여 보완

