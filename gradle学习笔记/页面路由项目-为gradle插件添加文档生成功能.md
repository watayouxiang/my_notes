# 为gradle插件添加文档生成功能



## 思路解析

1、将路径参数 rot_project_dir 传递到 kapt 注解处理器中。从而避免需要手动在app module的 build.gradle 中配置如下信息。

```
kapt {
	arguments {
		arg("root_project_dir", rootProject.projectDir.absolutePath)
	}
}
```

2、实现旧的构建产物的自动清理：删除上一次构建生成的 router_mapping 目录

3、在 javac 任务 (compileDebugJavaWithJavac) 后，汇总生成文档

```
// 可以在 setting.gradle 中打印本次构建执行的所有任务的名称，从而知晓 javac 任务的名称
gradle.taskGraph.beforeTask { task ->
 println("[all-task] " + task.name)
}

// $ ./gradlew :androiddemo:assembleDebug -q
```



## 编码内容

- router-gradle-plugin 项目 com.imooc.router.gradle.RouterPlugin 编码如下：

  - ```
    class RouterPlugin implements Plugin<Project> {
      // 实现apply方法，注入插件的逻辑
      @Override
      void apply(Project project) {
    
          // 1、自动帮助用户传递路径参数到注解处理器中
          //     kapt {
          //        arguments {
          //            arg("root_project_dir", rootProject.projectDir.absolutePath)
          //        }
          //    }
          if (project.extensions.findByName("kapt") != null) {
              project.extensions.findByName("kapt").arguments {
                  arg("root_project_dir", project.rootProject.projectDir.absolutePath)
              }
          }
    
          // 2、实现旧的构建产物的自动清理
          project.clean.doFirst {
              // 删除上一次构建生成的 router_mapping 目录
              File routerMappingDir = new File(project.rootProject.projectDir, "router_mapping")
              if (routerMappingDir.exists()) {
                  routerMappingDir.deleteDir()
              }
          }
    
          println("i am from RouterPlugin, apply from ${project.name}")
    
          // 创建 Extension
          project.getExtensions().create("router", RouterExtension)
    
          // 获取 Extension
          project.afterEvaluate {
              RouterExtension extension = project["router"]
              println("用户设置的 wikiDir 路径：${extension.wikiDir}")
    
              // 3、在 javac 任务 (compileDebugJavaWithJavac) 后，汇总生成文档
              project.tasks.findAll { task ->
                  task.name.startsWith('compile') && task.name.endsWith('JavaWithJavac')
              } each { task ->
                  task.doLast {
                      File routerMappingDir = new File(project.rootProject.projectDir, "router_mapping")
                      if (!routerMappingDir.exists()) {
                          return
                      }
                      File[] allChildFiles = routerMappingDir.listFiles()
                      if (allChildFiles.length < 1) {
                          return
                      }
    
                      StringBuilder markdownBuilder = new StringBuilder()
                      markdownBuilder.append("# 页面文档\n\n")
                      allChildFiles.each { child ->
                          if (child.name.endsWith(".json")) {
                              JsonSlurper jsonSlurper = new JsonSlurper()
                              def content = jsonSlurper.parse(child)
                              content.each { innerContent ->
                                  def url = innerContent['url']
                                  def description = innerContent['description']
                                  def realPath = innerContent['realPath']
                                  markdownBuilder.append("## $description\n")
                                  markdownBuilder.append("- url: $url\n")
                                  markdownBuilder.append("- realPath: $realPath\n\n")
                              }
                          }
                      }
    
                      File wikiFileDir = new File(extension.wikiDir)
                      if (!wikiFileDir.exists()) {
                          wikiFileDir.mkdir()
                      }
                      File wikiFile = new File(wikiFileDir, "页面文档.md")
                      if (wikiFile.exists()) {
                          wikiFile.delete()
                      }
                      wikiFile.write(markdownBuilder.toString())
                  }
              }
              
          }
      }
    }
    ```


- 测试 MD文档 是否生成

  - ```
    // 测试 MD文档 是否生成
    $ ./gradlew clean -q
    $ ./gradlew :androiddemo:assembleDebug -q
    ```

- 使用注意事项

  - ```
    // --------------------------
    // 在 app module 的 build.gradle 中，“router插件” 需要在 “kotlin插件” 之后声明使用
    // 
    // 因为：需要在 com.imooc.router.gradle.RouterPlugin 中获取 kapt 的 extension
    // 所以：“router插件” 需要在 “kotlin插件” 之后声明
    // --------------------------
    
    // kotlin 插件
    apply plugin: 'kotlin-android'
    apply plugin: 'kotlin-kapt'
    
    // 应用自己的插件
    apply plugin: 'com.imooc.router'
    router {
        wikiDir getRootDir().absolutePath
    }
    ```