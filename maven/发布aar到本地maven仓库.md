# 发布aar到本地maven仓库

> 文章内容总结自：http://www.voidcn.com/article/p-gybvqcqf-bon.html
>
> Maven系列（一）-- maven仓库的搭建：https://www.cnblogs.com/zhengjunfei/p/12951859.html
>
> Maven系列（二）-- 将项目上传到maven仓库私服：https://www.cnblogs.com/zhengjunfei/p/12977128.html
>
> 
>
> 发布到maven仓库：
>
> 1、发布到jcenter或maven； 
>
> 2、发布到自己搭建的maven或公司内部maven服务器上； 
>
> 3、发布到本地maven库；



## 1、在gradle中添加task，用于生成库



### 方式一

```
// 1.maven-插件
apply plugin: 'maven'
// 2.maven-信息
ext {// ext is a gradle closure allowing the declaration of global properties
    PUBLISH_GROUP_ID = 'com.watayouxiang'
    PUBLISH_ARTIFACT_ID = 'imclient'
    PUBLISH_VERSION = '1.1.2'
}
// 3.maven-路径
uploadArchives {
    repositories.mavenDeployer {
        String env = getAndroidSdkDir()
        println("androidSdkDir = ${env}")

        def deployPath = file(env + "/extras/android/m2repository/")
        String repository_url = "file://${deployPath.absolutePath}"
        println("repository_url = ${repository_url}")

        repository(url: repository_url)
        pom.project {
            groupId project.PUBLISH_GROUP_ID
            artifactId project.PUBLISH_ARTIFACT_ID
            version project.PUBLISH_VERSION
        }
    }
}

// get android sdk dir
String getAndroidSdkDir() {
    def rootDir = project.rootDir
    def androidSdkDir = null
    String envVar = System.getenv("ANDROID_HOME")
    def localProperties = new File(rootDir, 'local.properties')
    String systemProperty = System.getProperty("android.home")
    if (envVar != null) {
        androidSdkDir = envVar
    } else if (localProperties.exists()) {
        Properties properties = new Properties()
        localProperties.withInputStream { instr ->
            properties.load(instr)
        }
        def sdkDirProp = properties.getProperty('sdk.dir')
        if (sdkDirProp != null) {
            androidSdkDir = sdkDirProp
        } else {
            sdkDirProp = properties.getProperty('android.dir')
            if (sdkDirProp != null) {
                androidSdkDir = (new File(rootDir, sdkDirProp)).getAbsolutePath()
            }
        }
    }
    if (androidSdkDir == null && systemProperty != null) {
        androidSdkDir = systemProperty
    }
    if (androidSdkDir == null) {
        throw new RuntimeException(
                "Unable to determine Android SDK directory.")
    }
    androidSdkDir
}
```

### 方式二

```
// upload local repository
apply plugin: 'maven'
ext {
    PUBLISH_GROUP_ID = 'com.watayouxiang'
    PUBLISH_ARTIFACT_ID = 'imclient'
    PUBLISH_VERSION = '1.1.2'
}
uploadArchives {
    repositories.mavenDeployer {
        repository(url: uri('../libs'))
        pom.project {
            groupId project.PUBLISH_GROUP_ID
            artifactId project.PUBLISH_ARTIFACT_ID
            version project.PUBLISH_VERSION
        }
    }
}
```



## 2、执行task：uploadArchives



## 3、app添加依赖



使用aar的方式有两种：

1. implementation 'name 2.0'
   - 这种依赖方式，将会有依赖传递。（一般来说使用这种）
2. implementation 'name 2.0@aar'
   - 这种依赖方式，将不会有依赖传递



```
implementation 'com.watayouxiang:imclient:1.1.2'
```

## 4、添加maven仓库地址

### 方式一

```
allprojects {
    repositories {
        maven { url uri('/Users/TaoWang/Library/Android/sdk/extras/android/m2repository') }
    }
}
```

### 方式二

```
allprojects {
    repositories {
        maven { url uri('../libs') }
    }
}
```

