## Netflix Eureka 服务控制台

[源文件需要编译: http://git.genj.com.cn/dev/captain/web](//git.genj.com.cn/dev/captain/web) 


编译后替换：resources/static 即可

控制台（进度 10%）:
![image text](//git.genj.com.cn/dev/static/blob/master/img/20200921175532.png)

Maven 发布：

修改版本：mvn versions:set -DnewVersion=1.0.20201210

上传仓库：

Goals: clean deploy -P release -projects netflix_common,netflix_dashboard,netflix_netty_http,netflix_zuul_http

Profiles: oss

Parameter: gpg.passphrase=[password]

```
maven settings.xml 文件到wshun这来取

```