# V37 Docker Compose 部署中间件与微服务

> **所属模块**：M8  ·  **建议时长**：50～70 分钟  ·  **形式**：实操
> **对应大纲**：`Fondia_全栈实战课程大纲.md` → 8.2

## 一、学习目标

1. 用 Compose 拉起基础组件
2. 打包 cloud-start 镜像
3. prod profile 与环境变量

## 二、课前准备

- 云主机或本地 Docker

## 三、口播 / 讲解提纲

### 3.1 中间件

- MySQL/Redis/ES/Nacos/MinIO

### 3.2 应用

- jar 镜像
- SPRING_PROFILES_ACTIVE=prod

## 四、屏幕演示步骤

1. compose up 中间件
2. 构建并运行 gateway/user
3. 看日志确认注册 Nacos

## 五、课堂实验 / 作业

1. 在服务器跑通 Gateway + user + support/agent

## 六、本节小结（片尾 1 分钟）

- 镜像构建可重复
- 密钥走环境变量/Nacos

**下节预告**：V38 Nginx 反代


## 七、关键文件 / 参考

- `docker-compose.yml`
- `各 *-cloud-start/Dockerfile`
