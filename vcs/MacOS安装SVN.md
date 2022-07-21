## 1、安装brew

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



## 2、用brew安装svn

成功后，就可以安装svn了。

```
// 安装svn
brew install svn

// 安装svn遇到的问题：fatal: not in a git directory Error: Command failed with exit 128: git
// 解决办法：https://blog.csdn.net/Morris_/article/details/125182905

// SVN提示https证书验证失败问题 E230001: Server SSL certificate verification failed: certificate issued
// 解决办法：https://blog.csdn.net/weixin_49341379/article/details/125473916

// 查看版本
svn --version

// 查看路径
which svn
```

