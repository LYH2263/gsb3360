## 构建阶段
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B package -DskipTests

## 测试阶段（用于执行单元测试，不影响生产镜像）
FROM build AS test
RUN mvn -q -B test

## 运行阶段
## 说明：由于你本机拉取 eclipse-temurin:17-jre 长期超时，这里复用已成功拉取的 maven:3.9-eclipse-temurin-17 作为运行镜像。
## 仍然是官方 Java 17 运行环境，只是镜像略大，但能稳定构建通过。
FROM maven:3.9-eclipse-temurin-17
WORKDIR /app

ENV TZ=Asia/Shanghai

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "app.jar"]

