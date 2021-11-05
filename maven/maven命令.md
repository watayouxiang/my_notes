# maven命令



## 上传本地aar的包到私库

```
mvn deploy:deploy-file 
// 包路径
-Dfile=/Users/TaoWang/Documents/Code/tiocloud/tio-chat-android/common-silent/libs/liveness-silent-offline-cn-release.aar
// 项目组 id
-DgroupId=com.payease
// 项目名字 
-DartifactId=liveness-silent-offline-cn-release
// 版本 
-Dversion=1.0.0
// 包类型 
-Dpackaging=aar
// 私库地址  
-Durl=file:///Users/TaoWang/Documents/Code/tiocloud/tio-chat-android/libs/
```



## 安装本地aar包到本地仓库

 ```
mvn install:install-file 
// 包路径
-Dfile=/Users/TaoWang/Documents/Code/tiocloud/tio-chat-android/common-silent/libs/liveness-silent-offline-cn-release.aar
// 项目组id 
-DgroupId=com.payease
// 项目名字 
-DartifactId=liveness-silent-offline-cn-release
// 版本 
-Dversion=1.0.0
// 包类型 
-Dpackaging=aar
// 是否创建 pom 文件 
-DgeneratePom=true 
 ```

