# 使用macOS自带Apache服务器



编辑于：20171022



> macOS自带Apache服务器
> 
> 目录在 `/etc/apache2/`
> 
> 只要修改 `httpd.conf` 配置文件就可以使用了

### 1、因为`httpd.conf`是系统文件，所以修改前做个备份

```
// 备份
// cp (copy 的缩写) httpd.conf (源文件) httpd.conf.bak (目标文件)
$ sudo cp httpd.conf httpd.conf.bak

// 恢复
$ sudo cp httpd.conf.bak httpd.conf
```

### 2、创建服务器根目录

在Finder创建一个 "/Users/TaoWang/Sites" 文件夹

### 3、修改配置文件

```
#DocumentRoot "/Library/WebServer/Documents"
DocumentRoot "/Users/TaoWang/Sites"

#<Directory "/Library/WebServer/Documents">
<Directory "/Users/TaoWang/Sites">

#Options FollowSymLinks Multiviews
Options Indexes FollowSymLinks Multiviews
```

### 4、启动服务器

```
// 启动服务器
$ sudo apachectl -k start
$ sudo apachectl start

// 关闭服务器
$ sudo apachectl -k stop
$ sudo apachectl stop

// 重启服务器
$ sudo apachectl -k restart
$ sudo apachectl restart
```

### 5、访问服务器

```
//本机的ip地址
http://127.0.0.1
http://localhost
```
