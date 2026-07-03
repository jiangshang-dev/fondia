package org.jeecg.modules.user.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.extra.mail.MailUtil;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.user.service.IVerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现
 */
@Slf4j
@Service
public class VerificationCodeServiceImpl implements IVerificationCodeService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 验证码前缀
     */
    private static final String CODE_PREFIX = "user:code:";

    /**
     * 验证码有效期（分钟）
     */
    private static final int CODE_EXPIRE_MINUTES = 5;

    /**
     * 验证码重试间隔（秒）
     */
    private static final int CODE_RETRY_SECONDS = 60;

    @Override
    public void sendEmailCode(String email, String scene) {
        String retryKey = CODE_PREFIX + "retry:" + scene + ":" + email;
        
        // 检查是否频繁发送
        if (Boolean.TRUE.equals(redisTemplate.hasKey(retryKey))) {
            throw new JeecgBootException("验证码发送频繁，请稍后再试");
        }

        // 生成验证码
        String code = generateCode();
        String codeKey = CODE_PREFIX + scene + ":" + email;

        // 保存验证码到Redis
        redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        // 设置重试间隔
        redisTemplate.opsForValue().set(retryKey, "1", CODE_RETRY_SECONDS, TimeUnit.SECONDS);

        try {
            // 使用Hutool发送邮件
            String title = "验证码";
            String content = String.format("您的验证码是：%s，%s分钟内有效。请勿泄露给他人。", code, CODE_EXPIRE_MINUTES);
            // todo 发送邮件，上生产时记得打开注释
            // MailUtil.send(email, title, content, false);
            log.info("验证码: {}", code);
            log.info("发送邮箱验证码成功, email: {}, scene: {}, code: {}", email, scene, code);
        } catch (Exception e) {
            log.error("发送邮箱验证码失败", e);
            // 发送失败时清理Redis
            redisTemplate.delete(codeKey);
            redisTemplate.delete(retryKey);
            throw new JeecgBootException("验证码发送失败，请稍后再试");
        }
    }

    @Override
    public boolean verifyEmailCode(String email, String scene, String code) {
        return checkCode(buildEmailCodeKey(email, scene), code);
    }

    @Override
    public void sendPhoneCode(String phone, String scene) {
        String retryKey = CODE_PREFIX + "retry:" + scene + ":phone:" + phone;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(retryKey))) {
            throw new JeecgBootException("验证码发送频繁，请稍后再试");
        }

        String code = generateCode();
        String codeKey = buildPhoneCodeKey(phone, scene);
        redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(retryKey, "1", CODE_RETRY_SECONDS, TimeUnit.SECONDS);

        // todo 接入短信服务，生产环境请替换为真实短信发送
        log.info("发送手机验证码成功, phone: {}, scene: {}, code: {}", phone, scene, code);
    }

    @Override
    public boolean verifyPhoneCode(String phone, String scene, String code) {
        return checkCode(buildPhoneCodeKey(phone, scene), code);
    }

    @Override
    public boolean verifyAndConsumeEmailCode(String email, String scene, String code) {
        String codeKey = CODE_PREFIX + scene + ":" + email;
        if (!checkCode(codeKey, code)) {
            return false;
        }
        redisTemplate.delete(codeKey);
        return true;
    }

    @Override
    public boolean verifyAndConsumePhoneCode(String phone, String scene, String code) {
        String codeKey = buildPhoneCodeKey(phone, scene);
        if (!checkCode(codeKey, code)) {
            return false;
        }
        redisTemplate.delete(codeKey);
        return true;
    }

    private String buildEmailCodeKey(String email, String scene) {
        return CODE_PREFIX + scene + ":" + email;
    }

    private String buildPhoneCodeKey(String phone, String scene) {
        return CODE_PREFIX + scene + ":phone:" + phone;
    }

    private boolean checkCode(String codeKey, String code) {
        String storedCode = redisTemplate.opsForValue().get(codeKey);
        return storedCode != null && storedCode.equals(code);
    }

    @Override
    public String generateCode() {
        // 生成6位随机数字
        return RandomUtil.randomNumbers(6);
    }
}
