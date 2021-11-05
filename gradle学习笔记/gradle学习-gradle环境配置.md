## gradle环境配置

> Mac OS下配置Gradle环境变量: https://www.jianshu.com/p/454946370560



下载地址：https://gradle.org/releases/

```
// 进入到目录
$ cd ~/Downloads

// 查看文件
$ ls

// 解压到 ~/gradle 目录下
$ unzip gradle-6.8.3-bin.zip -d ~/gradle

// 打开文件夹
$ open ~/gradle

// 清空控制台内容
$ clear

// 查看当前环境变量有哪些
$ echo $PATH

// 配置环境变量
$ open ~/.bash_profile

// 添加如下
------------------------------------------
# Gradle
GRADE_HOME=/Users/TaoWang/.gradle/wrapper/dists/gradle-6.5-all/2oz4ud9k3tuxjg84bbf55q0tn/gradle-6.5
export GRADE_HOME
export PATH=${PATH}:/Users/TaoWang/.gradle/wrapper/dists/gradle-6.5-all/2oz4ud9k3tuxjg84bbf55q0tn/gradle-6.5/bin 
# Gradle END
------------------------------------------

// 环境变量立即生效
$ source ~/.bash_profile

// 查看所有环境变量
$ echo $PATH

// 检验gradle是否配置成功
$ gradle -v
$ gradle -version
```

