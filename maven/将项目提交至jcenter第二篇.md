# 将项目提交至jcenter第二篇

> 参考：[https://github.com/panpf/android-library-publish-to-jcenter](https://github.com/panpf/android-library-publish-to-jcenter)

## 简介

- 第一篇讲的是用 `com.novoda:bintray-release` 依赖库方式提交项目到jcenter。
- 第二篇讲的是用 `com.jfrog.bintray.gradle:gradle-bintray-plugin` 插件形式提交项目到jcenter。

## 步骤

### 1.project gradle中引入

```
buildscript {
    repositories {
        ...
    }
    dependencies {
        ...
           
        //插件是用来打包Maven所需文件的
        classpath 'com.github.dcendents:android-maven-gradle-plugin:2.1'
        //插件是用来将生成的 Maven 所需文件上传到 Bintray 的
        classpath 'com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4'
    }
}
```

### 2.添加`project.properties`到module gradle同级目录下

```
#project
#项目名称
project.name=AndroidUtils
#项目组ID，通常情况下如果你的包名为 com.example.test，那么项目组 ID 就是 com.example
project.groupId=com.watayouxiang
#项目ID，通常情况下如果你的包名为 com.example.test，那么项目 ID 就是 test
project.artifactId=AndroidUtils
#包类型，Android 库是 aar
project.packaging=aar
#项目官方网站的地址，没有的话就用 Github 上的地址，例如：https://github.com/xiaopansky/Sketch
project.siteUrl=https://github.com/watayouxiang/AndroidUtils
#项目的 Git 地址，例如：https://github.com/xiaopansky/Sketch.git
project.gitUrl=https://github.com/watayouxiang/AndroidUtils.git

#javadoc
#生成的 javadoc 打开后主页显示的名称，通常跟项目名称一样即可
javadoc.name=AndroidUtils
```

### 3.添加`local.properties`到module gradle同级目录下

```
#bintray
#你的 Bintray 的用户名
bintray.user=
#你的的 Bintray 的 API Key
bintray.apikey=

#developer
#通常是你在开源社区的昵称
developer.id=
#你的姓名
developer.name=
#你的邮箱
developer.email=
```

注意：local.properties文件内容属于私密信息，务必要忽略提交该文件！！！

### 4.添加`bintrayUpload.gradle`到module gradle同级目录下

#### 方式一：使用远程 bintrayUpload.gradle 文件

在 module 的 build.gradle 文件的最后添加

```
apply plugin: 'com.android.library'

android {
    ...
}

apply from: "https://raw.githubusercontent.com/panpf/android-library-publish-to-jcenter/master/bintrayUpload.gradle"
```

#### 方式二：使用本地 bintrayUpload.gradle 文件

1）bintrayUpload.gradle文件内容：

```
apply plugin: 'com.github.dcendents.android-maven'
apply plugin: 'com.jfrog.bintray'

// load properties
Properties properties = new Properties()
File localPropertiesFile = project.file("local.properties");
if(localPropertiesFile.exists()){
    properties.load(localPropertiesFile.newDataInputStream())
}
File projectPropertiesFile = project.file("project.properties");
if(projectPropertiesFile.exists()){
    properties.load(projectPropertiesFile.newDataInputStream())
}

// read properties
def projectName = properties.getProperty("project.name")
def projectGroupId = properties.getProperty("project.groupId")
def projectArtifactId = properties.getProperty("project.artifactId")
def projectVersionName = android.defaultConfig.versionName
def projectPackaging = properties.getProperty("project.packaging")
def projectSiteUrl = properties.getProperty("project.siteUrl")
def projectGitUrl = properties.getProperty("project.gitUrl")

def developerId = properties.getProperty("developer.id")
def developerName = properties.getProperty("developer.name")
def developerEmail = properties.getProperty("developer.email")

def bintrayUser = properties.getProperty("bintray.user")
def bintrayApikey = properties.getProperty("bintray.apikey")

def javadocName = properties.getProperty("javadoc.name")

group = projectGroupId

// This generates POM.xml with proper parameters
install {
    repositories.mavenInstaller {
        pom {
            project {
                name projectName
                groupId projectGroupId
                artifactId projectArtifactId
                version projectVersionName
                packaging projectPackaging
                url projectSiteUrl
                licenses {
                    license {
                        name 'The Apache Software License, Version 2.0'
                        url 'http://www.apache.org/licenses/LICENSE-2.0.txt'
                    }
                }
                developers {
                    developer {
                        id developerId
                        name developerName
                        email developerEmail
                    }
                }
                scm {
                    connection projectGitUrl
                    developerConnection projectGitUrl
                    url projectSiteUrl
                }
            }
        }
    }
}

// This generates sources.jar
task sourcesJar(type: Jar) {
    from android.sourceSets.main.java.srcDirs
    classifier = 'sources'
}

task javadoc(type: Javadoc) {
    source = android.sourceSets.main.java.srcDirs
    classpath += project.files(android.getBootClasspath().join(File.pathSeparator))
}

// Add compile dependencies to javadoc
afterEvaluate {
    javadoc.classpath += files(android.libraryVariants.collect { variant ->
        variant.javaCompile.classpath.files
    })
}

// This generates javadoc.jar
task javadocJar(type: Jar, dependsOn: javadoc) {
    classifier = 'javadoc'
    from javadoc.destinationDir
}

artifacts {
    archives javadocJar
    archives sourcesJar
}

// javadoc configuration
javadoc {
    options{
        encoding "UTF-8"
        charSet 'UTF-8'
        author true
        version projectVersionName
        links "http://docs.oracle.com/javase/7/docs/api"
        title javadocName
    }
}

// bintray configuration
bintray {
    user = bintrayUser
    key = bintrayApikey
    configurations = ['archives']
    pkg {
        repo = "maven"
        name = projectName
        websiteUrl = projectSiteUrl
        vcsUrl = projectGitUrl
        licenses = ["Apache-2.0"]
        publish = true
    }
}
```

2）module gradle文件中添加

```
apply plugin: 'com.android.library'

android {
    ...
}

apply from: "bintrayUpload.gradle"
```
### 5.打包并上传到 Bintray

- 如果你的本地已经配置了 Gradle 了，那么执行 gradle bintrayUpload 命令即可。
	- Gradle -> 你要提交的module -> Task -> publishing -> 点击 bintrayUpload
	- 因为gradlew 是 Gradle 的一层封装，如果你本地没有安装 Gradle gradlew 就会自动下载 Gradle。


