## gradle构建脚本基础



- setting.gradle
  - 声明当前project包含了哪些module
- build.grade
  - project 下的 build.gradle
    - 所有子工程都可以共用的配置
  - module 下的 build.gradle
    - 针对当前子工程的构建行为
- grade.properties
  - 配置开关型参数的文件



## gradle构建的生命周期

- 初始化阶段
  - 收集本次参加构建的所有子工程，创建一个项目的层次结构，为每一个项目创建一个project实例
  
  - ```
    // 在 setting.gradle 文件添加如下代码
    // 添加 构建的生命周期 监听
    gradle.addBuildListener(new BuildAdapter(){
        @Override
        void settingsEvaluated(Settings settings) {
            super.settingsEvaluated(settings)
            println("project 初始化阶段完成")
        }
    
        @Override
        void projectsEvaluated(Gradle gradle) {
            super.projectsEvaluated(gradle)
            println("project 配置阶段完成")
        }
    
        @Override
        void buildFinished(BuildResult result) {
            super.buildFinished(result)
            println("project 构建结束")
        }
    })
    
    // terminal中执行
    $./gradlew clean -q
    
    // 可以得出结论：setting.gradle 是在 初始化阶段之前 执行的
    ```
  
- 配置阶段
  
  - 执行各个module下的build.gradle脚本，来完成project对象的配置。并且根据项目自己的配置去构建出一个项目依赖图，以便在下一个执行阶段去执行
  
  - ```
    // 在project的build.gradle中写入
    println("我是project的build.gradle")
    
    // 在module的build.gradle中写入
    println("我是module的build.gradle")
    
    // terminal中执行
    $ ./gradlew clean -q
    
    // 可以得出结论：“project的build.gradle” 和 “module的build.gradle” 是在 初始化阶段之后，配置阶段之前 执行的
    ```
  
  - 
  
- 执行阶段

  - 把配置阶段生成的一个任务依赖图，依次去执行

  - ```
    // 在app module的build.gradle中写入
    task testTask() {
    	println("我是app module中的任务")
    }
    
    // terminal中执行
    $ ./gradlew :app:testTask -q
    
    // 可以得出结论：task是在 配置阶段之后，构建结束之前 执行的
    ```



## gradle几个主要角色



- 初始化阶段 - rootProject

  - 在初始化阶段之前，就能拿到 rootProject 对象了

  - ```
    // 在setting.gradle写入
    println("我的项目路径：${rootProject.projectDir}")
    
    $ ./gradlew clean -q
    ```

- 配置阶段 - project

  - 在配置阶段完成后，能拿到 project 对象

  - ```
    // 在 setting.gradle 中使用 project对象
    gradle.addBuildListener(new BuildAdapter(){
        @Override
        void projectsEvaluated(Gradle gradle) {
            super.projectsEvaluated(gradle)
            println("project 配置阶段完成")
            
            gradle.rootProject.childProjects.each {	name, proj ->
            	println("module名称是 $name, 路径是 ${proj.getProjectDir()}")
            }
        }
    })
    
    // 执行如下命令
    $ ./gradlew clean -q
    ```

  - ```
    // 在 app module 的 build.gradle 中使用 project对象
    println("我是app module，我的路径是：${project.projectDir}")
    
    // 执行如下命令
    $ ./gradlew clean -q
    ```

- 执行阶段 - task

  - gradle最小的执行单元，一个project可以有多个task，task之间可以相互依赖的，靠相互依赖的关系来串成一个有向无环图

  - 执行任务

    - ```
      // 在app module 的 build.gradle中写入
      task testTask(){
      	doLast{
      		println(“我是 testTask 任务”)
      	}
      }
      
      $ ./gradlew :app:testTask -q
      ```

  - 任务的依赖

    - ```
      // 在app module 的 build.gradle中写入
      task testTask(){
      	doLast{
      		println(“我是 testTask 任务”)
      	}
      }
      
      task test2(){
      	dependsOn testTask
      	doLast{
      		println(“我是 testTask 任务”)
      	}
      }
      
      $ ./gradlew :app:test2 -q
      
      // 结论：test2 依赖于 testTask
      ```