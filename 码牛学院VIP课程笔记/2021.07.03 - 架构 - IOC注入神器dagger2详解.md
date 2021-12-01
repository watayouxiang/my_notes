# IOC注入神器dagger2详解

> IOC（Inversion of Control）
>
> 是原来由程序代码中主动获取的资源，转变由第三方获取并使原来的代码被动接收的方式，以达到解耦的效果，称为控制反转

基本配置

```java
implementation 'com.google.dagger:dagger:2.40'
annotationProcessor 'com.google.dagger:dagger-compiler:2.40'
```

使用逻辑

- module: 用于提供对象

- component: 用于组织module并进行注入

## 基本使用

```java
// 1.提供用于注入的对象
public class HttpClient {
}
public class ImClient {
}

// 2.编写Module
@Module
public class HttpModule {
    @Provides
    public HttpClient provideHttpClient(){
        return new HttpClient();
    }
}
@Module
public class ImModule {
    @Provides
    public ImClient provideImClient(){
        return new ImClient();
    }
}

// 3.编写Component
@Component(modules = {HttpModule.class, ImModule.class})
public interface MainComponent {
    void injectMainActivity(MainActivity mainActivity);
}

// 4.注入到Activity
// 5.rebuild项目让APT生成需要的文件 
// 6.在需要注入的类中使用
public class MainActivity extends AppCompatActivity {
    @Inject
    HttpClient httpClient;
    
    @Inject
    ImClient imClient;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //1.语法1
        DaggerMainComponent.create().injectMainActivity(this);
        //2.语法2
        DaggerMainComponent.builder()
                .httpModule(new HttpModule())
                .imModule(new ImModule())
                .build()
                .injectMainActivity(this);
	}
}
```

## 单例使用

```java
// 1、该单例只能在注入类中局部有效
// 2、如果要实现局部单例，那么就要在 Component、Module 中都要标记 @Singleton
// 3、建议用自定义 Scope 注解（如@AppScope、@UserScope），来代替 @Singleton 实现局部单例

// --- 不建议的局部单例 ---
@Module
public class ImModule {
    @Singleton
    @Provides
    public ImClient provideImClient(){
        return new ImClient();
    }
}

@Singleton
@Component(modules = {HttpModule.class, ImModule.class})
public interface MainComponent {
    void injectMainActivity(MainActivity mainActivity);
}

// --- 推荐的局部单例 ---
@Scope
@Documented
@Retention(RUNTIME)
public @interface MainScope {
}

@Module
public class ImModule {
    @MainScope
    @Provides
    public ImClient provideImClient(){
        return new ImClient();
    }
}

@MainScope
@Component(modules = {HttpModule.class, ImModule.class})
public interface MainComponent {
    void injectMainActivity(MainActivity mainActivity);
}

// --- 全局单例 ---
public class MyApplication extends Application {
    private MainComponent mainComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        mainComponent = DaggerMainComponent.create();
    }

    public MainComponent getMainComponent() {
        return mainComponent;
    }
}
```

## 多个Component组合依赖

### 方式一：dependencies用法

```java
// 1.Scope
@Scope
@Documented
@Retention(RUNTIME)
public @interface DbScope {
}

// 2. module
public class DbClient {
}

@Module
public class DbModule {
    @DbScope
    @Provides
    public DbClient provideDbClient(){
        return new DbClient();
    }
}

// 3. component
@DbScope
@Component(modules = {DbModule.class})
public interface DbComponent {
    DbClient provideDbClient();
}

@MainScope
@Component(modules = {HttpModule.class, ImModule.class}, dependencies = {DbComponent.class})
public interface MainComponent {
    void injectMainActivity(MainActivity mainActivity);
}

// 4. 使用
public class MainActivity extends AppCompatActivity {
    @Inject
    HttpClient httpClient;

    @Inject
    ImClient imClient;

    @Inject
    DbClient dbClient;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DaggerMainComponent.builder()
                .httpModule(new HttpModule())
                .imModule(new ImModule())
                .dbComponent(DaggerDbComponent.create())
                .build()
                .injectMainActivity(this);

        Log.d("xiaowang", httpClient.hashCode() + "");
        Log.d("xiaowang", imClient.hashCode() + "");
        Log.d("xiaowang", dbClient.hashCode() + "");
    }
}
// dependencies注意事项：
// 		1) 多个 component 上面的 scope 不能相同
// 		2) 没有 scope 的组件不能去依赖有 scope 的组件
```

### 方式二：@Subcomponent用法

```java
@DbScope
@Subcomponent(modules = {DbModule.class})
public interface DbComponent {
    void injectMainActivity(MainActivity mainActivity);
}

@MainScope
@Component(modules = {HttpModule.class, ImModule.class})
public interface MainComponent {
    DbComponent getDbComponent();
}

DaggerMainComponent.create()
        .getDbComponent()
        .injectMainActivity(this);
```

## 其它用法

### 带参数module

```java
@Module
public class MainModule {
    @Provides
    B providerB() {
        return new B();
    }

	// 构造方法需要其他参数时候，dagger2会自动把参数传入，并构造
    @Provides
    A providerA(B b) {
        return new A(b);
    }
}
```

### @Named使用

```java
// ------- module中 -------
@Named("key1")
@Provides
public User provideUser(){
	return new User("jett","123")；
}

@Named("key2")
@Provides
public User provideUser2(){
	return new User("jett2","456")；
}

// ------- Activity中 -------
@Named("key1")
@Inject
User user1;

@Named("key2")
@Inject 
User user2;

// 通过以上语法，就可以实现两个不同的 key 注入不一样的同类型对象
```

### Lazy 和 Provider

这种方式是在get的时候，才初始化需要注入的对象

```java
// ------ 懒加载（具备局部单例特点）------
@Inject
Lazy<A> lazy;

@Inject
Lazy<A> lazy2;

Log.i("icoTag", lazy.get().hashCode() + "");// 43536281
Log.i("icoTag", lazy2.get().hashCode() + "");// 43536281

// ------ 懒加载（不具备局部单例特点）------
@Inject
Provider<A> provider;

@Inject
Provider<A> provider2;

Log.i("icoTag", provider.get().hashCode() + "");// 208712286
Log.i("icoTag", provider2.get().hashCode() + "");// 84461119
```