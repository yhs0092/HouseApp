FROM swr.cn-north-1.myhuaweicloud.com/hwcse/dockerhub-java:8-jre-alpine
RUN mkdir -p /home/apps/server
COPY ./edge-service/target/edge-service-0.0.1-SNAPSHOT.jar /home/apps/server
COPY ./edge-service/target/lib/ /home/apps/server/lib
ENTRYPOINT ["java", "-jar", "/home/apps/server/edge-service-0.0.1-SNAPSHOT.jar"]
