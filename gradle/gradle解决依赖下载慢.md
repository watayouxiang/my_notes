### 依赖下载慢怎么办？



替换依赖所在的仓库。

可以使用国内的仓库，例如：阿里云



```groovy
repositories {
    maven {
        // 阿里云maven：https://maven.aliyun.com/mvn/view
        url 'https://maven.aliyun.com/repository/central'
    }
}
```

