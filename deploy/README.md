# 生产部署说明

本目录用于把「毕业设计选题系统」以 Docker Compose 方式部署到一台公网服务器。

## 架构

| 服务 | 镜像 / 容器 | 说明 |
| --- | --- | --- |
| `work-frontend` | `graduation-topic-selection-frontend:local` | Caddy 2 提供 HTTPS、静态文件、API/WebSocket 反代 |
| `work-backend` | `graduation-topic-selection-backend:local` | Spring Boot 后端，仅容器内网可访问 |
| `work-mysql` | `mysql:8.0` | 数据库，数据保存在 `mysql-data` 卷 |
| `work-redis` | `redis:7-alpine` | 会话与缓存，数据保存在 `redis-data` 卷 |

后端 release 配置已经默认连接 `work-mysql:3306` 与 `work-redis:6379`，服务名与 `docker-compose.yml` 保持一致。

## 首次部署

### 1. 准备域名和服务器

- 域名解析（A 记录）指向服务器公网 IP；
- 服务器安全组/防火墙放行 80 和 443；
- 安装 Docker 和 Docker Compose v2。

### 2. 准备环境变量

```bash
cd deploy
cp .env.production.example .env
```

编辑 `deploy/.env`，至少填写 `SITE_DOMAIN`、三个随机密码。可用下面的命令生成密码：

```bash
openssl rand -base64 24
```

### 3. 构建并上传应用镜像

构建脚本需要 JDK 8、Node 24 与 pnpm 11（Windows 请在 Git Bash/WSL 中执行）：

```bash
cd 仓库根目录
./build.sh
```

如果镜像在开发机生成、部署在服务器，可以打包传输：

```bash
docker save graduation-topic-selection-backend:local graduation-topic-selection-frontend:local \
  | gzip > deploy/wts-images.tar.gz
scp deploy/wts-images.tar.gz 用户名@服务器IP:/tmp/
```

服务器上先加载镜像，再上传本 `deploy/` 目录（含 `.env`、`Caddyfile`、`docker-compose.yml`）：

```bash
docker load -i /tmp/wts-images.tar.gz
cd /path/to/deploy
```

也可以在服务器上直接克隆仓库并运行 `./build.sh`，前提是服务器具备 JDK 8、Node 24、pnpm 11。

### 4. 首次初始化数据库

只启动数据库，等它健康后导入公开表结构：

```bash
docker compose up -d work-mysql work-redis
docker compose ps

docker compose exec -T work-mysql \
  sh -c 'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD" work_topic_selection' \
  < ../work-topic-selection-backend/src/main/resources/sql/schema.sql
```

如果是迁移旧库，请先恢复原有备份，再按需执行仓库中的历史迁移脚本；不要对已有数据重复导入 `schema.sql`。

### 5. 启动全部服务

```bash
docker compose up -d
docker compose ps
docker compose logs -f work-backend
```

Caddy 会在启动后自动申请 HTTPS 证书。首次签发可能需要几十秒到几分钟。

### 6. 验证

```bash
curl -I https://${SITE_DOMAIN}
curl -I https://${SITE_DOMAIN}/work_topic_selection_api/actuator/health
```

浏览器打开 `https://${SITE_DOMAIN}`，用管理员账号登录后检查页面、通知声音与 WebSocket 实时推送是否正常。

## 备份与恢复

每天或每次发布前执行数据库备份：

```bash
cd deploy
./backup.sh
```

备份文件位于 `deploy/backups/`，请将备份文件转存到其他服务器或对象存储。

恢复备份：

```bash
cd deploy
gzip -dc backups/work_topic_selection-备份时间戳.sql.gz \
  | docker compose exec -T work-mysql \
      sh -c 'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD" work_topic_selection'
```

## 升级发布

1. 先备份数据库；
2. 拉取新代码，确认是否有需要先执行的数据库迁移；
3. 重新执行 `./build.sh`；
4. `docker compose up -d`（镜像标签相同时需先 `docker compose up -d --force-recreate` 重建容器）。

## 上线前仍需人工确认

- 页面底部已展示「粤ICP备2026131537号」并链接到 https://beian.miit.gov.cn；如后续完成公安联网备案，还需在页脚补充公网安备号；
- 首次管理员登录后立即修改密码，并清空 `.env` 中的引导变量；
- 需要邮件验证码时填写 SMTP 凭据；需要 AI 审题时填写腾讯云智能体 App Key；
- 建议确认接口文档（Knife4j）在生产环境已关闭，避免公开暴露接口结构。
