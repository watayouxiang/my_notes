# AndroidStudio上运行Groovy(20190614)

## 1、新建Java Library

- File - New Module - Java Library

## 2、修改module下的`build.gradle`

```
//引入groovy插件，groovy插件继承了java插件
apply plugin: 'groovy'

dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    //引入Gradle所带的Groovy库
    implementation localGroovy()
}
	
task renameGroovyToJava {
    doLast {
        delete "$buildDir/classes/java"
        File file = new File("$buildDir/classes/groovy")
        println file.renameTo("$buildDir/classes/java")
    }
}
	
compileJava.finalizedBy compileGroovy
compileGroovy.finalizedBy renameGroovyToJava
```

## 3、测试Groovy程序
### Java式Groovy

- 新建`GroovyDemo01.groovy`

	```
	class GroovyDemo01 {
	    static void main(String[] args) {
	        println("hi Groovy")
	    }
	}
	
	```
- 点击绿色箭头，即可运行

### 脚本式Groovy

- 新建`GroovyDemo02.groovy`

	```
	def word = "hi groovy script"
	println(word)
	
	```
- 配置groovy脚本运行方式
	- Edit Configurations
	- + Groovy
	- 输入 Name，选择 Script path
	- Apply and OK
- 运行

## 4、GroovyConsole的使用

- 点击 Tools - Groovy Console
- 此时会出现 “Groovy的控制台” 和 “Groovy的文本编辑框”
- “Groovy的文本编辑框” 中输入 `System.out.println("hi~")`，点击左上角运行按钮
- 输出结果则会在 “Groovy的控制台” 显示