# V42 HTTPS 证书与 App 强制加密

> **所属模块**：M9  ·  **建议时长**：30～40 分钟  ·  **形式**：实操
> **对应大纲**：`Fondia_全栈实战课程大纲.md` → 9.2

## 一、学习目标

1. 申请并配置证书到 Nginx
2. App 强制 HTTPS
3. 生产关闭明文流量

## 二、课前准备

- 已有域名（或测试证书）

## 三、口播 / 讲解提纲

### 3.1 证书

- 免费证书、自动续期

### 3.2 Nginx

- ssl_certificate 配置

### 3.3 客户端

- 禁止 cleartext 生产

## 四、屏幕演示步骤

1. Let's Encrypt 或云证书配置
2. Android networkSecurityConfig / 鸿蒙 cleartext

## 五、课堂实验 / 作业

1. https 访问 api 成功，http 按策略拒绝或跳转

## 六、本节小结（片尾 1 分钟）

- 商店审核越来越严要求 HTTPS
- 证书续期要自动化

**下节预告**：V43 API 域名规划


## 七、关键文件 / 参考

