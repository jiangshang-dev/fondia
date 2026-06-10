# 用户登录功能说明

## 功能概述
本模块实现了多种登录方式，包括：
- **账号密码登录**: 支持用户名、手机号、邮箱作为账号
- **邮箱验证码登录**: 使用邮箱验证码快捷登录
- **第三方登录**: Apple、Google、支付宝、微信等第三方账号登录

## 架构设计

### 核心组件
1. **ThirdPartyLoginHandler**: 第三方登录处理器接口，每种登录方式实现此接口
2. **FdUser**: 用户实体类
3. **FdUserAuth**: 用户第三方授权实体类
4. **IFdUserService**: 用户服务接口
5. **IVerificationCodeService**: 验证码服务接口
6. **LoginController**: 用户登录控制器

### 表结构
- `fd_user`: App用户表，存储用户基本信息
- `fd_user_auth`: App用户第三方登录授权表，存储第三方授权信息

## 使用说明

### 1. 数据库初始化
执行 `src/main/resources/db/sql/init_user_tables.sql` 脚本创建所需表结构。

### 2. 邮箱配置
参考 `config/application-mail-example.yml` 配置邮件服务，用于发送邮箱验证码。

### 3. 接口说明

#### 3.1 发送邮箱验证码
**接口地址**: `POST /user/auth/sendEmailCode`

**请求参数**:
```json
{
  "email": "test@example.com",  // 邮箱地址
  "scene": "login"               // 场景: login-登录, register-注册, reset-重置密码
}
```

**响应示例**:
```json
{
  "success": true,
  "code": 200,
  "message": "验证码发送成功",
  "result": null
}
```

#### 3.2 邮箱验证码登录
**接口地址**: `POST /user/auth/emailLogin`

**请求参数**:
```json
{
  "email": "test@example.com",  // 邮箱地址
  "code": "123456"             // 6位数字验证码
}
```

**响应示例**:
```json
{
  "success": true,
  "code": 200,
  "result": {
    "token": "token_123456_1234567890",
    "refreshToken": "refreshToken_123456_1234567890",
    "userInfo": {
      "id": 123456,
      "email": "test@example.com",
      "nickname": "用户1234567890",
      "status": 1
    },
    "isNewUser": true
  }
}
```

#### 3.3 账号密码登录
**接口地址**: `POST /user/auth/passwordLogin`

**请求参数**:
```json
{
  "account": "test@example.com",  // 账号: 支持用户名、手机号、邮箱
  "password": "123456"           // 密码
}
```

**响应示例**:
```json
{
  "success": true,
  "code": 200,
  "result": {
    "token": "token_123456_1234567890",
    "refreshToken": "refreshToken_123456_1234567890",
    "userInfo": {
      "id": 123456,
      "nickname": "用户昵称",
      "status": 1
    },
    "isNewUser": false
  }
}
```

#### 3.4 第三方登录
**接口地址**: `POST /user/auth/thirdPartyLogin`

**请求参数**:
```json
{
  "identityType": "APPLE",  // 登录渠道类型: APPLE, GOOGLE, ALIPAY, WECHAT
  "identifier": "user123",   // 第三方唯一标识
  "credential": "token123",  // 授权凭证/Token
  "nickname": "用户昵称",    // 第三方平台昵称
  "avatar": "头像URL",       // 第三方平台头像
  "phone": "13800138000",   // 手机号(首次登录时可选)
  "email": "test@example.com" // 邮箱(首次登录时可选)
}
```

**响应示例**:
```json
{
  "success": true,
  "code": 200,
  "result": {
    "token": "token_123456_1234567890",
    "refreshToken": "refreshToken_123456_1234567890",
    "userInfo": {
      "id": 123456,
      "nickname": "用户昵称",
      "avatar": "头像URL",
      "status": 1
    },
    "isNewUser": true
  }
}
```

## 扩展新的登录方式

如果需要添加新的登录方式，只需按照以下步骤操作：

### 步骤1: 在IdentityTypeEnum中添加新的类型
```java
public enum IdentityTypeEnum {
    APPLE("APPLE", "Apple账号"),
    GOOGLE("GOOGLE", "Google账号"),
    WECHAT("WECHAT", "微信"),
    ALIPAY("ALIPAY", "支付宝"),
    // 新增
    GITHUB("GITHUB", "GitHub");
    
    // ... 其他代码
}
```

### 步骤2: 创建新的登录处理器
```java
@Slf4j
@Component
public class GithubLoginHandler implements ThirdPartyLoginHandler {

    @Override
    public String getIdentityType() {
        return IdentityTypeEnum.GITHUB.getCode();
    }

    @Override
    public boolean verifyCredential(ThirdPartyLoginDTO loginDTO) {
        // 实现GitHub授权验证逻辑
        log.info("验证GitHub登录凭证, identifier: {}", loginDTO.getIdentifier());
        // TODO: 调用GitHub API验证token
        return true;
    }
    
    // 可选：重写getUserInfo方法获取更多用户信息
    @Override
    public ThirdPartyUserInfo getUserInfo(ThirdPartyLoginDTO loginDTO) {
        // 调用GitHub API获取用户信息
        return ThirdPartyUserInfo.builder()
                .nickname("GitHub用户")
                .avatar("头像URL")
                .build();
    }
}
```

### 步骤3: 配置第三方平台参数
在 `ThirdPartyLoginConfig` 中添加新平台的配置参数，或在 `application.yml` 中配置。

## 安全注意事项

1. **密码加密**: 示例中使用BCrypt加密密码，生产环境必须对密码进行加密存储
2. **Token生成**: 示例中的Token生成是简化实现，实际项目中应使用JWT等标准方案
3. **安全性**: 必须实现第三方授权凭证的验证逻辑，确保登录安全性
4. **验证码有效期**: 邮箱验证码默认5分钟有效期，60秒内不能重复发送
5. **限流机制**: 建议添加登录接口限流，防止暴力破解
6. **事务处理**: 登录流程已使用@Transactional确保数据一致性

## 后续优化建议

1. 集成 JustAuth 库简化OAuth流程
2. 实现JWT Token生成和刷新机制
3. 添加登录日志记录
4. 实现账号绑定/解绑功能
5. 添加限流和防刷机制
6. 实现第三方配置的动态管理
7. 添加手机号验证码登录功能
8. 实现忘记密码/重置密码功能
9. 添加社交账号绑定/解绑功能
10. 实现多设备登录管理
