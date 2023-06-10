FROM maven:3.8.5-openjdk-17-slim AS build
RUN mkdir -p /workspace
WORKDIR /workspace
COPY pom.xml /workspace
COPY src /workspace/src
RUN mvn -f pom.xml -Dmaven.test.skip clean package

FROM openjdk:17-jdk-slim
COPY --from=build /workspace/target/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=production
EXPOSE 8443
ENTRYPOINT ["java","-jar","app.jar"]