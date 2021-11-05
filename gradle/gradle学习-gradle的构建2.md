## 监听构建生命周期回调

- 在 setting.gradle 配置如下代码监听构建生命周期回调

- Terminal中输入 `./gradlew clean`

```java
gradle.buildStarted {
    println "项目构建开始..."
}

// 1、初始化阶段：执行项目根目录下的 setting.gradle 文件，分析哪些 project 参与本次构建
gradle.projectsLoaded {
    println "从 setting.gradle 解析完成参与构建的所有项目"
}

// 2、配置阶段：加载所有参与本次构建项目下的 build.gradle 文件，会将 build.gradle 文件解析
//    并实例化为一个 Gradle 的 Project 对象，然后分析 Project 之间的依赖关系，分析 Project 下的
//    Task 之间的依赖关系，生成有向无环拓扑结构图 TaskGraph
gradle.beforeProject { proj ->
    println "${proj.name} build.gradle 解析之前"
}
gradle.afterProject { proj ->
    println "${proj.name} build.gradle 解析完成"
}
gradle.projectsEvaluated {
    println "所有项目的 build.gradle 解析配置完成"
}

// 3、执行阶段：这是 Task 真正被执行的阶段，Gradle 会根据依赖关系决定哪些 Task 需要被执行，以及执行的先后顺序。
//    Task 是 Gradle 中的最小执行单元，我们所有的构建、编译、打包、debug 都是执行了一个或者多个 Task，
//    一个 Project 可以有多个 Task，Task 之间可以互相依赖。
gradle.getTaskGraph().addTaskExecutionListener(new TaskExecutionListener() {
    @Override
    void beforeExecute(Task task) {
        println("任务执行：start" + task.name)
    }

    @Override
    void afterExecute(Task task, TaskState taskState) {
        println("任务执行：end" + task.name)
    }
})

gradle.buildFinished {
    println "项目构建结束..."
}
```



## 打印构建阶段task依赖关系及输出输入

- 在根 project 的 build.gradle 配置如下

```java
// 打印构建阶段task依赖关系及输出输入
afterEvaluate { project ->
    // 收集所有project的task集合
    Map<Project, Set<Task>> allTasks = project.getAllTasks(true)
    // 遍历每一个project下的task集合
    allTasks.entrySet().each { projTask ->
        projTask.value.each { task ->
            // 输出task的名称 和dependOn依赖
            System.out.println(task.getName())
            for (Object o : task.getDependsOn()) {
                System.out.println("dependOn-->" + o.toString())
            }

            // 打印每个任务的输入，输出
            for (File file : task.getInputs().getFiles().getFiles()) {
                System.out.println("input-->" + file.getAbsolutePath())
            }
            for (File file : task.getOutputs().getFiles().getFiles()) {
                System.out.println("output-->" + file.getAbsolutePath())
            }

            System.out.println("----------------------------------------")
        }
    }
}

// 相当于上面写法
this.project.afterEvaluate {
}
```



## Project工程树

- RootProject (AsProj)
  - SubProject (biz_home)
  - SubProject (biz_detail)
  - SubProject (service_provider)
    - SubProject (app_module)
    - SubProject (service_module)
    - SubProject (core_module)

> 1、此时 service_provider 称为下面三个 project 的 parentProject
>
> 2、RootProject 永远指的是 ASPProj


