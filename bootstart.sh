#!/bin/sh
echo =====================================
echo 自动化部署脚本启动
echo =====================================

echo 停止原来运行中的工程
APP_NAME=reggie

# 获取进程 ID（过滤掉 grep、kill 自身进程干扰）
tpid=$(ps -ef | grep $APP_NAME | grep -v grep | grep -v kill | awk '{print $2}')
if [ -n "${tpid}" ]; then
    echo 'Stop Process...'
    kill -15 $tpid
fi

sleep 2

# 再次检查进程，若仍存在则强制 kill -9
tpid=$(ps -ef | grep $APP_NAME | grep -v grep | grep -v kill | awk '{print $2}')
if [ -n "${tpid}" ]; then
    echo 'Kill Process!'
    kill -9 $tpid
else
    echo 'Stop Success!'
fi

echo 准备从 Git 仓库拉取最新代码
cd /usr/local/app

echo 开始从 Git 仓库拉取最新代码
if [ -d ".git" ]; then
    git pull
    if [ $? -ne 0 ]; then
        echo "Git拉取失败"
        exit 1
    fi
else
    git clone https://github.com/Liligo/reggie.git .
    if [ $? -ne 0 ]; then
        echo "Git克隆失败"
        exit 1
    fi
fi
echo 代码拉取完成

echo 开始打包
mvn clean package -Dmaven.test.skip=true
if [ $? -ne 0 ]; then
    echo "Maven打包失败"
    exit 1
fi

if [ -d "target" ]; then
    cd target
else
    echo "target目录不存在，打包可能失败"
    exit 1
fi

echo 启动项目
JAR_FILE=$(ls reggie-*.jar | sort -V | tail -n 1)
if [ -z "$JAR_FILE" ]; then
    echo "找不到JAR包"
    exit 1
fi
nohup java -jar "$JAR_FILE" &>> reggie.log &
echo "启动命令: nohup java -jar $JAR_FILE &>> reggie.log &"
echo 项目启动完成