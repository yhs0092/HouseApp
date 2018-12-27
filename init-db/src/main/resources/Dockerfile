from swr.cn-north-1.myhuaweicloud.com/servicestage/mysqlutf8:0.2
COPY ./init-db/target/sql /sql
COPY ./init-db/target/entrypoint.sh /
RUN chmod +x /entrypoint.sh
ENTRYPOINT ["./entrypoint.sh"]
CMD ["--help"]
