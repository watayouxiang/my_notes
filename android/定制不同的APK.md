- Build Variants
  - 构建变体
  - 通过 Build Variants 选项卡可选择构建不同的app
- buildTypes
  - 构建类型
- productFlavors
  - 产品风味
- flavorDimensions
  - 产品维度
  - 每个 “产品风味” 必须指定所属的 “产品维度”
  - 每个productFlavors必须指定所属的flavorDimensions



```
android {
    // 构建类型
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
        debug {

        }
    }
    // 产品维度
    flavorDimensions "size", "material"
    // 产品风味
    productFlavors {
        small {
            dimension "size"
        }
        big {
            dimension "size"
        }
        egg {
            dimension "material"
            applicationIdSuffix ".egg"
        }
        bacon {
            dimension "material"
            applicationIdSuffix ".bacon"
        }
    }
}
```



