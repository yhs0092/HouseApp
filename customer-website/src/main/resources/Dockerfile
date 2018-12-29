FROM swr.cn-north-1.myhuaweicloud.com/hwcse/dockerhub-java:8-jre-alpine
RUN mkdir -p /home/apps/server
ADD https://swr.cn-north-1.myhuaweicloud.com/swr/v2/domains/servicestage/namespaces/servicestage/repositories/HouseApp/packages/tomcat/versions/8.5.24/file_paths/apache-tomcat-8.5.24.tar.gz /home/apps/server
RUN tar zxf /home/apps/server/apache-tomcat-8.5.24.tar.gz -C /home/apps/server
RUN rm -rf /home/apps/server/apache-tomcat-8.5.24/webapps/*
COPY ./customer-website/target/customer-website-0.0.1-SNAPSHOT.war /home/apps/server/apache-tomcat-8.5.24/webapps/ROOT.war
COPY ./customer-website/target/server.xml /home/apps/server/apache-tomcat-8.5.24/conf
RUN ["chmod", "+x", "/home/apps/server/apache-tomcat-8.5.24/bin/catalina.sh"]
CMD ["/home/apps/server/apache-tomcat-8.5.24/bin/catalina.sh","run"]