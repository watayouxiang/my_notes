```
// 控制台打印结果，“-q” 代表过滤日志
// ./gradlew clean -q

////////////////////////////////////////////////////////
// 闭包
////////////////////////////////////////////////////////

def c = { println("hello Closure") }
c()

// 有一个参数的时候，参数可以忽略，默认名称为 it
def c2 = { println("c2: it = $it") }
c2("watayouxiang")

def c4 = { name1, name2 ->
    println("c4: name1 = $name1")
    println("c4: name2 = $name2")
}
c4("test_1", "test_2")

def list = [1, 3, 5, 7, 9]
list.each { println("item = $it") }

////////////////////////////////////////////////////////
// DSL
////////////////////////////////////////////////////////

// 实现自定义 DSL
def myAndroid = {
    compileSdkVersion 27
    defaultConfig {
        versionName "1.0"
    }
}

Android a = new Android()
// 将闭包与具体对象关联起来
myAndroid.delegate = a
myAndroid.call()

println("myAndroid = $a")

class DefaultConfig {
    private String versionName

    def versionName(String versionName) {
        this.versionName = versionName
    }

    @Override
    String toString() {
        return "DefaultConfig{ versionName = $versionName }"
    }
}

class Android {
    private int compileSdkVersion
    private DefaultConfig defaultConfig

    Android() {
        this.defaultConfig = new DefaultConfig()
    }

    def compileSdkVersion(int compileSdkVersion) {
        this.compileSdkVersion = compileSdkVersion
    }

    def defaultConfig(Closure closure) {
        // 将闭包和具体对象关联起来
        closure.setDelegate(defaultConfig)
        closure.call()
    }

    @Override
    String toString() {
        return "Android { compileSdkVersion = $compileSdkVersion, " +
                "defaultConfig = $defaultConfig }"
    }
}
```