# 将项目提交至jcenter

> 参考：[https://blog.csdn.net/ziwang_/article/details/76556621](https://blog.csdn.net/ziwang_/article/details/76556621)

### 1.使用bintray-release提交项目

- bintray-release 项目地址 [https://github.com/novoda/bintray-release/releases](https://github.com/novoda/bintray-release/releases)
- project 的 build.gradle 下添加：

```
buildscript {
    repositories {
        google()
        jcenter()
        
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.2.1'
        classpath 'com.novoda:bintray-release:0.9.1'//引入
    }
}
```

- module 的 build.gradle 下添加：

```
apply plugin: 'com.android.library'
apply plugin: 'com.novoda.bintray-release'//引入

publish {
    //bintray用户名
    userOrg = 'watayouxiang'
    //组id，建议使用：com.+用户名
    groupId = 'com.watayouxiang'
    //模块名称
    artifactId = 'DemoShell'
    //版本号
    publishVersion = '1.0.0'
    //项目描述
    desc = 'quick build your demo'
    //你的项目地址，可填 github 地址
    website = 'https://github.com/watayouxiang/DemoShell'
}

```

### 2.注册bintray账号，并创建仓库

- 注册地址 [https://bintray.com/signup/oss](https://bintray.com/signup/oss)
- 创建 maven 仓库

	```
	1. 头像处进入 View Profile
	2. 点击 "Add New Repository" 
		1) 勾选： Public - anyone can download your files.
		2) Name填：maven
		3) Type选择：Maven
	3. 点击创建即可
	```
- 复制 API Key
	
	```
	1. 头像处进入 Edit Profile
	2. 左侧选择 API Key 选项卡
	3. 输入框键入你的密码，即可查看你的 API key
	```
	
### 3.使用Terminal命令，上传项目到jcenter

```
//Windows:
gradlew clean build bintrayUpload -PbintrayUser=用户名 -PbintrayKey=APIKey -PdryRun=false

//Mac OS:
./gradlew clean build bintrayUpload -PbintrayUser=用户名 -PbintrayKey=APIKey -PdryRun=false
```

- 如果出现 BUILDSUCCESSFUL 则代表已经成功
- 此时，进入View Profile -> maven仓库，已经可以看到刚上传的项目。但是该库还不是公有的，需要手动将库从你目前的私人 repo 同步到 jcenter 中。

```
1. 打开你的 repo 并进入你的项目中，在如下的位置会有一个 add to jcenter 的按钮
2. 填写项目作用描述，点击 send 提交申请
```

