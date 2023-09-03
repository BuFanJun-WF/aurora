FROM amazoncorretto:8u382
# 创建文件夹
RUN mkdir /aurora-blog
RUN mkdir /aurora-blog/config

# 设置工作目录
WORKDIR /aurora-blog

COPY aurora-blog-backend.jar /aurora-blog/aurora-blog-backend.jar
# 暴露端口
EXPOSE 8080

# 设置环境变量：时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo '$TZ' > /etc/timezone

# docker run时执行的命令
CMD java -jar /aurora-blog/aurora-blog-backend.jar
