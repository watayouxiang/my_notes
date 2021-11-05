# macOS终端命令（20171018）



## Finder显隐“隐藏文件”

```
--- Finder显示“隐藏文件 ---
$ defaults write com.apple.finder AppleShowAllFiles -boolean true
$ killall Finder

--- Finder隐藏“隐藏文件 ---
$ defaults write com.apple.finder AppleShowAllFiles -boolean false
$ killall Finder
```



## 文件命令

### 进入文件夹

```
// 进入到当前用户根目录
$ cd ~

// 进入目录
$ cd <dir>

// 返回上一级目录
$ cd ..

```

### 打开文件

```
// 打开文件
$ open <name>

// 打开当前目录
$ open .

```

### 创建文件

```
// 创建文件
$ touch <file>

// 创建目录
$ mkdir <dirname>

```

### 删除文件

```
// 删除文件
$ rm <file>

// 参数`-d`表示递归和强制
$ rm -d <dir>

```

### 查看文件

```
// 查看文件内容
$ cat <file>
```

### 复制文件

```
// 将 buildSrc 复制一份，名字叫 buildSrc-copy
$ cp -rf buildSrc buildSrc-copy
```



## 查看命令

```
// 查看命令的信息
$ info <命令名字>

// 查看命令的使用说明
$ man <命令名字>
```



## 其他常用命令

```
// 显示当前路径
$ pwd

// 显示所有文件
$ ls

// 退出
$ ctrl + Z

// 显示所有环境变量
$ echo $PATH

// 退出
$ q

// 清空控制台内容
$ clear
```
