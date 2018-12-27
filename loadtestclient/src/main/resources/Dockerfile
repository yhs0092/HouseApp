FROM swr.cn-north-1.myhuaweicloud.com/hwcse/dockerhub-java:8-jre-alpine
RUN mkdir -p /home/apps/server
COPY ./loadtestclient/target/loadtestclient-0.0.1-SNAPSHOT.jar /home/apps/server
COPY ./loadtestclient/target/lib/ /home/apps/server/lib
ENTRYPOINT ["java", "-jar", "/home/apps/server/loadtestclient-0.0.1-SNAPSHOT.jar"]
