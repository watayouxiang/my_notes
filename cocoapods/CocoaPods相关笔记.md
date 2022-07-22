## CocoaPods安装

> CocoaPods 是一个 Cocoa 和 Cocoa Touch 框架的依赖管理器，具体原理和 Homebrew 有点类似，都是从 GitHub 下载索引，然后根据索引下载依赖的源代码。

```
// --------------------------------------------------------------------
// 1、安装Homebrew
// --------------------------------------------------------------------

$ /bin/zsh -c "$(curl -fsSL https://gitee.com/cunkai/HomebrewCN/raw/master/Homebrew.sh)"

// --------------------------------------------------------------------
// 2、安装rvm
// --------------------------------------------------------------------

$ curl -L get.rvm.io | bash -s stable 
 
$ source ~/.bashrc
 
$ source ~/.bash_profile

// --------------------------------------------------------------------
// 3、更新ruby
// --------------------------------------------------------------------

// 这里我安装的是2.6.3版本
$ rvm install 2.6.3

// 列出可安装的版本
$ rvm list known

// 将刚刚安装的ruby设置为默认版本
$ rvm use 2.6.3 --default

// 更换ruby镜像源
$ sudo gem update --system

$ gem sources --remove https://rubygems.org/

$ gem sources --add https://gems.ruby-china.com/

// 查看ruby镜像源
$ gem sources -l

// --------------------------------------------------------------------
// 4、安装CocoaPods
// --------------------------------------------------------------------

// 安装CocoaPods
$ sudo gem install -n /usr/local/bin cocoapods

// clone CocoaPods仓库
// 清华大学fork的CocoaPods仓库
$ git clone https://mirrors.tuna.tsinghua.edu.cn/git/CocoaPods/Specs.git  ~/.cocoapods/repos/trunk

// gitee上的一个私人fork的仓库
$ git clone https://gitee.com/mirrors/CocoaPods-Specs.git  ~/.cocoapods/repos/trunk

// 检查是否能正常使用
$ pod search AFNetworking

// --------------------------------------------------------------------
// 5、如果你是M1芯片的Mac，还需要做以下操作：
// --------------------------------------------------------------------

1）访达 - 应用程序 - 实用工具里，右键点击终端 - 显示简介（如果找不到实用工具，请以列表或分栏的方式显示项目）
2）勾选使用 Rosetta 打开
3）执行 `sudo gem install ffi`
```

## CocoaPods换镜像源

```
// --------------------------------------------------------------------
// 对于旧版的 CocoaPods 可以使用如下方法使用 tuna 的镜像：
// --------------------------------------------------------------------

$ pod repo remove master
$ pod repo add master https://mirrors.tuna.tsinghua.edu.cn/git/CocoaPods/Specs.git
$ pod repo update

// --------------------------------------------------------------------
// 新版的 CocoaPods 不允许用pod repo add直接添加master库了，但是依然可以：
// --------------------------------------------------------------------

// 进入目录
$ cd ~/.cocoapods/repos

// 删除该源
$ pod repo remove master

// 在该目录下创建新源
$ git clone https://mirrors.tuna.tsinghua.edu.cn/git/CocoaPods/Specs.git master

// --------------------------------------------------------------------
// 最后进入自己的工程，在自己工程的 Podfile 第一行加上：
// --------------------------------------------------------------------

source 'https://mirrors.tuna.tsinghua.edu.cn/git/CocoaPods/Specs.git'
```

## 可用的 CocoaPods 镜像源

```
// --------------------------------------------------------------------
// 清华镜像源
// --------------------------------------------------------------------

// 源地址
https://mirrors.tuna.tsinghua.edu.cn/git/CocoaPods/Specs.git

// 安装
$ git clone https://mirrors.tuna.tsinghua.edu.cn/git/CocoaPods/Specs.git ~/.cocoapods/repos/tsinghua

// 使用：Podfile 第一行加上
source 'https://mirrors.tuna.tsinghua.edu.cn/git/CocoaPods/Specs.git'

// --------------------------------------------------------------------
// 码云镜像源
// --------------------------------------------------------------------

// 源地址
https://gitee.com/mirrors/CocoaPods-Specs.git

// 安装
$ git clone https://gitee.com/mirrors/CocoaPods-Specs.git ~/.cocoapods/repos/gitee

// 使用：Podfile 第一行加上
source 'https://gitee.com/mirrors/CocoaPods-Specs.git'

// --------------------------------------------------------------------
// 原始github官方镜像源
// --------------------------------------------------------------------

// 源地址
https://github.com/CocoaPods/Specs.git

// 安装
$ git clone https://github.com/CocoaPods/Specs.git ~/.cocoapods/repos/github

// 使用：Podfile 第一行加上
source 'https://github.com/CocoaPods/Specs.git'

// --------------------------------------------------------------------
// 阿里云的镜像源
// --------------------------------------------------------------------

// 源地址
https://github.com/aliyun/aliyun-specs.git

// 安装
$ git clone https://github.com/aliyun/aliyun-specs.git ~/.cocoapods/repos/aliyun

// 使用：Podfile 第一行加上
source 'https://github.com/aliyun/aliyun-specs.git'
```

## CocoaPods常用命令

```
// 首先要安装 Xcode 命令行工具
$ xcode-select --install

// 查看当前 CocoaPods 版本
$ pod --version

// 列出 CocoaPods 所有镜像源
$ pod repo list

// 安装
$ pod setup

// 根据 Podfile 下载三方库
$ pod install

// 根据 Podfile 下载三方库，并详细打印下载进度
$ pod install --verbose --no-repo-update

// 检查是否能正常使用
$ pod search AFNetworking
```

## CocoaPods报错汇总

### The sandbox is not in sync with the Podfile.lock. Run 'pod install' or update your CocoaPods install

```
方案一：

command+Q 退出 Xcode，重新执行pod install，之后再重新打开Xcode运行。

正常 方案一: 即可解决问题.

方案二：

稍微麻烦一点，删除以下文件：

xcworkspace
Podfile.lock
Pods文件夹
~/Library/Developer/Xcode/DerivedData路径下对应工程的文件夹

之后重新执行pod install --verbose --no-repo-update

方案三:

1.设置Configurations

选中Project->选择Info tab->看看Configurations是不是被设置为 None了，如果是None的话请改为Pods，如图：

2.修改Pods脚本文件路径:

右键工程根目录下的xxx.xcodeproj文件，显示包内容
双击打开project.pbxproj文件
查找"resources.sh", 把路径改为如下红框处：

重新打开工程（双击workspace文件），到此应该是能正常编译运行了.

方案四:

1.找到项目根目录下的Podfile.lock文件,和Pods文件夹下的Manifest.lock文件

2.然后复制Podfile.lock文件的内容替换掉Manifest.lock文件内的内容,好啦,至此问题解决
```

