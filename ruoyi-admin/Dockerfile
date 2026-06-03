
# Dockerfile
# 使用官方支持多架构的 JDK 镜像
# FROM adoptopenjdk:17-jre-hotspot
FROM eclipse-temurin:17-jre


# 安装 JDK 17
# RUN apt-get update && apt-get install -y --no-install-recommends \
 #   openjdk-17-jre-headless \
 #   && apt-get clean \
 #   && rm -rf /var/lib/apt/lists/*

# 设置工作目录
WORKDIR /app

# 将你编译好的 JAR 包复制到镜像中
# 假设你的 JAR 包在 target 目录下，且名字包含 "my-java-app"
 COPY target/*.jar app.jar
# COPY ruoyi-admin/target/*.jar app.jar

EXPOSE 9898

# 指定容器启动命令（使用 exec 格式以便接收停止信号）
ENTRYPOINT ["java", "-jar", "app.jar"]