### 如何手动下载gradle？



1、gradle-wrapper

- gradle-wrapper 是管理 gradle 的插件

- gradle-wrapper.properties

  ```
  distributionBase=GRADLE_USER_HOME
  distributionPath=wrapper/dists
  
  // 基本路径 /Users/TaoWang/.gradle/
  zipStoreBase=GRADLE_USER_HOME
  
  // 相对路径 wrapper/dists
  zipStorePath=wrapper/dists
  
  // gradle的下载地址
  distributionUrl=https\://services.gradle.org/distributions/gradle-6.1.1-all.zip
  ```

- 如果 gradle 下载慢，可以浏览器直接粘添 “gradle的下载地址” 下载。下载完后放入 `/Users/TaoWang/.gradle/wrapper/dists/gradle-6.1.1-all/cfmwm155h49vnt3hynmlrsdst` ，再次 Sync Project 即可。

