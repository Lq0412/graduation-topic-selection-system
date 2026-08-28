# 毕业设计选题系统

一个面向毕业设计选题流程的前后端分离系统，覆盖学生、教师、系部主任和管理员四类角色。系统包含账号管理、题目发布与审核、学生预选/确认、选题进度统计、CSV 导入导出、邮件通知和 WebSocket 通知等功能。

> 首次公开前的关键安全和稳定性整改已经完成，但它仍是个人毕业设计项目，尚未按正式生产系统做完整运维加固。后续可选工作见 [TODO.md](./TODO.md)。

## 技术栈

- 前端：React 18、Umi Max 4、Ant Design 5、TypeScript、pnpm
- 后端：Java 8、Spring Boot 2.5、MyBatis-Plus、Sa-Token、Maven
- 数据服务：MySQL 8、Redis
- 可选服务：SMTP 邮箱、腾讯云智能体 API

## 目录结构

```text
.
├── work-topic-selection-frontend/   # React/Umi 前端
├── work-topic-selection-backend/    # Spring Boot 后端
├── .env.example                     # 本地环境变量示例
├── build.sh                         # 本地构建前后端 Docker 镜像
└── TODO.md                          # 整理、安全和稳定性待办
```

## 本地运行

### 1. 准备环境

- JDK 8
- MySQL 8.x
- Redis
- Node.js 24.x
- pnpm 11.19.0

### 2. 初始化数据库

先使用你自己的 MySQL 管理账号创建数据库：

```sql
CREATE DATABASE work_topic_selection
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
```

再导入不含账号和凭据的公开表结构：

```bash
mysql -u YOUR_MYSQL_USER -p work_topic_selection \
  < work-topic-selection-backend/src/main/resources/sql/schema.sql
```

如需少量虚构的系部、专业和题目数据，可继续导入：

```bash
mysql -u YOUR_MYSQL_USER -p work_topic_selection \
  < work-topic-selection-backend/src/main/resources/sql/demo-data.sql
```

公开示例不会创建可登录账号。若数据库中还没有任何管理员，可在本地 `.env` 中同时填写 `APP_BOOTSTRAP_ADMIN_ACCOUNT`、`APP_BOOTSTRAP_ADMIN_NAME` 和 `APP_BOOTSTRAP_ADMIN_PASSWORD`。后端只会在管理员数量为零时创建一次，并以 BCrypt 保存密码；首次登录会要求改密。创建完成后可以清空这三个变量。

如果要继续使用旧版数据库，请先备份数据库，再依次执行迁移脚本：

```bash
mysql -u YOUR_MYSQL_USER -p work_topic_selection \
  < work-topic-selection-backend/src/main/resources/sql/migration-20260828-teacher-account.sql

mysql -u YOUR_MYSQL_USER -p work_topic_selection \
  < work-topic-selection-backend/src/main/resources/sql/migration-20260829-selection-uniqueness.sql
```

第一份脚本最后会列出无法自动匹配教师账号的旧题目，必须人工补全 `teacherAccount`；第二份脚本最前面的重复检查必须没有结果，否则应先人工整理重复选题记录和对应题目计数，再添加唯一约束。

### 3. 配置环境变量

```bash
cp .env.example .env
```

修改 `.env` 中的 MySQL 配置。Redis 没有密码时可以保持空值；邮件和 AI 功能不用时也可以留空。随后在准备启动后端的终端加载变量：

```bash
source ./env.sh
```

普通 `.env` 文件不会被 Spring Boot 自动读取，所以每个新终端都需要重新加载。

### 4. 启动后端

```bash
cd work-topic-selection-backend
./mvnw spring-boot:run
```

后端默认监听 `http://127.0.0.1:8000`，接口文档为 `http://127.0.0.1:8000/doc.html`。

### 5. 启动前端

打开另一个终端：

```bash
cd work-topic-selection-frontend
pnpm install --frozen-lockfile
pnpm dev
```

前端默认监听 `http://127.0.0.1:3000`，通过 `127.0.0.1` 或 `localhost` 访问时会连接本机 `8000` 端口的后端。

## 检查与构建

```bash
# 后端测试
cd work-topic-selection-backend
./mvnw test

# 前端代码、类型、依赖与生产构建检查
cd ../work-topic-selection-frontend
pnpm lint:js
pnpm tsc
pnpm audit --prod
pnpm build
```

已安装 Docker 时，也可以在根目录运行 `./build.sh`，它只会构建两个本地 `:local` 镜像，不会推送到镜像仓库。

## 数据与隐私

真实学生/教师名单、账号表格、带账号或凭据的旧 SQL、操作截图和二维码已移动到本机根目录下的 `.private-data/`，并已加入 `.gitignore`；普通环境变量和构建产物同样不会提交。提交前请运行 `git add -n .` 再次核对将要进入 Git 的文件。

批量导入学生或教师时，需要为每个账号填写不同的 12–72 字节临时密码；系统只保存 BCrypt 散列。仓库不提供可直接登录的公开演示账号。

邮件发送和 AI 审题是可选功能。公开部署时还应配置 HTTPS、可信反向代理、严格的 CORS/WebSocket 来源、Redis 密码和独立的 SMTP/API 凭据；不要把本机 `.env` 上传到仓库。

## 来源与许可证

本项目基于 [limou3434/work-topic-selection](https://github.com/limou3434/work-topic-selection) 整理和继续开发，源码中的原作者标注予以保留。原项目 `pom.xml` 声明 MIT License，本仓库据此补充了 [LICENSE](./LICENSE)。
