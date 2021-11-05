## 插件分类和使用

- 二进制插件

  - 平时使用的android插件就是二进制插件

  - ```
    //////////////////////////////////////
    // 二进制插件的使用
    //////////////////////////////////////
    
    // 1、引用插件
    // 在 project 的 build.gradle 中
    buildscript { 
        // 插件所在的仓库
        repositories {
            google()
            jcenter()
        }
    
        // gradle 插件
        dependencies {
            // 声明插件的 ID 和 版本号
            classpath 'com.android.tools.build:gradle:4.1.2'
        }
    }

    // 2、应用插件
    // 在 module 的 build.gradle 中
    apply plugin: 'com.android.application'
    
    // 3、配置插件
    // 在 module 的 build.gradle 中
    android {
        compileSdkVersion 30
        buildToolsVersion "30.0.1"
    
        defaultConfig {
            applicationId "com.watayouxiang.androiddemo"
            minSdkVersion 16
            targetSdkVersion 30
            versionCode 1
            versionName "1.0"
        }
    
        buildTypes {
            release {
                minifyEnabled false
                proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
            }
        }
    }
    ```
    

- 脚本插件
  
  - 一个独立gradle脚本
  
  - ```
    //////////////////////////////////////
    // 脚本插件的使用
    //////////////////////////////////////
    
    // 1、编写脚本插件
    // 在工程根目录下，新建 other.gradle，内容如下
    println("我是脚本插件的代码")
    
    // 2、应用脚本插件
    // 在 module 的 build.gradle 中应用，内容如下
    apply from: project.rootProject.file("other.gradle")
    
    // 3、运行插件
    $ ./gradlew clean -q
    ```



## 插件的开发流程

- 建立插件工程
- 实现插件内部逻辑
- 发布与使用插件