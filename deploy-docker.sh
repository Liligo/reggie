#!/bin/bash

# deploy-docker.sh

# --- 配置区 ---
APP_NAME="reggie"
IMAGE_NAME="liligo/${APP_NAME}:latest"
CONTAINER_NAME="${APP_NAME}-container"
GIT_REPO_URL="https://github.com/Liligo/reggie.git"
PROJECT_DIR="reggie" # 新增：用于存放源码的子目录

# 环境变量文件名，部署时会加载这个文件
ENV_FILE="prod.env"

# 部署相关的配置

UPLOAD_DIR="${PROJECT_DIR}/src/main/java/com/liligo/reggie/imgs/"# 宿主机上用于存放上传文件的目录
HOST_PORT=8080
CONTAINER_PORT=8080
LOG_FILE="deploy.log"

# --- 脚本执行区 ---
# 将标准输出和标准错误都重定向到控制台和日志文件
exec > >(tee -i $LOG_FILE)
exec 2>&1

echo "=================================================="
echo "Docker 自动化部署脚本启动 - $(date)"
echo "=================================================="

# 函数：检查上一条命令是否成功
check_status() {
    if [ $? -ne 0 ]; then
        echo "错误: 上一步操作失败，脚本终止。"
        exit 1
    fi
}

echo "步骤 1: 更新或克隆 Git 仓库..."
if [ -d "$PROJECT_DIR/.git" ]; then
    echo "Git 仓库已存在，进入目录并执行 git pull..."
    cd ${PROJECT_DIR}
    check_status
    git pull
    check_status
else
    echo "Git 仓库不存在，删除旧目录并重新克隆..."
    rm -rf ${PROJECT_DIR}
    git clone ${GIT_REPO_URL} ${PROJECT_DIR}
    check_status
    cd ${PROJECT_DIR}
    check_status
fi
echo "代码更新/克隆完成。"

echo "步骤 2: 停止并删除旧容器..."
if [ "$(docker ps -a -q -f name=^/${CONTAINER_NAME}$)" ]; then
    docker stop ${CONTAINER_NAME} && docker rm ${CONTAINER_NAME}
    check_status
    echo "旧容器 ${CONTAINER_NAME} 已停止并删除。"
else
    echo "未找到旧容器，无需操作。"
fi

echo "步骤 3: 删除旧镜像..."
if [ "$(docker images -q ${IMAGE_NAME})" ]; then
    docker rmi ${IMAGE_NAME}
    check_status
    echo "旧镜像 ${IMAGE_NAME} 已删除。"
else
    echo "未找到旧镜像，无需操作。"
fi

echo "步骤 4: 构建 Docker 镜像..."
# 注意：我们现在在 PROJECT_DIR 目录内执行构建
docker build -t ${IMAGE_NAME} .
check_status
echo "镜像 ${IMAGE_NAME} 构建成功。"

# 返回到上层目录，以便访问 prod.env
cd ..
check_status

echo "步骤 5: 创建宿主机持久化目录..."
mkdir -p ${UPLOAD_DIR}
check_status
echo "目录 ${UPLOAD_DIR} 已准备就绪。"

echo "步骤 6: 启动新的 Docker 容器..."
if [ ! -f "$ENV_FILE" ]; then
    echo "错误: 环境变量文件 '$ENV_FILE' 未找到！请确保它与本脚本在同一目录。"
    exit 1
fi

docker run -d \
    -p ${HOST_PORT}:${CONTAINER_PORT} \
    --name ${CONTAINER_NAME} \
    -v ${UPLOAD_DIR}:/app/uploads \
    --env-file ./${ENV_FILE} \
    --restart always \
    ${IMAGE_NAME}
check_status

echo "=================================================="
echo "部署成功！"
echo "容器 ${CONTAINER_NAME} 已启动。"
echo "查看运行日志：docker logs -f -t ${CONTAINER_NAME}"
echo "访问地址: http://<你的服务器IP>:${HOST_PORT}"
echo "=================================================="