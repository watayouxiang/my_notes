# MacOS安装Homebrew

> 参考来源：https://blog.csdn.net/mehent/article/details/108180940



## Homebrew介绍

- Homebrew 能干什么? 使用 Homebrew 安装 Apple 没有预装，但你需要的东西。

- Xcode 现在已经不提供svn的命令行工具了，想安装的话，需要先安装 brew。
- Homebrew官方地址: https://brew.sh/index_zh-cn



## 安装brew

安装 brew，建议使用国内镜像。使用国外镜像，会报错。

```
// 安装brew（国内镜像地址）
/bin/zsh -c "$(curl -fsSL https://gitee.com/cunkai/HomebrewCN/raw/master/Homebrew.sh)"

// 安装软件
brew install 软件名

// 卸载软件
brew uninstall 软件名

// 搜索软件
brew search 软件名

// 查看已安装软件列表
brew list

// 查看需要更新的软件
brew outdated

// 更新软件
brew upgrade 软件名

// 更新Homebrew
brew update
```



## 安装svn

成功后，就可以安装svn了。

```
// 安装svn
brew install svn

// 查看版本
svn --version

// 查看路径
which svn
```



## 安装MySQL

- 官网：https://dev.mysql.com/downloads/mysql/


```
// 使用homebrew安装MySQL
brew install mysql

// 初始化mysql配置
mysql_secure_installation

// 后台启动mysql
brew service start mysql

// 前台启动mysql(关闭控制台，服务停止)
mysql.server start

// 连接mysql服务器
// -u后接用户名，默认用户名是root，-p表示输入密码
mysql -u root -p

// 查看所有本地数据库
show databases;

```

