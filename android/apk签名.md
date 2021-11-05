## 创建一个apk key





## 升级apk key的安全性

- 将apk key迁移到行业标准的格式PKCS12

```
// 进入到apk key所在的位置
$ cd /Users/TaoWang/Documents/Code/github/Android/wt_apkkey

// 升级apk key迁移到行业标准的格式PKCS12
// -srckeystore 源key地址
// -destkeystore 升级后的key地址（同名直接覆盖就好）
// -deststoretype 签名的格式类型（pkcs12）
$ keytool -importkeystore -srckeystore ./test_wt666.key -destkeystore ./test_wt666.key -deststoretype pkcs12

// 输入key的密码便升级成功
```



## 使用apk key

- app module 的 build.gradle 配置信息

```
android {
    signingConfigs {
        release {
            keyAlias 'test_wt666'
            keyPassword 'test_wt666'
            storeFile file('../wt_apkkey/test_wt666.key')
            storePassword 'test_wt666'
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
        }
    }
}
```

