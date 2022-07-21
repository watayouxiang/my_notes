# MacOS系统配置GitSSH(20190116)

### 1. 生成 “ssh key”

```
// 生成 "ssh key 公钥和私钥"
$ ssh-keygen -t rsa -C "test@qq.com"

// 正在生成了 "ssh key 公钥和私钥"
Generating public/private rsa key pair.

// 密钥保存的位置（默认保存到 "/c/Users/admin/.ssh/" 目录下，文件名是 "id_rsa"）
Enter file in which to save the key (/c/Users/admin/.ssh/id_rsa):

// 输入密码（不输入则表示不需要密码）
Enter passphrase (empty for no passphrase):

// 再次确认密码
Enter same passphrase again:

// 看到如下提示说明生成 ssh key 成功
Your identification has been saved in /c/Users/admin/.ssh/id_rsa.
Your public key has been saved in /c/Users/admin/.ssh/id_rsa.pub.
The key fingerprint is:
SHA256:dEkBwI1SczroUiOjISGntaYYa7hTzuVOyTuf3kSgWYE watayouxiang@qq.com
The key's randomart image is:
+---[RSA 2048]----+
|o o  +=+o.o.     |
|.= .E.o=.. .     |
|= = +.= . o      |
|+B = = + .       |
|*.o =   S        |
|.= = . .         |
|o o =   .        |
| . o.. +         |
|    o++ .        |
+----[SHA256]-----+
```

### 2. 查看 “ssh key”

```
// 进入 "ssh key 公钥和私钥" 所在的文件夹
$ cd  /Users/admin/.ssh

// 查看当前目录所有的文件
$ ls
id_rsa  id_rsa.pub  known_hosts

// 查看公钥 "id_rsa.pub"
$ cat id_rsa.pub
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDKjxHAKWTOnpAFUGn0MkWf83h8xvZE1xIukuQFuYl7tblgnblyiHjL5vXxqiS7gq0Uhr3xSpCmHtrpVw9ToYoP+28kPNQkJyeySwJc1ypMG+xSBiNIkKVz/2GcOBrwilFlKlXaDdiA4vgf8d+MVI+NByFAbdr3V2n26/bN6mVjPYdvBTzwBwmqOkJ8acHf5Q9dISjgMbuFnoKxQ1jDWTODx1UXviCFLSRBxQC0K7BFGqaXKV2iN4iLEE5Eaj6AeTK5wv7vR7Fe9WgtjljBTXlkZMBP0/kYAnAjOaHESameFcFpivHHZpGtZCrqrdH+1s2uFf6qOF00g9K4WvG75zbL test@qq.com

//查看私钥 "id_rsa"
$ cat id_rsa
-----BEGIN RSA PRIVATE KEY-----
MIIEowIAAAKCAQAA1cXLJCA98E7CBomDAj5fLu8bOpTunNBRuYNnhBQTuyHZFxnC
otrKol/RzQrOWnxXvVAQSa67ccrblygNR5OsW018exD+8TRbY6YISXCHGwXjIBrm
PJmzsinrWKFHY8EAFsT+zFYsy/sW2Krs6P57eae6pirSIAVActdDeWStzECY5QcF
lCwVJ0SiNhdFGvp7oAmAUhHhNF783q6+BWogth4HWfB7GZeEMDu8FJmdL5lyEWYS
0Y7Om+EQ/O84fxsrDTLbAYCARQKaZ3mx4KlRrgaflQXHm+h1PenPa5HtK3sa6mA8
P9oRPPpxtlvXpGwBfS10gAyAAdtkoJP4fuAAdQIDAQABAoIBADptLPBdr7oWTIFs
5Bmg4nL5dv+z5OOHLE7/0zMOKbZjsenWAAQKKIfomHbqFSw1/+UG5tS8pQ6c2xed
IsQFAA4z12nrY0KZhMDfiDRKRSLloQYNVBAMEkkHnANFUTW6dhdTEAAY/LVjkwL0
9HtZ0nWVmjYATh3iUZMRsqdjFP1Vyno6wfY6ndwIys0sfAL8wtLFGQx6f/MO8ilh
dcqaaWRsrYX7OVJTgpnbAn3o/CsvrMvrRq/VjGvX4ag50g+sXuFmCu3sK9UP9kwy
mccXJcekwy69XqHrVVfdWHMNGGKeB27eb+xfq/CjqePRagDKe6i3vS4kgViZMcOO
SbFzaQECgYEA81md8ddrMVpxp/8SucyfcTpB4rXRFYYSmG8ZJRd+9ns4K5vzuo6+
oMKWRIG703KAAAxZ0aVNSxtXcezjZTximlt31fH3QQsBAJ7aa8wlWz/k00+4EicZ
y2okgVkzTmpxOaQ3rAAsuORGs/FFs7xsuzbwvnKVF0diKyDprrEQ/O0CgYEA4OKT
F9bfBC06so4JyWYDJZ3OXZTFP6c5rkrx4QSN7GP47L2E7+mte/2+rGJWf3lfj+rk
bZr82c50KWibg4vrPLBJw9+9BDYQh+M/8iH+dkamRhqIoTqU7xHRMQZI9hyH2Ysi
SWzgDIlqTQqOVt5xbu64kWAq2dCbJhueqk7AeKkAgYAcnRr/As8p3HKkyE9RXYgZ
7jm1CJ/vIfapydZxEBQzs0Vli07bTtGHYZn9sCt/r2fl7lRZXFB8Vliv4qTSUIlf
kQXEOFFUapDRm9we9MAnvJmAAAxDliloS7DDLNPlkoqBtpd9YZDHpL/ThLknay51
zP39xsdVF2WpoJMZoWRX+QKBAAA8mGOIqTEAa0mSc3n+N77MrsWlCJ8FLAQv14aI
EM6NqG7ytbE6WBYrIEc8d8NcIawaVmCbxhALJMJPDfE96Tng73YsrX4MptbgspCT
cO2nurx1lWJucxDPEia1v6xbaBVY0X8uqlzN9t6avVBpvytpZyHBWQXWzPazit8Z
dmTpAoGBALWTMemiLnEwCNZpRFLO1Erpuy1x5JB70djztP+kU2vQbH3hrjOnqaqa
cuKtZ8yjBh+4Z0E6+SuAAAAJQXV053YtX6gv0omebn12ZSsGLfVzMgzDStLc93IA
F0oAAyAAwK2Klj9ZtYAAAjaU6khfR6wzWRJLKNqlM8znAA//d4p5
-----END RSA PRIVATE KEY-----
```

### 3. “私钥” 添加到 ssh-agent

```
// 启用 ssh-agent
$ ssh-agent -s

// 把私钥添加到 ssh-agent
$ ssh-add ~/.ssh/id_rsa
```

### 4. “公钥” 添加到git远程仓库

```
// 公钥
$ cat id_rsa.pub
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDKjxHAKWTOnpAFUGn0MkWf83h8xvZE1xIukuQFuYl7tblgnblyiHjL5vXxqiS7gq0Uhr3xSpCmHtrpVw9ToYoP+28kPNQkJyeySwJc1ypMG+xSBiNIkKVz/2GcOBrwilFlKlXaDdiA4vgf8d+MVI+NByFAbdr3V2n26/bN6mVjPYdvBTzwBwmqOkJ8acHf5Q9dISjgMbuFnoKxQ1jDWTODx1UXviCFLSRBxQC0K7BFGqaXKV2iN4iLEE5Eaj6AeTK5wv7vR7Fe9WgtjljBTXlkZMBP0/kYAnAjOaHESameFcFpivHHZpGtZCrqrdH+1s2uFf6qOF00g9K4WvG75zbL test@qq.com
```

### 5. 配置 config 文件

```
// 进入到.ssh目录
$ cd ~/.ssh

// 打开config配置文件
$ open config

// 修改config配置文件
Host *
UseKeychain yes
AddKeysToAgent yes
IdentityFile ~/.ssh/id_rsa

// 验证连通成功
$ ssh -T git@github.com
```
