# publish2mavenCenter教程

> 参考文章：
>
> - 替换Jcenter，发布开源代码到 Sonatype Maven Central：https://blog.bihe0832.com/oss-sonatype.html



## 1、创建sonatype账号

> 与jCenter是由jForg运营，在bintray.com做管理一样，Maven Central是由sonatype运营的，那么首先需要去注册一个sonatype的账号并获得仓库使用许可。
>
> 
>
> sonatype账号注册地址：https://issues.sonatype.org/
>
> MavenCentral仓库地址：https://s01.oss.sonatype.org/
>
> sonatype Maven Central Repository Search: https://search.maven.org/



1. 前往 [https://issues.sonatype.org](https://issues.sonatype.org/) 注册账号
   - account: watayouxiang
   - pwd: xxxxxxxx
   - email: watayouxiang@qq.com
2. 提交一个Issue进行申请，新建 `Community Support - Open Source Project Repository Hosting`
   - 问题类型：New Project
   - 概要：项目名称
   - 描述：项目描述
   - Group Id: io.github.watayouxiang
   - Project URL: https://github.com/watayouxiang/StarUtils
   - SCM url: https://github.com/watayouxiang/StarUtils
   - Username(s): watayouxiang
   - Already Synced to Central: No
3. 申请后请耐心等待官方的回复，待issues状态变为已解决，那么就说明申请成功了



## 2、创建GPG密钥

```
// 安装gpg
$ brew install gpg

// 创建密钥
$ gpg --full-gen-key

// 加密方式：RSA 和 RSA
// 密钥的长度：4096
// 密钥的有效期限：密钥永不过期
// 真实姓名：watayouxiang
// 电子邮件地址：watayouxiang@qq.com
// 输入密码：xxxxxxxx
// 确认输入密码：xxxxxxxx

// -----------------------------------------------------------
gpg: /Users/TaoWang/.gnupg/trustdb.gpg：建立了信任度数据库
gpg: 密钥 B4AD0138D6C7312A 被标记为绝对信任
gpg: 目录‘/Users/TaoWang/.gnupg/openpgp-revocs.d’已创建
gpg: 吊销证书已被存储为‘/Users/TaoWang/.gnupg/openpgp-revocs.d/27C5FAB5ABA559949B0B7E23B4AD0138D6C7312A.rev’
公钥和私钥已经生成并被签名。

pub   rsa4096 2021-04-20 [SC]
      27C5FAB5ABA559949B0B7E23B4AD0138D6C7312A
uid   watayouxiang <watayouxiang@qq.com>
// -----------------------------------------------------------

// 把目录切到~/.gnupg/下
$ cd ~/.gnupg/

// 创建gpg文件
// 执行以上命令创建文件secring.gpg，创建过程会让输入上边我们创建密钥过程中输入的密码，验证完密码后会在~/.gnupg目录生成secring.gpg文件
$ gpg --export-secret-keys -o secring.gpg

// 注意：gpg公钥得上传到服务
```



## 3、编写gradle上传aar

### 1）创建上传脚本

- ../publish/local.properties

  ```
  signing.keyId=D6C7312A
  signing.password=xxxxxxxx
  signing.secretKeyRingFile=/Users/TaoWang/.gnupg/secring.gpg
  
  ossrhUsername=watayouxiang
  ossrhPassword=xxxxxxxx
  ```

- ../publish/publish2mavenCenter.gradle

  ```
  apply plugin: 'maven-publish'
  apply plugin: 'signing'
  
  // 源码文件
  task androidSourcesJar(type: Jar) {
      archiveClassifier.set("sources")
      from android.sourceSets.main.java.source
  
      exclude "**/R.class"
      exclude "**/BuildConfig.class"
  }
  
  ext["signing.keyId"] = ''
  ext["signing.password"] = ''
  ext["signing.secretKeyRingFile"] = ''
  ext["ossrhUsername"] = ''
  ext["ossrhPassword"] = ''
  
  File secretPropsFile = file("${rootDir.path}/publish/local.properties")
  if (secretPropsFile.exists()) {
      println "Found secret props file, loading props"
      Properties p = new Properties()
      p.load(new FileInputStream(secretPropsFile))
      p.each { name, value ->
          ext[name] = value
      }
  } else {
      println "No props file, loading env vars"
  }
  
  publishing {
      // 定义发布什么
      publications {
          release(MavenPublication) {
              groupId PUBLISH_GROUP_ID
              artifactId PUBLISH_ARTIFACT_ID
              version PUBLISH_VERSION
  
              // 发布的 arr 的文件
              artifact("$buildDir/outputs/aar/starutils-release.aar")
              // 发布的源码文件
              artifact androidSourcesJar
  
              pom {
                  // 构件名称
                  name = PUBLISH_ARTIFACT_ID
                  // 构件描述
                  description = 'starutils'
                  // 构件主页
                  url = 'https://github.com/watayouxiang'
                  // 许可证名称和地址
                  licenses {
                      license {
                          name = 'The Apache License, Version 2.0'
                          url = 'https://www.apache.org/licenses/LICENSE-2.0.txt'
                      }
                  }
                  // 开发者信息
                  developers {
                      developer {
                          id = 'watayouxiang'
                          name = 'TaoWang'
                          email = 'watayouxiang@qq.com'
                      }
                  }
                  // 版本控制仓库地址
                  scm {
                      url = 'https://github.com/watayouxiang/StarUtils'
                      connection = 'https://github.com/watayouxiang/StarUtils'
                      developerConnection = 'https://github.com/watayouxiang/StarUtils.git'
                  }
                  // 解决依赖关系
                  withXml {
                      def dependenciesNode = asNode().appendNode('dependencies')
  
                      project.configurations.implementation.allDependencies.each {
                          if (it.name != 'unspecified') {// 如果是本地依赖，那么忽略掉
                              def dependencyNode = dependenciesNode.appendNode('dependency')
                              dependencyNode.appendNode('groupId', it.group)
                              dependencyNode.appendNode('artifactId', it.name)
                              dependencyNode.appendNode('version', it.version)
                          }
                      }
                  }
              }
          }
      }
      // 定义发布到哪里
      repositories {
          maven {
              name = "sonatype"
  
              // 发布的位置，这里根据发布的版本区分了 SNAPSHOT 和最终版本两种情况
              def releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
              def snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
              url = version.endsWith('SNAPSHOT') ? snapshotsRepoUrl : releasesRepoUrl
  
              // 这里就是之前在 issues.sonatype.org 注册的账号
              credentials {
                  username ossrhUsername
                  password ossrhPassword
              }
          }
      }
  }
  
  signing {
      sign publishing.publications
  }
  ```

### 2）编辑module的build.gradle

```
ext {
    PUBLISH_GROUP_ID = 'io.github.watayouxiang'
    PUBLISH_ARTIFACT_ID = 'starutils'
    PUBLISH_VERSION = '0.0.1'
}
apply from: "${rootDir.path}/publish/publish2mavenCenter.gradle"
```

### 3）执行gradle上传任务

生成aar包：gradle/Task/build/assemble

发布aar包：gradle/Task/publishing/publishReleasePublishToMaventralRepository



## 4、发布aar

打开 Nexus Repository Manager (https://s01.oss.sonatype.org/) 

在左侧 `Staging Repositories` 页面找到你的 group id，选中，点击上边的close，等待5~10分钟后刷新状态，等其状态变为closed后，再点击Release，等待几个小时后可以在 [search.maven.org/](https://search.maven.org/) 查询发布结果，此时所有人都用使用你的库了。



