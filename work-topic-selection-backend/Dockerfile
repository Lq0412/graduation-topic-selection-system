# 编译代码(请在本地直接使用 ./mvnw clean package -DskipTests 进行编译避免容器过大, 可以提前使用 mvn wrapper:wrapper 安装包装器来跨环境编译)
FROM openjdk:8-jdk-slim
WORKDIR /app
COPY ./target/*.jar ./app.jar
ENV SERVER_ADDRESS=0.0.0.0
EXPOSE 8000
CMD ["java", "-Duser.timezone=Asia/Shanghai", "-jar", "app.jar"]
