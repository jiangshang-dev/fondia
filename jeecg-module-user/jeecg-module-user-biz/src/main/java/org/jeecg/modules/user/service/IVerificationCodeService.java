package org.jeecg.modules.user.service;

/**
 * 验证码服务接口
 */
public interface IVerificationCodeService {

    /**
     * 发送邮箱验证码
     * @param email 邮箱
     * @param scene 场景
     */
    void sendEmailCode(String email, String scene);

    /**
     * 验证邮箱验证码
     * @param email 邮箱
     * @param scene 场景
     * @param code 验证码
     * @return 是否验证通过
     */
    boolean verifyEmailCode(String email, String scene, String code);

    /**
     * 生成6位随机数字验证码
     * @return 验证码
     */
    String generateCode();
}
