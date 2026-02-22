# SynJi-Server Docker 部署指南（Ubuntu 22.04）

## 1. 服务器初始化（阿里云 Ubuntu 22）

### 1.1 更新系统
```bash
sudo apt update
sudo apt -y upgrade
sudo apt -y install ca-certificates curl gnupg lsb-release make
```

### 1.2 安装 Docker Engine + Compose 插件（官方源）
```bash
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo $VERSION_CODENAME) stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt update
sudo apt -y install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

### 1.3 启动 Docker 并设置开机自启
```bash
sudo systemctl enable docker
sudo systemctl start docker
sudo systemctl status docker --no-pager
```

### 1.4 （可选）免 sudo 使用 docker
```bash
sudo usermod -aG docker $USER
newgrp docker
docker version
docker compose version
```

## 2. 上传/拉取项目代码

你有两种方式：

### 方式 A：服务器直接 `git clone`
```bash
git clone <你的仓库地址> SynJi-Server
cd SynJi-Server
```

### 方式 B：本地打包上传（Workbench/SCP）
在本地项目根目录执行：
```bash
tar czf synji-server.tar.gz SynJi-Server
```
上传到服务器后执行：
```bash
tar xzf synji-server.tar.gz
cd SynJi-Server
```

## 3. 配置生产环境变量

```bash
cp .env.example .env
vim .env
```

你至少要改这些字段：
- `DB_PASSWORD`
- `DB_ROOT_PASSWORD`
- `SILICON_API_KEY`
- `APP_CORS_ALLOWED_ORIGIN_PATTERNS`（改成你的前端域名）

内测阶段建议：
- `JPA_SHOW_SQL=true`（保留 SQL 日志）
- `APP_AUTH_RETURN_VERIFY_CODE=true`（如果你仍要前端拿到验证码）

## 4. 一键启动

```bash
make up
# 或者
make deploy
```

常用命令：
```bash
make ps
make logs
make down
make restart
```

## 5. 健康检查

启动后访问：
```bash
curl http://127.0.0.1:8080/api/ping
```

如果你已放行安全组和服务器防火墙端口（8080），外网访问：
```bash
curl http://<你的公网IP>:8080/api/ping
```

## 6. 阿里云网络与防火墙

### 6.1 阿里云控制台安全组
开放入站端口：
- `22/tcp`（SSH）
- `8080/tcp`（应用）

### 6.2 Ubuntu UFW（如果启用）
```bash
sudo ufw allow 22/tcp
sudo ufw allow 8080/tcp
sudo ufw status
```

## 7. 升级发布

```bash
git pull
make up
```

`make up` 会自动重新构建并重启新容器。

## 8. 数据库初始化说明

- `docker-compose.yml` 里的 `MYSQL_DATABASE` 会先创建数据库（默认 `synji_calendar`）。
- 首次启动 MySQL 容器时，会自动执行 `docker/mysql/init/01-schema.sql`。
- 该脚本是 `CREATE IF NOT EXISTS`，不会在重启时清空数据。
- MySQL 数据持久化在 Docker volume `mysql_data`。

## 9. 回滚与清理

仅停止服务：
```bash
make down
```

危险操作（删除数据库数据）：
```bash
make clean
```
