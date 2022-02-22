# IOC-Hilt实现原理

> Hilt就是Android团队联系了Dagger2团队，一起开发出来的一个专门面向Android的依赖注入框架。
>
> 相比于Dagger2，Hilt最明显的特征就是：1. 简单。2. 提供了Android专属的API。

## 网络隔离层设计

> 静态代理

### 1、回调接口设计

```java
public interface ICallback {
    void onSuccess(String result);
    void onFailure(String e);
}

public abstract class HttpCallback<Result> implements ICallback {
    @Override
    public void onSuccess(String result) {
        // 1.得到泛型Result，具体的class
        Class<?> clz = analysisClassInfo(this);
        // 2.把String转成javaBean
        Gson gson = new Gson();
        Result objResult = (Result) gson.fromJson(result, clz);
        // 3.把javaBean交给用户
        onSuccess(objResult);

    }

    public abstract void onSuccess(Result objResult);

    // 得到泛型Result，具体的class
    private Class<?> analysisClassInfo(Object object) {
        Type getType = object.getClass().getGenericSuperclass();
        Type[] params = ((ParameterizedType) getType).getActualTypeArguments();
        return (Class<?>) params[0];
    }


    @Override
    public void onFailure(String e) {

    }
}
```
### 2、代理类设计

```java
// 【房产公司】
public interface IHttpProcessor {
    // 【卖房的方法】
    void post(String url, Map<String, Object> params, ICallback callback);
}

// 【业务员】
public class HttpHelper implements IHttpProcessor {
    // 【有房子的人】
    private static IHttpProcessor mIHttpProcessor = null;
    
    public static void init(IHttpProcessor httpProcessor) {
        mIHttpProcessor = httpProcessor;
    }
    
    // 单例
    private static HttpHelper instance;

    public static HttpHelper obtain() {
        synchronized (HttpHelper.class) {
            if (instance == null) {
                instance = new HttpHelper();
            }
        }
        return instance;
    }

    private HttpHelper() {
    }

    // 【卖房的方法】
    @Override
    public void post(String url, Map<String, Object> params, ICallback callback) {
        mIHttpProcessor.post(url, params, callback);
    }
}
```

### 3、具体实现类

```java
// 【有房子的人】
public class OkHttpProcessor implements IHttpProcessor {

    private OkHttpClient mOkHttpClient;
    private Handler myHandler;

    public OkHttpProcessor() {
        mOkHttpClient = new OkHttpClient();
        myHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void post(String url, Map<String, Object> params, final ICallback callback) {
        final RequestBody requestBody = appendBody(params);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                if (response.isSuccessful()) {
                    myHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(result);
                        }
                    });
                } else {
                    myHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure(result);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                myHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailure("onFailure");
                    }
                });
            }

        });
    }

    private RequestBody appendBody(Map<String, Object> params) {
        FormBody.Builder body = new FormBody.Builder();
        if (params == null || params.isEmpty()) {
            return body.build();
        }
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            body.add(entry.getKey(), entry.getValue().toString());
        }
        return body.build();
    }
}
```

### 4、开始使用

```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
      
        // 初始化      
//        HttpHelper.init(new VolleyProcessor(this));
//        HttpHelper.init(new XUtilsProcessor(this));
        HttpHelper.init(new OkHttpProcessor());
      
        // 开始使用
        // HttpHelper.obtain().post(...);
      
    }
}
```

## Hilt基本语法

### 基本配置

```java
// --------------------
// project gradle
// --------------------
classpath 'com.google.dagger:hilt-android-gradle-plugin:2.28-alpha'

// --------------------
// module gradle
// --------------------
apply plugin: 'dagger.hilt.android.plugin'

implementation "com.google.dagger:hilt-android:2.28-alpha"
annotationProcessor "com.google.dagger:hilt-android-compiler:2.28-alpha"

//kotlin
//katp "com.google.dagger:hilt-android-compiler:2.28-alpha"

// 支持java8
compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
}
```

### 基本使用

```java
// 1、标记Application（必须写，否则报错）
@HiltAndroidApp
public class MyApplication extends Application {
}

// 2、module，Component
public class HttpObject {
}

@InstallIn(ActivityComponent.class)
@Module
public class HttpModule  {
    @Provides
    public HttpObject getHttpObject(){
        return new HttpObject();
    }
}

// 3、添加一个注入点
@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    @Inject
    HttpObject httpObject;

    @Inject
    HttpObject httpObject2;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("jett",httpObject.hashCode()+"");
        Log.i("jett",httpObject2.hashCode()+"");
    }
}

// Hilt 提供给了如下组件：
// --------------------------------------------------------------------------------------------------------
// Android类																	 生成的组件																作用域
// --------------------------------------------------------------------------------------------------------
// Application																ApplicationComponent										@Singleton
// ViewModel																	ActivityRetainedComponent								@ActivityRetainedScope
// Activity																		ActivityComponent												@ActivityScoped
// Fragment																		FragmentComponent												@FragmentScoped
// View																				ViewComponent														@ViewScoped
// 带有@WithFragmentBindings注释的View					 ViewWithFragmentComponent							 @ViewScoped
// Service																		ServiceComponent												@ServiceScoped
// --------------------------------------------------------------------------------------------------------
```

### 单例

```java
// 局部单例
@InstallIn(ActivityComponent.class)
@Module
public class HttpModule  {
    @ActivityScoped
    @Provides
    public HttpObject getHttpObject(){
        return new HttpObject();
    }
}


// 全局单例
@InstallIn(ApplicationComponent.class)
@Module
public class HttpModule  {
    @Singleton
    @Provides
    public HttpObject getHttpObject(){
        return new HttpObject();
    }
}
```

## Hilt使用示例

### Hilt实现隔离层设计

```java
// 1、抽象类
public interface TestInterface {
    void method();
}

// 2、具体的实现类
public class TestImpl implements TestInterface{
    // 实现类一定要提供构造方法！
    @Inject
    TestImpl(){
    }
  
//    @Inject
//    TestImpl(@ApplicationContext Application application){
//    }
//
//    @Inject
//    TestImpl(@ActivityContext Activity activity){
//    }

    @Override
    public void method() {
        Log.i("jett", "注入成功！");
    }
}

// 3、代理类
@Module
@InstallIn(ActivityComponent.class)
public abstract class TestModule {
    // 普通对象用 @provides
    // 接口对象用 @Binds
    @Binds
    public abstract TestInterface bindTestImpl(TestImpl impl);
}

// 4、使用
@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    @Inject
    TestInterface testInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testInterface.method();
    }
}
```

### Hilt实现网络隔离层设计

```java
// 1、定义注解
// 相当于 dragger 的 @Name 注解
// 用于区分对象
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface BindOkhttp {
}

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface BindVolley {
}

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface BindXUtils {

}

// 2、代理类（全局单例）
@Module
@InstallIn(ApplicationComponent.class)
public abstract class HttpProcessorModule {

    @BindOkhttp
    @Binds
    @Singleton
    abstract IHttpProcessor bindOkhttp(OkHttpProcessor okHttpProcessor);

    @BindVolley
    @Binds
    @Singleton
    abstract IHttpProcessor bindVolley(VolleyProcessor volleyProcessor);

    @BindXUtils
    @Binds
    @Singleton
    abstract IHttpProcessor bindXUtils(XUtilsProcessor xUtilsProcessor);
}

// 3、具体实现类
public class OkHttpProcessor implements IHttpProcessor {
    private OkHttpClient mOkHttpClient;
    private Handler myHandler;

    @Inject
    public OkHttpProcessor() {
        mOkHttpClient = new OkHttpClient();
        myHandler = new Handler(Looper.getMainLooper());
    }
  
  	......
}

public class VolleyProcessor implements IHttpProcessor{
    private static RequestQueue mQueue = null;

    @Inject
    public VolleyProcessor(@ApplicationContext Context context){
        mQueue = Volley.newRequestQueue(context);
    }
  
  	......
}

// 4、使用
@HiltAndroidApp
public class MyApplication extends Application {

//    @BindVolley
//    @BindXUtils
    @BindOkhttp
    @Inject
    IHttpProcessor iHttpProcessor;

    public IHttpProcessor getiHttpProcessor() {
        return iHttpProcessor;
    }
}

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    IHttpProcessor iHttpProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iHttpProcessor=((MyApplication)getApplication()).getiHttpProcessor();
    }
}
```