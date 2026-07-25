# V38 Nginx：API 域名与 SSE 长连接

> **所属模块**：M8  ·  **建议时长**：35～50 分钟  ·  **形式**：实操
> **对应大纲**：`Fondia_全栈实战课程大纲.md` → 8.3

## 一、学习目标

1. 配置 api 域名反代 Gateway
2. SSE/WS 关闭缓冲与超时
3. 静态/OSS 分离

## 二、课前准备

- 域名解析到服务器（或 hosts）

## 三、口播 / 讲解提纲

### 3.1 反代

- api.fondia.com → :9999

### 3.2 SSE/WS

- buffering off
- read_timeout

### 3.3 静态

- CDN/OSS 域名

## 四、屏幕演示步骤

1. nginx.conf 关键指令
2. 用 curl -N 测 SSE

## 五、课堂实验 / 作业

1. 手机 App 改 BASE_URL 走域名访问成功

## 六、本节小结（片尾 1 分钟）

- SSE 必须 proxy_buffering off
- 超时要覆盖长对话

**下节预告**：V39 配置外置


## 七、关键文件 / 参考

