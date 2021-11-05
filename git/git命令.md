# git命令

> 参考：
> 
> - [https://git-scm.com/book/zh/v2](https://git-scm.com/book/zh/v2)
> - [http://www.liaoxuefeng.com/wiki/0013739516305929606dd18361248578c67b8067c8c017b000](http://www.liaoxuefeng.com/wiki/0013739516305929606dd18361248578c67b8067c8c017b000)

### Git 全局设置

```
git config --global user.name "user_name"
git config --global user.email "user_email@qq.com"
```

### 提交到已有的远程git仓库

```
// 克隆仓库
git clone git@code.aliyun.com:user_name/test.git

// 进入仓库
cd test

// 创建"README.md"
touch README.md

// 浏览您所做的更改
$ git status

// 添加"README.md"
git add README.md

// 提交修改
git commit -m "add README"

// 推送到"master"
git push -u origin master
```

### 新建远程git仓库

```
// 进入文件夹
cd existing_folder

// 初始化成git仓库
git init

// 建立远程仓库
git remote add origin git@code.aliyun.com:user_name/test.git

// 添加所有文件
git add .

// 提交所有文件
git commit

// 推送到"master"
git push -u origin master
```

### git常用命令速查表

<img src="http://upload-images.jianshu.io/upload_images/1490226-b320d0261760ae48.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240" width="800"  />
