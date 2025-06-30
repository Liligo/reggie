# --- 第一阶段: 构建阶段 ---
# 使用包含 Maven 和 JDK 17 的镜像作为构建环境
FROM maven:3.9.4-eclipse-temurin-17 AS builder

# 设置工作目录
WORKDIR /app

# 仅复制 pom.xml 以利用 Docker 的层缓存机制
COPY pom.xml .
# 下载所有依赖
RUN mvn dependency:go-offline

# 复制所有源代码
COPY src ./src

# 打包应用，跳过测试
RUN mvn clean package -Dmaven.test.skip=true

# --- 第二阶段: 运行阶段 ---
# 使用非常轻量的 JRE 镜像作为最终运行环境
FROM eclipse-temurin:17-jre-alpine

# 设置工作目录
WORKDIR /app

# 从构建阶段复制已打包的 JAR 文件
# 请确保这里的 JAR 文件名与 pom.xml 中的 artifactId 和 version 一致
COPY --from=builder /app/target/reggie-0.0.1-SNAPSHOT.jar app.jar

# 暴露应用端口
EXPOSE 8080

# 设置 JVM 参数并启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]