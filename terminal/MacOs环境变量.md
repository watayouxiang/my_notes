## 常见环境变量



Mac系统的环境变量，加载顺序为：

- /etc/profile
  - 系统级别的,系统启动就会加载
  - 全局（公有）配置，不管是哪个用户，登录时都会读取该文件。

- /etc/paths
  - 系统级别的,系统启动就会加载

- ~/.bash_profile
  - 用户级的环境变量
  - 如果 ~/.bash_profile 文件存在，则后面的几个文件就会被忽略，若不存在才会依次读取 `~/.bash_login`、`~/.profile`
  - 若需要立即生效，需要执行 `source ~/.bash_profile` ,否则一般重启后生效。

- ~/.bash_login

- ~/.profile
- ~/.bashrc
  - ~/.bashrc 没有上述规则，它是 bash shell 打开的时候载入的



