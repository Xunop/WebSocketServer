FROM xun_jdk19:1.0.0
MAINTAINER xun
# 设置时区
ENV TZ="Asia/Shanghai"
# VOLUME 指定临时文件目录为 /tmp
VOLUME /tmp
# 将 jar 放入容器内
ADD schedule-0.0.1-SNAPSHOT.jar .
# 启动服务，nohup 意思是不挂断运行命令,当账户退出或终端关闭时,程序仍然运行
ENTRYPOINT ["nohup","java","--enable-preview","-jar","/schedule-0.0.1-SNAPSHOT.jar","&", "--spring.profiles.active=pro"]
EXPOSE 8888
