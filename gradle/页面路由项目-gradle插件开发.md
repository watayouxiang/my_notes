## gradle插件开发步骤



### 1、建立插件工程，实现插件内部逻辑

- 建立 buildSrc 目录

- 建立 build.gradle

  - ```
    // 引用 groovy 插件，编译插件工程中的代码
    apply plugin: 'groovy'
    
    // 声明仓库的地址
    repositories {
        jcenter()
    }
    
    // 声明依赖的包
    dependencies {
        implementation gradleApi()
        implementation localGroovy()
    }
    ```

- 建立 src/main/groovy/com.imooc.router.gradle.RouterExtension

  - ```
    class RouterExtension {
        String wikiDir
    }
    ```

- 建立 src/main/groovy/com.imooc.router.gradle.RouterPlugin

  - ```
    class RouterPlugin implements Plugin<Project> {
        // 实现apply方法，注入插件的逻辑
        @Override
        void apply(Project project) {
            println("i am from RouterPlugin, apply from ${project.name}")
    
            // 创建 Extension
            project.getExtensions().create("router", RouterExtension)
    
            // 获取 Extension
            project.afterEvaluate {
                RouterExtension extension = project["router"]
                println("用户设置的 wikiDir 路径：${extension.wikiDir}")
            }
        }
    }
    ```

- 建立 resources/gradle-plugins/com.imooc.router.properties

  - ```
    implementation-class=com.imooc.router.gradle.RouterPlugin
    ```



### 2、发布插件到本地maven仓库

- 拷贝一份插件工程 buildSrc，重命名为 router-gradle-plugin

  - ```
    // 将 buildSrc 复制一份，名字叫 router-gradle-plugin
    $ cp -rf buildSrc router-gradle-plugin
    ```

- router-gradle-plugin 工程的 build.gradle 添加如下

  - ```
    // 调用 maven 插件，用于发布自己的插件
    apply plugin: 'maven'
    
    // 配置 maven 插件中的 uploadArchives 任务
    uploadArchives {
        repositories {
            mavenDeployer {
                // 设置发布路径为 工程根目录下面的 repo 文件夹
                repository(url: uri('../repo')) {
                    // 设置groupId，通常为包名
                    pom.groupId = 'com.imooc.router'
                    // 设置artifactId，为当前插件的名称
                    pom.artifactId = 'router-gradle-plugin'
                    // 设置插件版本号
                    pom.version = '1.0.0'
                }
            }
        }
    }
    
    // 执行发布命令：terminal 中输入
    // $ ./gradlew :router-gradle-plugin:uploadArchives
    ```



### 3、使用插件



#### 1）使用buildSrc中的插件

- 未发布的插件指 buildSrc 目录中的插件


- app module 的 build.gradle 中写入

  - ```
    // 应用自己的插件
    apply plugin: 'com.imooc.router'
    
    // 向路由插件传递参数
    router {
        wikiDir getRootDir().absolutePath
    }
    ```




#### 2）使用发布到本地maven仓库的插件

- 根目录 build.gradle 添加如下

  - ```
    buildscript {
        // 插件所在的仓库
        repositories {
            /**
             * 配置maven仓库地址
             * 这里可以是相对路径地址，也可以是绝对路径地址
             */
            maven {
                url uri("/Users/TaoWang/Documents/Code/github/Android/repo")
            }
    
            google()
            jcenter()
        }
    
        // gradle 插件
        dependencies {
            classpath 'com.android.tools.build:gradle:4.1.3'
    
            /**
             * 声明依赖的插件
             * 形式是：groupId : artifactId : version
             */
            classpath 'com.imooc.router:router-gradle-plugin:1.0.0'
        }
    }
    
    allprojects {
        // 工程依赖所在的仓库
        repositories {
            /**
             * 配置maven仓库地址
             * 这里可以是相对路径地址，也可以是绝对路径地址
             */
            maven {
                url uri("/Users/TaoWang/Documents/Code/github/Android/repo")
            }
    
            google()
            jcenter()
        }
    }
    ```
- app module 的 build.gradle 中写入

  - ```
    // 应用自己的插件
    apply plugin: 'com.imooc.router'
    
    // 向路由插件传递参数
    router {
        wikiDir getRootDir().absolutePath
    }
    ```

